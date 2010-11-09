#!/bin/bash

summary() {
    echo "load a FASTA file"
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` fasta -o <organism> [-t <type>] [-e <entry_type>] <file or directory>
Options:
  -o organism
    The common name of the organism. You can get a list of organisms
    by running the command: chado_dump_genome --list
  -t type, possible values are: 
    -t supercontig (default)
    -t chromosome
    -t plasmid
    -t none
  -e entry_type, possible values are:
    -e contig (default)
    -e EST
    -e BAC_end
  -r
    Reload. If this option is specified, all genomic data for the specified
    organism are deleted before the load begins. Use with caution!
USAGE
    standard_options
    echo
}

loaderHelp() {
    cat <<HELP
The FASTA loader will load one or several FASTA files, for the same organism,
into the database. Typically each FASTA file represents a supercontig, and each
entry represents a contig, but this is configurable.

If a directory rather than a file is specified, the directory is scanned for
files with the extension .fasta or .fasta.gz, and all these files are loaded.

The name of the supercontig (or whatever the FASTA file represents) is taken
from the filename, by removing the file extension.
HELP
}

doLoad() {
    organism=''
    topLevel=supercontig
    entryType=contig
    properties=''
    debug=false
    reload=false

    OPTIND=0
    while getopts "do:t:e:x:r$stdopts" option; do
        case "$option" in
        d)  debug=true
            ;;
        o)  organism="$OPTARG"
            ;;
        t)  topLevel="$OPTARG"
            ;;
        e)  entryType="$OPTARG"
            ;;
        r)  reload=true
            ;;
        x)  properties="$properties -Dload.$OPTARG"
            ;;
        *)  process_standard_options "$option"
            ;;
        esac
    done
    shift $[ $OPTIND - 1 ]
    
    if [ -z "$organism" ]; then
        loaderUsage >&2
        exit 1
    fi
    
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
    
    if $reload; then
    	
        export PGHOST="$dbhost" PGPORT="$dbport" PGDATABASE="$dbname" PGUSER="$dbuser"
        psql --no-psqlrc <<SQL2
        delete from feature where organism_id in (
            select organism_id from organism where common_name = '${organism}'
        );
SQL2
    fi
    
    
    read_password

    if $debug; then
        echo "Classpath:"
        echo "$CLASSPATH" | perl -0777 -ne 'for (split(/:/,$_)) {print"\t$_\n"}'
        set -x
    fi
    
    java -Xmx2G -Dlog4j.configuration=log4j.loader.properties \
        -Dload.organismCommonName="$organism" \
        -Dload.inputDirectory="$file" \
        -Dload.topLevel="$topLevel" -Dload.entryType="$entryType" \
        $properties $database_properties \
        org.genedb.db.loading.LoadFasta
}
