#!/bin/bash

usage() {
cat <<OPTIONS
Usage: index.sh -o OUTDIR1,OUTDIR2 -t TMPDIR -p /path/to/psql-driver.jar [-r org1,org2 | -s 2010-08-10] [-1] [-2] [-3] [-4]

You can choose to specify a list of organisms (-r) or a date (-s), if you specify neither then all organisms will be used. There are several actions in the workflow that you can call: 

 1. dump to nightly and cleanup 
 2. lucene indexing 
 3. berkley dto caching 
 4. copying nightly to staging 

You can ommit any one or more of these actions (they are all off by default). However they will always be called in the same order.

Options:

 -o OUTDIRS 			dir1,dir2,dir3
    A comma-separated list of output folders.

 -t TMPDIR				tmpfolder
    The path to a temp folder.

 -p POSTGRES_DRIVER		/path/to/psql-driver.jar
    The path to the jdbc postgres driver jar. 

 -r ORGANISMS 			org1,org2,org3
    A comma-separated list of organisms. Do not run with -s flag. Not specifying either of -r or -s will default to all organisms being indexed.

 -s SINCE 				2010-08-10
    All organisms changed since this date. Do not run with -r flag. Not specifying either of -r or -s will default to all organisms being indexed.

 -1 DUMP_AND_CLEANUP_DB
    Copy the pathogens to nightly, and cleans up. Off by default. Stage 1.

 -2 DO_INDEXING
 	Run the lucene indexing operations. Off by default. Stage 2. 

 -3 DO_BERKLEY_CACHE
 	Run the berkley cache (DTO) indexing operations. Off by default. Stage 3. 

 -4 COPY_DB_TO_STAGING
    Copy the nightly to staging at the end of the process. Off by default. Stage 4.

OPTIONS
}

VERSION=0.1
HST="pathdbsrv1a"

if [ `uname -n` != ${HST} ]; then
    echo "This only runs on ${HST}. Not proceeding."
    exit 1
fi

while getopts "s:r:o:t:p:1234v" o ; do  
    case $o in  
        s ) SINCE=$OPTARG;;
        r ) ORGANISMS=$(echo $OPTARG | tr "," "\n" );;
        o ) OUTDIRS=$(echo $OPTARG | tr "," "\n" );;
        t ) TMPDIR=$OPTARG;;
        p ) POSTGRES_DRIVER=$OPTARG;;
        1 ) DUMP_AND_CLEANUP_DB=1;;
        2 ) DO_INDEXING=1;;
        3 ) DO_BERKLEY_CACHE=1;;
        4 ) COPY_DB_TO_STAGING=1;;
        v ) echo $VERSION  
            exit 0;;
        ?) usage
                exit;;
    esac
done


if [[ -z $OUTDIRS ]] || [[ -z $TMPDIR ]] || [[ -z $POSTGRES_DRIVER ]]
then
    echo "You must supply a tempdir (-t) and outdir (-o) and postgres driver (-p) parameters"
    usage
    exit 1
fi


if [[ $ORGANISMS ]] && [[ $SINCE ]]; then
    echo "You can either -o or -s flags, but cannot combine them".
    usage
    exit 1
fi


SCRIPT_DIRECTORY=`dirname $(readlink -f $0)`
echo SCRIPT_DIRECTORY $SCRIPT_DIRECTORY

SOURCE_HOME=`dirname $(readlink -f "${SCRIPT_DIRECTORY}/../")`
cd $SOURCE_HOME
echo Executing indexing at $SOURCE_HOME 


ORIGINAL_IFS=$IFS
IFS=$'\n'


if [[ $DUMP_AND_CLEANUP_DB ]]; then
	echo "Stage 1"
    echo Backing up db
    genedb-web-control ci-web stop
    dropdb -h pgsrv2 nightly
    createdb -h pgsrv2 -E SQL-ASCII nightly
    pg_dump -Naudit -Naudit_backup -Ngraphs -h pgsrv1 pathogens | psql -h pgsrv2 nightly
    
    for sqlfile in $SOURCE_HOME/sql/cleanup/*.sql
    do
        echo "Processing SQL cleanup file? " $sqlfile
        psql -h pgsrv2 nightly < $sqlfile
    done
    
    cd $SOURCE_HOME
    echo "Backed up db"
fi

# a flag to indicate if all organisms should be indexed
ALL_ORGANISMS=0;

# if no organisms (-r) have been supplied
if [[ -z $ORGANISMS ]]; then
    
    GET_ORGANISMS_SQL=""
    
    # if a since (-s) has been supplied
    if [[ $SINCE ]]; then
        GET_ORGANISMS_SQL="select common_name from organism where organism_id in (select distinct(organism_id) from feature where timelastmodified >= '${SINCE}');"
    else
    	# get everything
        GET_ORGANISMS_SQL="select common_name from organism"
        ALL_ORGANISMS=1;
    fi
    
    echo ${GET_ORGANISMS_SQL}
    ORGANISMS=`psql -t -h pgsrv1.internal.sanger.ac.uk -U pathdb -c "${GET_ORGANISMS_SQL}" pathogens`
    
fi



#
# Here we make a big string of ":" separated organisms, because the groovy scripts below require this.
#
if [[ $DO_INDEXING ]] || [[ $DO_BERKLEY_CACHE ]]
then
	
	ORGANISMS_JOINED=""
	
	if [[ ALL_ORGANISMS -eq 0 ]]; then
	    echo "Joining organism list"
	    for organism in $ORGANISMS
	    do
		    #regex / / to trim white spaces
	    	organism=${organism/ /} 
	    	if [[ $organism != 'dummy' ]]; then
		        ORGANISMS_JOINED="$ORGANISMS_JOINED:${organism/ /}"
	        fi
	    done
	    ORGANISMS_JOINED="${ORGANISMS_JOINED:1}"
	    
	fi
	
	echo "Groovy Orgs List: $ORGANISMS_JOINED"
	
fi


if [[ $DO_INDEXING ]]; then
	
	echo "Stage 2"
	
	#
	# Clean up tmp directories for the organisms to be indexed. 
	#
	
	for organism in $ORGANISMS
	do
		echo "Cleaning up lucene: " $organism;
	    rm -fr $TMPDIR/Lucene/output/$organism
	    rm -fr $TMPDIR/Lucene/scripts/${organism}.script*
	done
	
	
		
	#
	# Generate lucene indicces and check for any errors.
	#
	
	mkdir -p $TMPDIR/Lucene/scripts
	GENERATE_LUCENE="groovy -cp $POSTGRES_DRIVER $SOURCE_HOME/src/org/genedb/web/mvc/model/GenerateBatchJobs.groovy Lucene nightly $SOURCE_HOME $TMPDIR $ORGANISMS_JOINED "
	echo $GENERATE_LUCENE
	eval $GENERATE_LUCENE
	
	
	LUCENE_ERRORS=`cat $TMPDIR/Lucene/scripts/*.err`
	LEN_LUCENE_ERRORS=${#LUCENE_ERRORS}
	if [[ $LUCENE_ERRORS > 0 ]]; then
	    echo "Found errors in Lucene"
	    exit 1
	else
	    echo "Found no errors in Lucene"
	fi
	
	
	
	#
	# Merge the lucene indices and copy them into place.
	#
	
	cd $SOURCE_HOME
	rm -fr $TMPDIR/Lucene/merged
	
	# gv1 - tested on laptop:
	# ant -f build-apps.xml -Dconfig=gv1-osx -Dmerge.lucene.destination=/Users/gv1/Desktop/lucene/merged/ -Dmerge.lucene.origin=/Users/gv1/Desktop/lucene/organisms/ runMergeLuceneIndices
	
	
	MERGE_LUCENE="ant -f build-apps.xml -Dconfig=nightly -Dmerge.lucene.destination=$TMPDIR/Lucene/merged -Dmerge.lucene.origin=$TMPDIR/Lucene/output runMergeLuceneIndices"
	echo $MERGE_LUCENE
	eval $MERGE_LUCENE
	
	
	#
	# Generate the lucene dictionary on the final merged indices. To do this only once, it should be done before the lucene merged folder is copied. 
	#
	
	MAKE_DICTIONARY_LUCENE="ant -f build-apps.xml -Dconfig=nightly -Ddir=$TMPDIR/Lucene/merged _LuceneDictionary"
	echo $MAKE_DICTIONARY_LUCENE
	eval $MAKE_DICTIONARY_LUCENE
	
	
	
	for OUTDIR in $OUTDIRS
	do
	    echo "Copying merged lucenes from $TMPDIR/Lucene/merged to $OUTDIR/lucene"
	    rm -fr $OUTDIR/lucene
	    mkdir -p $OUTDIR/lucene
	    cp -r  $TMPDIR/Lucene/merged/*  $OUTDIR/lucene
	done
	
	
	
fi

if [[ $DO_BERKLEY_CACHE	]];then
	
	echo "Stage 3"
	
	#
	# Clean up tmp directories for the organisms to be indexed. 
	#
	
	for organism in $ORGANISMS
	do
		echo "Cleaning up DTO: " $organism;
	    rm -fr $TMPDIR/DTO/output/$organism
	    rm -fr $TMPDIR/DTO/scripts/${organism}.script*
	done
	
	#
	# Generate DTO caches and check for errors.
	#
	
	mkdir -p $TMPDIR/DTO/scripts
	GENERATE_DTO="groovy -cp $POSTGRES_DRIVER $SOURCE_HOME/src/org/genedb/web/mvc/model/GenerateBatchJobs.groovy DTO nightly $SOURCE_HOME $TMPDIR $ORGANISMS_JOINED "
	echo $GENERATE_DTO
	eval $GENERATE_DTO
	
	
	DTO_ERRORS=`cat $TMPDIR/DTO/scripts/*.err`
	LEN_DTO_ERRORS=${#DTO_ERRORS}
	if [[ $LEN_DTO_ERRORS > 0 ]]; then
	    echo "Found errors in DTO"
	    exit 1
	else
	    echo "Found no errors in DTO"
	fi
	
	
	
	
	#
	# Merge the DTO caches into place. Sshing into a BIG MEM machine to do this. 
	#
	
	rm -fr $TMPDIR/DTO/merged
	
	# gv1 - tested on laptop:
	# ant -f build-apps.xml -Dconfig=gv1-osx-cachetest -Dmerge.indices.destination=/Users/gv1/Desktop/dto/merged/ -Dmerge.indices.origin=/Users/gv1/Desktop/dto/output/ runMergeIndices 
	
	MERGE_DTO="ant -f $SOURCE_HOME/build-apps.xml -Dconfig=nightly -Dmerge.indices.destination=$TMPDIR/DTO/merged -Dmerge.indices.origin=$TMPDIR/DTO/output runMergeIndices"
	echo $MERGE_DTO
	ssh pcs4m "$MERGE_DTO"
	
	
	
	
	echo Sleeping for 60 seconds to give NFS time to catch up with whats been happening...
	sleep 60
	echo Awake!
	
	
	
	
	for OUTDIR in $OUTDIRS
	do
	    echo "Copying merged indices from $TMPDIR/DTO/merged to $OUTDIR/cache"
	    rm -fr $OUTDIR/cache;
	    mkdir -p $OUTDIR/cache
	    cp -r $TMPDIR/DTO/merged/* $OUTDIR/cache;
	done
	
	

	
fi



#
# Copy the database accross from nightly. 
#

if [[ $COPY_DB_TO_STAGING ]]; then
	echo "Stage 4"
    echo Copying db to staging
    dropdb -h genedb-db snapshot-old
    createdb -h genedb-db staging
    pg_dump -h pgsrv2 nightly | psql -h genedb-db staging
fi


