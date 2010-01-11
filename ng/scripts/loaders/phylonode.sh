#!/bin/bash

summary() {
    echo "Manipulate the phylonodes in the database"
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` phylonode [options] <command> <parameters>

`basename $0` phylonode list
    Print out the tree of phylonodes

`basename $0` phylonode insert <parent> <child>
    Insert a new phylonode with name <child> below the node <parent>.
    If there is an organism with common name <child>, that organism will
    be linked to the phylonode.
    
    The parent node can be specified either by label or by ID number.

`basename $0` phylonode delete <node>
    Delete the specified phylonode. Can be specified either by label
    or by ID number.

Options:
USAGE
    standard_options
    echo
}

loaderHelp() {
    cat <<HELP
If you don't know what this is for, then you should not use it.

HELP
}

doLoad() {
    OPTIND=0
    while getopts "$stdopts" option; do
        process_standard_options "$option"
    done
    shift $[ $OPTIND - 1 ]
    
    java -Dlog4j.configuration=log4j.loader.properties \
         org.genedb.db.adhoc.PhylonodeManager "jdbc:postgresql://$dbhost:$dbport/$dbname" "$dbuser" "$@"
}
