#!/bin/bash

summary() {
    echo "load phylonode props"
}

loaderUsage() {
    python -c "from loaders.phylonodeprop_loader import parser; parser.print_help();"
    standard_options
}

loaderHelp() {
    cat <<HELP

This is used for loading phylonode props into the database, by passing in there
values as commandline properties. It can update or create new properties. 

HELP
}

doLoad() {
    declare -a options
    OPTIND=0
    while getopts "p:c:t:v:$stdopts" option; do
        case "$option" in
            [pctv])
                options=("${options[@]}" "-$option" "$OPTARG")
                ;;
            *)
                process_standard_options "$option"
                ;;
        esac
    done
    shift $[ $OPTIND - 1 ]

    read_password
    python -c "from loaders.phylonodeprop_loader import doLoad; doLoad();" $database_properties "${options[@]}"
    
}
