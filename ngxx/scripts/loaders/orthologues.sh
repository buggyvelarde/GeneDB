#!/bin/bash

source "$GENEDB_HOME/$BIN_FOLDER/loaders/orthopara.ish"

summary() {
    echo "load orthologues"
}

loaderUsage() {
    orthoparaUsage "orthologues"
}

loaderHelp() {
    cat <<HELP
This is used for loading predicted orthologues into the database.
The input format is a simple tab-separated file, where each line has
four or five fields. There are two different formats, depending on
whether the data is in the form of pairwise predictions or predicted
clusters.

For pairwise predictions, the fields are:

 1. Source organism,
 2. Uniquename of source feature,
 3. Target organism,
 4. Uniquename of target feature,
 5. Optionally, the percentage identity.

For clustered predictions, the fields are:

 1. Organism,
 2. Uniquename of feature,
 3. The word "cluster",
 4. Cluster identifier.


HELP
}

doLoad() {
    orthoparaLoad "orthologues" "$@"
}
