#!/bin/bash

summary() {
    echo "load SNPs"
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` SNP  [options] <file or directory>
Options:

-s schema
    The name of the database schema in chado where the SNPs are to be loaded.
-t type, possible values are:
    -t SNP
    -t Pileup
-p file pattern:
    eg: -p .*\\\.SNP to load all files that end with .SNP

USAGE
    standard_options
    echo
}

loaderHelp() {
    cat <<HELP
The SNPs loader will load one or several SNP files into chado.
 
 If there's an error, then an error message will pop up in a window.
 You have the option to correct the error and retry, which can be a
 big timesaver when you're loading a number of files at once.
 
 
 Any other questions, comments or problems, please contact the
 software development team on <psu-dev@sanger.ac.uk>.

HELP
}

doLoad() {
    schema=''
    dataset=''
    filepattern=''
    properties=''
    debug=false
    	
    OPTIND=0
    while getopts "ds:t:p:x$stdopts" option; do
        case "$option" in
        d)  debug=true
            ;;
        s)  schema="$OPTARG"
            ;;
        t)  dataset="$OPTARG"
            ;;
        p)  filepattern="$OPTARG"
            ;;
        x)  case "$OPTARG" in
            # Documented options
            *) loaderUsage >&2
               exit 1
               ;;
            esac

            properties="$properties -Dload.$OPTARG"
            ;;
        *)  process_standard_options "$option"
            ;;
        esac
    done
    shift $[ $OPTIND - 1 ]
    
    if [ $# -ne 1 ]; then
        loaderUsage >&2
        exit 1
    fi
    
    file="$1"
    shift
    
    if [ ! -e "$file" ]; then
        echo >&2 "`basename $0`: no such file or directory '$file'"
        exit 1
    fi

    read_password

    if $debug; then
        echo "Classpath:"
        echo "$CLASSPATH" | perl -0777 -ne 'for (split(/:/,$_)) {print"\t$_\n"}'
        set -x
    fi

    if [ "$dataset" == "SNP" ]; then
    java -Xmx1G -Dlog4j.configuration=log4j.loader.properties \
         -Dload.inputDirectory="$file" \
         -Dload.fileNamePattern="$filepattern" \
         -Dload.dbSchema="$schema" \
         $properties $database_properties \
         org.genedb.db.loading.LoadSNPs
    fi
    if [ "$dataset" == "Pileup" ]; then
    java -Xmx1G -Dlog4j.configuration=log4j.loader.properties \
         -Dload.inputDirectory="$file" \
         -Dload.fileNamePattern="$filepattern" \
         -Dload.dbSchema="$schema" \
         $properties $database_properties \
         org.genedb.db.loading.LoadPileups
    fi

}
