#!/bin/bash -xv

NIGHTLY=/nfs/pathdb/genedb/nightly
SAFETY=${NIGHTLY}/backup
INVESTIGATION=${NIGHTLY}/failed
CVS=`basename "$0"`/../..

DB_NIGHTLY="-h pgsrv2 nightly"
DB_PATHOGENS="-h pgsrv1 pathogens"

mkdir $SAFETY
rm -fr $SAFETY/*

# Safety copies
cp -r $NIGHTLY/cache $SAFETY
cp -r $NIGHTLY/lucene $SAFETY


pg_dump -c -h pathdbsrv1-dmz nightly > ${SAFETY}/dump.sql


# Reset nightly
./ensure_db_not_in_use nightly
dropdb $DB_NIGHTLY
createdb $DB_NIGHTLY

# Copy pathogens into nightly and run fix-up scripts
pg_dump $DB_PATHOGENS | psql $DB_NIGHTLY

cd ${CVS}/genedb-ng/sql/cleanup
for sql in *.sql; do
  echo "Running cleanup script $sql"
  <"$sql" psql $DB_NIGHTLY
done



# Run incremental updates
#cd ${CVS}/genedb_web/
#ant -Ddeploy=nightly incremental_update

#if [$? -eq 1 ]; then
  #Failure
#  echo "Nightly update has failed during incremental updates"
#  mkdir $INVESTIGATION
#  mv $NIGHTLY/cache $INVESTIGATION
#  mv $NIGHTLY/lucene $INVESTIGATION
#  mv $SAFETY/cache $NIGHTLY
#  pg_dump -c nightly > ${INVESTIGATION}/dump.sql
#  mv $SAFETY/lucene $NIGHTLY
#  psql nightly < ${SAFETY}/dump.sql
#fi




# Success
#Extract the sequence ids from the copy, and update the production db

#echo "Nightly update completed - attempting to deploy web server"

#Deploy the nightly web server into position
#ant -Ddeploy=nightly final_deploy
