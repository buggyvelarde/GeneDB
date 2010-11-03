#!/bin/bash

SCRIPT_DIRECTORY=`dirname $(readlink -f $0)`
echo SCRIPT_DIRECTORY $SCRIPT_DIRECTORY

SOURCE_HOME=`dirname $(readlink -f "${SCRIPT_DIRECTORY}/../")`
cd $SOURCE_HOME
echo Executing indexing at $SOURCE_HOME 

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
