#!/bin/bash

summary() {
    echo "rename existing features"
}

loaderHelp() {
    cat <<HELP
Rename existing features from a delimited file of 2 columns: the first one being old feature names, the second one being new feature names.

HELP
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` renamefeatures [-p]  [-d delimiter] <file>
Options:
  -d delimiter
    The delimiter used to split the two columns (e.g. '\t' for tab)
  -p 
    Match the first column to featureName prefixes, e.g. providing Pf3_d7 -> PF3D7 would convert Pf3_d7.100 -> PF3D7.100 and Pf3_d7.100:pep to PF3D7.100:pep, and so on.   
USAGE
    standard_options
    echo
}

doLoad() {
    delimiter='\t'
    matchPrefixOnly=false
    
    OPTIND=0
    while getopts "d:p$stdopts" option; do
        case "$option" in
        d)  delimiter="$OPTARG"
            ;;
        p)  matchPrefixOnly=true
            ;;
        *)  process_standard_options "$option"
            ;;
        esac
    done
    
    shift $[ $OPTIND - 1 ]
    read_password

    java $database_properties -Dlog4j.configuration=log4j.loader.properties \
        org.genedb.db.loading.auxiliary.Load renameFeatures \
        --delimiter="$delimiter" --matchPrefixOnly="$matchPrefixOnly" "$@"
}
