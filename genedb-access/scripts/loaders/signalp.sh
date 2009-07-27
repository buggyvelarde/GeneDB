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
}

doLoad() {
    organism=''
    signalpVersion=''
    reload=false
    debug=false

    OPTIND=0
    while getopts "do:v:r" option; do
        case "$option" in
        d)  debug=true
            ;;
        o)  organism="$OPTARG"
            ;;
        v)  signalpVersion="$OPTARG"
            ;;
        r)  reload=true
            ;;
        *)  loaderUsage >&2
            exit 1
            ;;
        esac
    done
    shift $[ $OPTIND - 1 ]
    
    if [ -z "$signalpVersion" -o -z "$organism" ]; then
        loaderUsage >&2
        exit 1
    fi

    user="$LOGNAME@sanger.ac.uk"
    echo -n "Password for $user: "
    trap 'stty echo' EXIT ;# In case the user presses ^C, for example
    stty -echo
    read password
    stty echo
    trap - EXIT
    echo

    if $debug; then
        echo "Classpath:"
        echo "$CLASSPATH" | perl -0777 -ne 'for (split(/:/,$_)) {print"\t$_\n"}'
        set -x
    fi
    
    if $reload; then
        java -Xmx256m -Ddbuser="$user" -Ddbpassword="$password" \
            org.genedb.db.loading.auxiliary.ClearSignalP"$organism"
    fi

    java org.genedb.db.loading.auxiliary.Load signalploader \
        -Ddbuser="$user" -Ddbpassword="$password" \
        --signalp-version="signalpVersion" "$file"
}
