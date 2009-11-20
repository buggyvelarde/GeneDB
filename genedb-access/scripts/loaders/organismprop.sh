#!/bin/bash

summary() {
    echo "load organism props"
}

loaderUsage() {
    cat <<USAGE

    -genus genius -species exceptional human -cv genedb_misc -cvterm htmlFullName -value new value

genus       the orgnanim's genus field
species     the organism's species field
cv          the name of the controlled vocabulary to type the property
cvterm      the cvterm used to type the property
value       the value of the property

NOTE : this loader depends on having the path to the psu/genlib/python/ libraries in you PYTHONPATH. Contact Giles for info.

USAGE
}

loaderHelp() {
    cat <<HELP

This is used for loading organismprops into the database, by passing in there
values as commandline properties. It can update or create new properties. 

HELP
}

doLoad() {
    
    python -c "from loaders.organismprop_loader import doLoad; doLoad();" "$@"
}
