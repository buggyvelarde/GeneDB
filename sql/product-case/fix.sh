#!/bin/bash

#export PGPORT=11111 PGHOST=localhost PGUSER=pathdb PGDATABASE=malaria_workshop

set -e

if [ -z "$PGPORT" ]; then
    echo >&2 "The environment variable PGPORT is not set"
    exit 64
fi
if [ -z "$PGHOST" ]; then
    echo >&2 "The environment variable PGHOST is not set"
    exit 64
fi
if [ -z "$PGUSER" ]; then
    echo >&2 "The environment variable PGUSER is not set"
    exit 64
fi
if [ -z "$PGDATABASE" ]; then
    echo >&2 "The environment variable PGDATABASE is not set"
    exit 64
fi


psql < get-products.sql | \
    perl punctuation-clashes.pl \
    > normalised-terms.txt

less normalised-terms.txt

perl canonicalise.pl normalised-terms.txt | \
    psql
