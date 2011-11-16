#!/bin/bash

summary() {
    echo "add phosphopeptides to polypeptides"
}

loaderHelp() {
    cat <<HELP
Accepts a delimeted file of 2 columnts: the first one being gene/transcript/polypeptide names, the second being position on the polypeptide. You can set the delimiter using the -d option, and set it to delete the sites using the -x option.

HELP
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` renamefeatures [-p]  [-d delimiter] <file>
Options:
  -d delimiter
    The delimiter used to split the two columns (e.g. '\t' for tab)
  -x
    If true, then it will delete the specifid phosphopeptide sites (if it can find them), else it creates them (if it can find the gene)
USAGE
    standard_options
    echo
}

doLoad() {
    delimiter='\t'
    delete=false
    
    OPTIND=0
    while getopts "d:x$stdopts" option; do
        case "$option" in
        d)  delimiter="$OPTARG"
            ;;
        x)  delete=true
            ;;
        *)  process_standard_options "$option"
            ;;
        esac
    done
    
    shift $[ $OPTIND - 1 ]
    read_password

    java $database_properties -Dlog4j.configuration=log4j.loader.properties \
        org.genedb.db.loading.auxiliary.Load phosphopeptideLoader \
        --delimiter="$delimiter" --delete="$delete" "$@"
}
