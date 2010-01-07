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
    OPTIND=0
    while getopts "$stdopts" option; do
        process_standard_options "$option"
    done
    shift $[ $OPTIND - 1 ]
    
    python -c "from loaders.phylonodeprop_loader import doLoad; doLoad();" $database_properties "$@"
    
}
