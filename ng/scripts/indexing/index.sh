#!/bin/bash

usage() {
cat <<OPTIONS
Usage: index.sh -t TMPDIR -p /path/to/psql-driver.jar [-o OUTDIR1,OUTDIR2] [-r org1,org2 | -s 2010-08-10] -c [-0] [-1] [-2] [-3] [-4] [-5] [-6] [-7]

You can choose to specify a list of organisms (-r) or a date (-s), if you specify neither then all organisms will be used. There are several actions in the workflow that you can call: 

 0. Copy the the pathogens db to nightly, and run SQL cleanup scripts.
 1. lucene indexing
 2. moving merged lucenes to the config location 
 3. berkley dto caching  
 4. merge the berkley caches (on its own because this can be problematic)
 5. move the berkley cache to the config location
 6. Copy the nightly db to staging

You can ommit any one or more of these actions (they are all off by default). However they will always be called in the same order.

Options:

 -t TMPDIR				tmpfolder
    The path to a temp folder.

 -p POSTGRES_DRIVER		/path/to/psql-driver.jar
    The path to the jdbc postgres driver jar. 

 -r ORGANISMS 			org1,org2,org3
    A comma-separated list of organisms. Do not run with -s flag. Not specifying either of -r or -s will default to all organisms being indexed.

 -s SINCE 				2010-08-10
    All organisms changed since this date. Do not run with -r flag. Not specifying either of -r or -s will default to all organisms being indexed.

 -o OUTDIRS             dir1,dir2,dir3
    A comma-separated list of output folders.

 -c CONFIG
    The name of the genedb config (e.g. beta, nightly, etc.) 

 -0 COPY_PATHOGEN_TO_NIGHTLY_AND_CLEANUP
    Clone the pathogen database to the nightly database, and run the cleanup scripts on it. Stage 0.

 -1 DO_INDEXING
 	Run the lucene indexing operations. Off by default. Stage 1. 

 -2 DO_MOVE_OF_INDEX_TO_CONFIG_LOCATION
    Move the merged indices to the location specified by the config file. If not specified, the berkley caching will run ontop of old indices.  Off by default. Stage 2.

 -3 DO_BERKLEY_CACHE
 	Run the berkley cache (DTO) indexing operations. Off by default. Stage 3. 
    
 -4 DO_MERGE_BERKLEY_CACHE
    Merge the berkley caches. Stage 4.
    
 -5 DO_MOVE_OF_CACHE_TO_CONFIG_LOCATION
    Move the merged cache to the location specified by the config file. Off by default. Stage 5.
    
 -6 COPY_NIGHTLY_TO_STAGING
    Copies the contents of the nightly database to the staging database. Stage 6.
 
 -7 RESTART_GENEDB
 	The final automated step. Restarts genedb www1 and www2 servers whilst copying the caches and indices.  
     
 
    
 
OPTIONS
}

logecho() {
    echo [`date '+%F %T'`] $1	
}

doeval() {
	command=$1
	logecho $command
	eval $command
	exitCode=$?    
	if [ $exitCode -ne 0 ];then
	    logecho "The script returned a status code of ${exitCode}. Exiting."
	    echo "processes :"
	    echo "`ps -ef`"
	    echo "top :"
	    echo "`top -b -c -n 1`"
	    echo "free :"
	    echo "`free -tm`"
	    exit ${exitCode}
	fi
}

VERSION=0.1
HST="pcs-genedb-dev"

if [ `uname -n` != ${HST} ]; then
    echo "This only runs on ${HST}. Not proceeding."
    exit 1
fi

while getopts "s:r:o:t:p:c:01234567v" o ; do  
    case $o in  
        s ) SINCE=$OPTARG;;
        r ) ORGANISMS=$(echo $OPTARG | tr "," "\n" );;
        o ) OUTDIRS=$(echo $OPTARG | tr "," "\n" );;
        t ) TMPDIR=$OPTARG;;
        p ) POSTGRES_DRIVER=$OPTARG;;
        0 ) COPY_PATHOGEN_TO_NIGHTLY_AND_CLEANUP=1;;
        1 ) DO_INDEXING=1;;
        2 ) DO_MOVE_OF_INDEX_TO_CONFIG_LOCATION=1;;
        3 ) DO_BERKLEY_CACHE=1;;
        4 ) DO_MERGE_BERKLEY_CACHE=1;;
        5 ) DO_MOVE_OF_CACHE_TO_CONFIG_LOCATION=1;;
        6 ) COPY_NIGHTLY_TO_STAGING=1;;
        7 ) RESTART_GENEDB=1;;
        c ) CONFIG=$OPTARG;;
        v ) echo $VERSION  
            exit 0;;
        ?) usage
                exit;;
    esac
done


if [[ -z $TMPDIR ]] || [[ -z $POSTGRES_DRIVER ]] || [[ -z $CONFIG ]]
then
    echo "You must supply a tempdir (-t), config (-c) and postgres driver (-p) parameters."
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

CONFIG_FILE=$SOURCE_HOME/property-file.$CONFIG

echo Using config file $CONFIG_FILE

if [ ! -f $CONFIG_FILE ]; then
	echo The file "$CONFIG_FILE" does not exist. 
	exit 1
fi



ORIGINAL_IFS=$IFS
IFS=$'\n'


if [[ $COPY_PATHOGEN_TO_NIGHTLY_AND_CLEANUP ]]; then

	echo Backing up db
	genedb-web-control ci-web stop
	sleep 20
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
	
	CLEANUP_BINS="groovy -cp $POSTGRES_DRIVER $SOURCE_HOME/scripts/indexing/CleanupBins.groovy $CONFIG commit "
	doeval $CLEANUP_BINS
	
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
    	# get everything... that's genedb public
        GET_ORGANISMS_SQL="SELECT DISTINCT(organism.common_name) FROM organismprop JOIN cvterm ON organismprop.type_id = cvterm.cvterm_id AND cvterm.name = 'genedb_public' JOIN organism ON organism.organism_id = organismprop.organism_id and organism.common_name != 'dummy' JOIN feature ON organism.organism_id = feature.organism_id WHERE value = 'yes' "
        ALL_ORGANISMS=1;
    fi
    
    logecho ${GET_ORGANISMS_SQL}
    ORGANISMS_COMMAND="ORGANISMS=\`psql -t -h pgsrv2 -c \"${GET_ORGANISMS_SQL}\" nightly\`"
    doeval $ORGANISMS_COMMAND
fi



#
# Here we make a big string of ":" separated organisms, because the groovy scripts below require this.
#
if [[ $DO_INDEXING ]] || [[ $DO_BERKLEY_CACHE ]]
then
	
	ORGANISMS_JOINED=""
	
	if [[ ALL_ORGANISMS -eq 0 ]]; then
	    logecho "Joining organism list"
	    for organism in $ORGANISMS
	    do
		    #regex / / to trim white spaces
	    	organism=${organism/ /} 
	    	if [[ $organism != 'dummy' ]]; then
		        ORGANISMS_JOINED="$ORGANISMS_JOINED:$organism"
	        fi
	    done
	    ORGANISMS_JOINED="${ORGANISMS_JOINED:1}"
	    
	fi
	
	logecho "Groovy Orgs List: $ORGANISMS_JOINED"
	
fi


if [[ $DO_INDEXING ]]; then
	
	logecho "Stage 1"
	
	#
	# Clean up tmp directories for the organisms to be indexed. 
	#
	
	for organism in $ORGANISMS
	do
		#regex / / to trim white spaces
        organism=${organism/ /}
        
		logecho "Cleaning up lucene: " $organism;
	    rm -fvr $TMPDIR/Lucene/output/$organism
	    rm -fvr $TMPDIR/Lucene/scripts/${organism}.script*
	done
	
	
		
	#
	# Generate lucene indicces and check for any errors.
	#
	
	mkdir -p $TMPDIR/Lucene/scripts
	GENERATE_LUCENE="groovy -cp $POSTGRES_DRIVER $SOURCE_HOME/scripts/indexing/GenerateBatchJobs.groovy Lucene $CONFIG $SOURCE_HOME $TMPDIR $ORGANISMS_JOINED "
	doeval $GENERATE_LUCENE
	
	exitCode=$?    
	if [ $exitCode -ne 0 ];then
	    logecho "The script returned a status code of ${exitCode}"
	    exit ${exitCode}
	fi

	
	LUCENE_ERRORS=`cat $TMPDIR/Lucene/scripts/*.err`
	LEN_LUCENE_ERRORS=${#LUCENE_ERRORS}
	if [[ $LUCENE_ERRORS > 0 ]]; then
	    logecho "Found errors in Lucene"
	    exit 1
	else
	    logecho "Found no errors in Lucene"
	fi
	
	
	
	#
	# Merge the lucene indices and copy them into place.
	#
	
	cd $SOURCE_HOME
	rm -fr $TMPDIR/Lucene/merged
	
	# gv1 - tested on laptop:
	# ant -f build-apps.xml -Dconfig=gv1-osx -Dmerge.lucene.destination=/Users/gv1/Desktop/lucene/merged/ -Dmerge.lucene.origin=/Users/gv1/Desktop/lucene/organisms/ runMergeLuceneIndices
	
	
	MERGE_LUCENE="ant -f build-apps.xml -Dconfig=$CONFIG -Dmerge.lucene.destination=$TMPDIR/Lucene/merged -Dmerge.lucene.origin=$TMPDIR/Lucene/output runMergeLuceneIndices"
	doeval $MERGE_LUCENE
	
	exitCode=$?    
	if [ $exitCode -ne 0 ];then
	    logecho "The script returned a status code of ${exitCode}"
	    exit ${exitCode}
	fi

	
	#
	# Generate the lucene dictionary on the final merged indices. To do this only once, it should be done before the lucene merged folder is copied. 
	#
	
	MAKE_DICTIONARY_LUCENE="ant -f build-apps.xml -Dconfig=$CONFIG -Ddir=$TMPDIR/Lucene/merged _LuceneDictionary"
	doeval $MAKE_DICTIONARY_LUCENE
	
	exitCode=$?    
	if [ $exitCode -ne 0 ];then
	    logecho "The script returned a status code of ${exitCode}"
	    exit ${exitCode}
	fi

	
    if [[ ! -z $OUTDIRS ]]; then
		for OUTDIR in $OUTDIRS
		do
		    logecho "Copying merged lucenes from $TMPDIR/Lucene/merged to $OUTDIR/lucene"
		    rm -fr $OUTDIR/lucene
		    mkdir -p $OUTDIR/lucene
		    cp -vr  $TMPDIR/Lucene/merged/*  $OUTDIR/lucene
		done
    fi
	
	
fi


if [[ $DO_MOVE_OF_INDEX_TO_CONFIG_LOCATION ]]; then
	
	logecho "Stage 2"
	
    #
    # Determine location of the indices as specified in the config file. 
    #
	
    LUCENE_LINE=`grep lucene.indexDirectory $CONFIG_FILE`
    LUCENE_INDEX_DIRECTORY=${LUCENE_LINE#lucene.indexDirectory=}
    
    #
    # Move the indices to the specified location.  
    #
    
    logecho Wiping $LUCENE_INDEX_DIRECTORY
    rm -frv $LUCENE_INDEX_DIRECTORY
    
    logecho Copying merged indices from "$TMPDIR/Lucene/merged/" to $LUCENE_INDEX_DIRECTORY
    mkdir -p $LUCENE_INDEX_DIRECTORY
    cp -vr  $TMPDIR/Lucene/merged/*  $LUCENE_INDEX_DIRECTORY
    
fi

if [[ $DO_BERKLEY_CACHE ]] || [[ $DO_MERGE_BERKLEY_CACHE ]]
then
	logecho "Cleaning up DTO: MERGEs"
	rm -fv $TMPDIR/DTO/scripts/MERGE.script*
fi

if [[ $DO_BERKLEY_CACHE	]];then
	
	logecho "Stage 3"
	
	#
	# Clean up tmp directories for the organisms to be indexed. 
	#
	
	for organism in $ORGANISMS
	do
		#regex / / to trim white spaces
        organism=${organism/ /}
        
        logecho "Cleaning up DTO: " $organism
        
	    rm -fvr $TMPDIR/DTO/output/$organism
	    rm -fvr $TMPDIR/DTO/scripts/${organism}.script*
        
	done
	
	
	#
	# Generate DTO caches and check for errors.
	#
	
	mkdir -p $TMPDIR/DTO/scripts
	GENERATE_DTO="groovy -cp $POSTGRES_DRIVER $SOURCE_HOME/scripts/indexing/GenerateBatchJobs.groovy DTO $CONFIG $SOURCE_HOME $TMPDIR $ORGANISMS_JOINED "
	doeval $GENERATE_DTO
	
	
	DTO_ERRORS=`cat $TMPDIR/DTO/scripts/*.err`
	LEN_DTO_ERRORS=${#DTO_ERRORS}
	if [[ $LEN_DTO_ERRORS > 0 ]]; then
	    logecho "Found errors in DTO"
	    exit 1
	else
	    logecho "Found no errors in DTO"
	fi
	

fi

if [[ $DO_MERGE_BERKLEY_CACHE ]];then	
	
	logecho "Stage 4"
	
	#
	# Merge the DTO caches into place. Sshing into a BIG MEM machine to do this. 
	#
	
	rm -fr $TMPDIR/DTO/merged
	
	# gv1 - tested on laptop:
	# ant -f build-apps.xml -Dconfig=gv1-osx-cachetest -Dmerge.indices.destination=/Users/gv1/Desktop/dto/merged/ -Dmerge.indices.origin=/Users/gv1/Desktop/dto/output/ runMergeIndices 
	
	MERGE_DTO="ant -f $SOURCE_HOME/build-apps.xml -Dconfig=$CONFIG -Dmerge.indices.destination=$TMPDIR/DTO/merged -Dmerge.indices.origin=$TMPDIR/DTO/output runMergeIndices"
	#groovy MergeCacheIndices.groovy SOURCE_HOME SCRIPT_DIR TO FROM
	#MERGE_DTO="groovy $SOURCE_HOME/scripts/indexing/MergeBerkleyIndicesJobManager.groovy $SOURCE_HOME $TMPDIR/DTO/scripts $TMPDIR/DTO/merged $TMPDIR/DTO/output"
	
	logecho $MERGE_DTO
	ssh pcs4m "$MERGE_DTO"
	#doeval $MERGE_DTO
	
	
	
	logecho Sleeping for 60 seconds to give NFS time to catch up with whats been happening...
	sleep 60
	logecho Awake!
	
	
	
	if [[ ! -z $OUTDIRS ]]; then
		for OUTDIR in $OUTDIRS
		do
		    logecho "Copying merged indices from $TMPDIR/DTO/merged to $OUTDIR/cache"
		    rm -fr $OUTDIR/cache;
		    mkdir -p $OUTDIR/cache
		    cp -vr $TMPDIR/DTO/merged/* $OUTDIR/cache;
		done
	fi
	

	
fi




if [[ $DO_MOVE_OF_CACHE_TO_CONFIG_LOCATION ]]; then
    
    logecho "Stage 5"
    
    #
    # Determine location of the indices as specified in the config file. 
    #
    
    CACHE_LINE=`grep cacheDirectory $CONFIG_FILE`
    CACHE_INDEX_DIRECTORY=${CACHE_LINE#cacheDirectory=}
    
    #
    # Move the indices to the specified location.  
    #
    
    logecho Wiping $CACHE_INDEX_DIRECTORY
    rm -frv $CACHE_INDEX_DIRECTORY
    
    logecho Copying merged caches from "$TMPDIR/DTO/merged/" to $CACHE_INDEX_DIRECTORY
    mkdir -p $CACHE_INDEX_DIRECTORY
    cp -vr  $TMPDIR/DTO/merged/*  $CACHE_INDEX_DIRECTORY
    
    
fi


#
# Copy the database accross from nightly. 
#

if [[ $COPY_NIGHTLY_TO_STAGING ]]; then
    echo "Stage 6"
    echo Copying db to staging
    dropdb -h genedb-db snapshot-old
    dropdb -h genedb-db staging
    createdb -h genedb-db staging
    pg_dump -h pgsrv2 nightly | psql -h genedb-db staging
    
    /nfs/pathdb/bin/push-staging-to-snapshot2 
 	/nfs/pathdb/bin/fix-snapshot 
fi

#
# Restart genedb
#
if [[ $RESTART_GENEDB ]]; then
	echo "Stage 7"
	echo Restarting...
	ssh pcs-genedb1 /nfs/pathdb/bin/copy_and_restart_genedb
	
fi
