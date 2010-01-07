#!/bin/bash

summary() {
    echo "load SignalP predictions"
}

loaderHelp() {
    cat <<HELP
Load signal peptide predictions, from the file produced by the SignalP program.

HELP
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` signalp -o <organism> -v <version> [-r] <file>
Options:
  -o organism
    The common name of the organism. You can get a list of organisms
    by running the command: chado_dump_genome --list
  -v version
    The version of SignalP that was used to generate the input file
  -r
    Reload. If this option is present, existing SignalP predictions for
    the specified organism will be deleted before the new ones are loaded.
USAGE
    standard_options
    echo
}

doLoad() {
    organism=''
    signalpVersion=''
    reload=false
    debug=false

    OPTIND=0
    while getopts "do:v:r$stdopts" option; do
        case "$option" in
        d)  debug=true
            ;;
        o)  organism="$OPTARG"
            ;;
        v)  signalpVersion="$OPTARG"
            ;;
        r)  reload=true
            ;;
        *)  process_standard_options "$option"
            ;;
        esac
    done
    shift $[ $OPTIND - 1 ]
    
    if [ -z "$signalpVersion" -o -z "$organism" ]; then
        loaderUsage >&2
        exit 1
    fi

    read_password

    if $debug; then
        echo "Classpath:"
        echo "$CLASSPATH" | perl -0777 -ne 'for (split(/:/,$_)) {print"\t$_\n"}'
        set -x
    fi
    
    if $reload; then
        java -Xmx256m $database_properties -Dlog4j.configuration=log4j.loader.properties \
            org.genedb.db.loading.auxiliary.ClearSignalP "$organism"
    fi

    java $database_properties -Dlog4j.configuration=log4j.loader.properties \
        org.genedb.db.loading.auxiliary.Load signalploader \
        --signalp-version="signalpVersion" "$@"
}
