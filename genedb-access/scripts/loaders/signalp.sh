#!/bin/bash

summary() {
    echo "load SignalP predictions"
}

help() {
    echo ""
}

doLoad() {
    organism="$1"
    file="$2"

    java -Dload.organismCommonName="$organism" \
        org.genedb.db.loading.auxiliary Load signalploader "$file"
}
