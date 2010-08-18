#!/bin/bash

usage() {
cat <<OPTIONS
Usage: index.sh -o OUTDIR1,OUTDIR2 -t TMPDIR -p /path/to/psql-driver.jar [options]
Options:
 -o OUTDIRS dir1,dir2,dir3
    A comma-separated list of output folders.
 -t TMPDIR
    The path to a temp folder.
 -r ORGANISMS org1,org2,org3
    A comma-separated list of organisms. Do not run with -s flag. Not specifying either of -r or -s will default to all organisms being indexed.
 -s SINCE
    All organisms changed since this date. Do not run with -r flag. Not specifying either of -r or -s will default to all organisms being indexed.
 -p POSTGRES_DRIVER
    The path to the jdbc postgres driver jar
 -d DUMP_AND_CLEANUP_DB
    Copy the pathogens to nightly, and cleans up. 
 -c COPY_DB_TO_STAGING
    Copy the nightly to staging at the end of the process.
OPTIONS
}

VERSION=0.1
HST="pathdbsrv1a"

if [ `uname -n` != ${HST} ]; then
    echo "This normally runs on ${HST}. Not proceeding."
    exit 1
fi

while getopts "s:r:o:t:p:bcdv" o ; do  
    case $o in  
        s ) SINCE=$OPTARG;;
        r ) ORGANISMS=$(echo $OPTARG | tr "," "\n" );;
        o ) OUTDIRS=$(echo $OPTARG | tr "," "\n" );;
        t ) TMPDIR=$OPTARG;;
        p ) POSTGRES_DRIVER=$OPTARG;;
        d ) DUMP_AND_CLEANUP_DB=1;;
        c ) COPY_DB_TO_STAGING=1;;
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
echo $SCRIPT_DIRECTORY

SOURCE_HOME=`dirname $(readlink -f "${SCRIPT_DIRECTORY}/../")`
cd $SOURCE_HOME
echo Executing indexing at $SOURCE_HOME 

ORIGINAL_IFS=$IFS
IFS=$'\n'


if [[ $DUMP_AND_CLEANUP_DB ]]; then
    echo Backing up db
    ###genedb-web-control ci-web stop
    ###dropdb -h pgsrv2 nightly
    ###createdb -h pgsrv2 -E SQL-ASCII nightly
    ###pg_dump -Naudit -Naudit_backup -Ngraphs -h pgsrv1 pathogens | psql -h pgsrv2 nightly
    
    for sqlfile in $SOURCE_HOME/sql/cleanup/*.sql
    do
        echo "Processing SQL cleanup file? " $sqlfile
        ###psql -h pgsrv2 nightly < $sqlfile
    done
    
    cd $SOURCE_HOME
    echo "Backed up db"
fi

ALL_ORGANISMS=0;

if [[ -z $ORGANISMS ]]; then
    
    GET_ORGANISMS_SQL=""
    
    if [[ $SINCE ]]; then
        GET_ORGANISMS_SQL="select common_name from organism where organism_id in (select distinct(organism_id) from feature where timelastmodified >= '${SINCE}');"
    else
        GET_ORGANISMS_SQL="select common_name from organism"
        ALL_ORGANISMS=1;
    fi
    
    echo ${GET_ORGANISMS_SQL}
    ORGANISMS=`psql -t -h pgsrv1.internal.sanger.ac.uk -U pathdb -c "${GET_ORGANISMS_SQL}" pathogens`
    
fi


#
# Clean up tmp directories for the organisms to be indexed. 
#

for organism in $ORGANISMS
do
    echo "Cleaning up " $organism;
    rm -fr $TMPDIR/Lucene/output/$organism
    rm -fr $TMPDIR/Lucene/scripts/${organism}.script*
    rm -fr $TMPDIR/DTO/output/$organism
    rm -fr $TMPDIR/DTO/scripts/${organism}.script*
done


#
# The groovy scripts below use ":" separated organism lists as arguments.
#

ORGANISMS_JOINED=""

if [[ ALL_ORGANISMS -eq 0 ]]; then
    echo "Joining organism list"
    for organism in $ORGANISMS
    do
        ORGANISMS_JOINED="$ORGANISMS_JOINED:${organism}"
    done
    ORGANISMS_JOINED="${ORGANISMS_JOINED:1}"
    
fi

echo "Groovy Orgs List: $ORGANISMS_JOINED"

#
# Generate lucene indicces and check for any errors.
#

mkdir -p $TMPDIR/bulk/Lucene/scripts
GENERATE_LUCENE="groovy -cp $POSTGRES_DRIVER $SOURCE_HOME/src/org/genedb/web/mvc/model/GenerateBatchJobs.groovy Lucene nightly $SOURCE_HOME $TMPDIR $ORGANISMS_JOINED "
#echo $GENERATE_LUCENE
eval $GENERATE_LUCENE


LUCENE_ERRORS=`cat $TMPDIR/bulk/Lucene/scripts/*.err`
LEN_LUCENE_ERRORS=${#VAR}
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
rm -fr $TMPDIR/bulk/Lucene/destination

# gv1 - tested on laptop:
# ant -f build-apps.xml -Dconfig=gv1-osx -Dmerge.lucene.destination=/Users/gv1/Desktop/lucene/merged/ -Dmerge.lucene.origin=/Users/gv1/Desktop/lucene/organisms/ runMergeLuceneIndices


MERGE_LUCENE="ant -f build-apps.xml -Dconfig=nightly -Dmerge.lucene.destination=$TMPDIR/bulk/Lucene/destination -Dmerge.lucene.origin=$TMPDIR/bulk/Lucene/output runMergeLuceneIndices"
#echo $MERGE_LUCENE
eval $MERGE_LUCENE

for OUTDIR in $OUTDIRS
do
    echo "Copying merged lucenes to $OUTDIR/lucene"
    rm -fr $OUTDIR/lucene
    cp -r  $TMPDIR/bulk/Lucene/destination  $OUTDIR/lucene
done

# gv1 - hardcoded exit for testing
exit

#
# Generate the lucene dictionary on the final merged indices.
#

MAKE_DICTIONARY_LUCENE="ant -f build-apps.xml -Dconfig=nightly -Ddir=$OUTDIR/lucene _LuceneDictionary"
echo MAKE_DICTIONARY_LUCENE $MAKE_DICTIONARY_LUCENE
###${MAKE_DICTIONARY_LUCENE}




#
# Generate DTO caches and check for errors.
#

mkdir -p $TMPDIR/bulk/DTO/scripts
GENERATE_DTO="groovy -cp $POSTGRES_DRIVER $SOURCE_HOME/src/org/genedb/web/mvc/model/GenerateBatchJobs.groovy DTO nightly $SOURCE_HOME $TMPDIR $ORGANISMS_JOINED "
echo GENERATE_DTO $GENERATE_DTO
###${GENERATE_DTO}


DTO_ERRORS=`cat $TMPDIR/bulk/DTO/scripts/*.err`
LEN_DTO_ERRORS=${#VAR}
if [[ $LEN_DTO_ERRORS > 0 ]]; then
    echo "Found errors in DTO"
    exit 1
else
    echo "Found no errors in DTO"
fi


#
# Merge the DTO caches into place. Sshing into a BIG MEM machine to do this. 
#

###rm -fr $TMPDIR/bulk/DTO/destination

# gv1 - tested on laptop:
# ant -f build-apps.xml -Dconfig=gv1-osx-cachetest -Dmerge.indices.destination=/Users/gv1/Desktop/dto/merged/ -Dmerge.indices.origin=/Users/gv1/Desktop/dto/output/ runMergeIndices 

MERGE_DTO="ant -f $SOURCE_HOME/ant-build.xml -Dconfig=nightly -Dmerge.indices.destination=$TMPDIR/bulk/DTO/destination -Dmerge.indices.origin=$TMPDIR/bulk/DTO/output runMergeIndices"
echo $MERGE_DTO
###ssh pcs4s "$MERGE_DTO"

for OUTDIR in $OUTDIRS
do
    echo "Copying merged indices from $TMPDIR/bulk/DTO/destination to $OUTDIR/cache"
    ###rm -fr $OUTDIR/cache;
    ###cp -r $TMPDIR/bulk/DTO/destination $OUTDIR/cache;
done



#
# Copy the database accross from nightly. 
#

if [[ $COPY_DB_TO_STAGING ]]; then
    echo Copying db to staging
    ###dropdb -h genedb-db snapshot-old
    ###createdb -h genedb-db staging
    ###pg_dump -h pgsrv2 nightly | psql -h genedb-db staging
fi


