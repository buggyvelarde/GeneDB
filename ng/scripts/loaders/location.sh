#!/bin/bash

summary() {
    echo "Add locations to unlocated features in the datbase using a GFF file."
}

loaderHelp() {
    cat <<HELP
Will accept a file with GFF3 style tab-delimited columns (9), where the last column must contain only an ID. e.g.

berg14  pbg clone_genomic_insert    231580  239153  -   0   -   ID=PbG01-2349f12

This will then look for features in the database and if they have no locations use the ones in the GFF file. 

HELP
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` location [-p]  [-s sourceFeature] <file>
Options:
  -s sourceFeature
    The feature to locate everything onto (e.g. a chromosome or contig).
USAGE
    standard_options
    echo
}

doLoad() {
    sourceFeature=''
    
    OPTIND=0
    while getopts "s:$stdopts" option; do
        case "$option" in
        s)  sourceFeature="$OPTARG"
            ;;
        *)  process_standard_options "$option"
            ;;
        esac
    done
    
    shift $[ $OPTIND - 1 ]
    read_password

    java $database_properties -Dlog4j.configuration=log4j.loader.properties \
        org.genedb.db.loading.auxiliary.Load locationLoader \
        --sourceFeature="$sourceFeature" "$@"
}
