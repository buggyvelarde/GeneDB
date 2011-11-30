#!/bin/bash

summary() {
    echo "add cloned genomic inserts from a GFF file"
}

loaderHelp() {
    cat <<HELP
Will accept a file with GFF3 style tab-delimited columns (9), where the last column must contain only an ID. e.g.

berg14	pbg	clone_genomic_insert	231580	239153	-	0	-	ID=PbG01-2349f12

The d

HELP
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` clonedgenomicinsert [-p]  [-d delimiter] <file>
Options:
  -d deleteall
    If set, will delete all features of type 'cloned_genomic_insert'. 
  -x delete
    If set, will delete the features specified in the file.
USAGE
    standard_options
    echo
}

doLoad() {
    delimiter='\t'
    delete=false
    
    OPTIND=0
    while getopts ":d:x$stdopts" option; do
        case "$option" in
        d)  deleteall="$OPTARG"
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
        org.genedb.db.loading.auxiliary.Load clonedGenomicInsertLoader \
        --delete="$delete" --deleteall="$deleteall" "$@"
}
