#!/bin/bash

summary() {
    echo "Load GPI anchor predictions"
}

loaderHelp() {
    cat <<HELP
Load GPI predictions, from the file produced by the dgpi_chado program.

HELP
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` dgpi -o <organism> -v <version> [-r] <file>
Options:
  -o organism
    The common name of the organism. You can get a list of organisms
    by running the command: chado_dump_genome --list
  -r
    Reload. If this option is present, existing gpi predictions for
    the specified organism will be deleted before the new ones are loaded.
USAGE
    standard_options
    echo
}

doLoad() {
    organism=''
    reload=false
    debug=false

    OPTIND=0
    while getopts "do:r$stdopts" option; do
        case "$option" in
        d)  debug=true
            ;;
        o)  organism="$OPTARG"
            ;;
        r)  reload=true
            ;;
        *)  process_standard_options "$option"
            ;;
        esac
    done
    shift $[ $OPTIND - 1 ]
    
    if [ -z "$organism" ]; then
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
        echo "reload"
       # java -Xmx256m $database_properties -Dlog4j.configuration=log4j.loader.properties \
       #     org.genedb.db.loading.auxiliary.ClearDGPI "$organism"
    fi

    #java $database_properties -Dlog4j.configuration=log4j.loader.properties \
    #    org.genedb.db.loading.auxiliary.Load dgpiloader \
    #    "$@"
}
