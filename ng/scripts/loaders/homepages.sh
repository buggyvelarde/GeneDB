#!/bin/bash

summary() {
    echo "load homepages"
}

loaderUsage() {
    standard_options
}

loaderHelp() {
    cat <<HELP

This is used for loading homepages from our wiki into the database. 

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
    python -c "from loaders.alchemy.wikihomepage_loader import main; main();" $database_properties "${options[@]}"
}
