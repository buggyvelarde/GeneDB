#!/bin/bash

source "$GENEDB_HOME/$BIN_FOLDER/loaders/orthopara.ish"

summary() {
    echo "load paralogues"
}

loaderUsage() {
    orthoparaUsage "paralogues"
}

loaderHelp() {
    cat <<HELP
This is used for loading predicted paralogues into the database.
HELP
}

doLoad() {
    orthoparaLoad "paralogues" "$@"
}
