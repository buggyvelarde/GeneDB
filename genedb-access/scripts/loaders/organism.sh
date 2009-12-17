#!/bin/bash

summary() {
    echo "load organism"
}

loaderUsage() {
    python -c "from loaders.organism_loader import parser; parser.print_help();"
    standard_options
}

loaderHelp() {
    cat <<HELP

This is used for loading organism into the database, by passing in there
values as commandline properties. It can update or create new properties. 

HELP
}

doLoad() {
    declare -a options
    OPTIND=0
    while getopts "g:s:a:n:c:$stdopts" option; do
        case "$option" in
            [gsanc])
                options=("${options[@]}" "-$option" "$OPTARG")
                ;;
            *)
                process_standard_options "$option"
                ;;
        esac
    done
    shift $[ $OPTIND - 1 ]

    read_password
    python -c "from loaders.organism_loader import doLoad; doLoad();" $database_properties "${options[@]}"
}
