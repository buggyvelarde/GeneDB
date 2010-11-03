#!/bin/bash

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

