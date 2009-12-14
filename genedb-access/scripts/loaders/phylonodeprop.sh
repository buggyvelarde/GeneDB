#!/bin/bash

summary() {
    echo "load phylonode props"
}

loaderUsage() {
    cat <<USAGE

    -phylonode Bbronchiseptica -cv genedb_misc -cvterm htmlFullName -value "<i>Bordetella bronchiseptica</i>"

phylonode   the phylonode's label field
cv          the name of the controlled vocabulary to type the property
cvterm      the cvterm used to type the property
value       the value of the property

NOTE : this loader depends on having the path to the psu/genlib/python/ libraries in you PYTHONPATH. Contact Giles for info.

USAGE
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
