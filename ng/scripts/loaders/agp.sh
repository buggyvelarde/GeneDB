#!/bin/bash

summary() {
    echo "Load an AGP file"
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` agp -o <organism> [options] <file or directory>
Options:
  -o organism
    The common name of the organism. You can get a list of organisms
    by running the command: chado_dump_genome --list
  -t topLevel type, possible values are: 
    -t chromosome 
    -t supercontig (default)
  -c childLevel type, possible values are:
  	-c contig (default)
  	-c supercontig
  -x createMissingContigs=no (default)
    Do not create any child level features (usually contigs) that cannot be found
  -x overwriteExisting=yes
    If a child level feature cannot be found, create it.
  -x mode=1 (default)
    Most common scenario. Loads in a new assembly. Deletes any existing toplevelfeatures and creates
    new ones using the scaffolding information in the agp files. This is the default.
  -x mode=2
  	Rare, but can happen. Creates childlevel features and gaps and maps them onto already existing
  	toplevel features.
  -x putUnusedContigsInBin=no (default)
  	Put any unused child features (in mode 1) in a bin toplevel feature. Will look for a toplevel
  	feature of the type specified with a name like '%bin%'. Default no.
   	
USAGE
    standard_options
    echo
}

loaderHelp() {
    cat <<HELP
The AGP loader will load one or several AGP files, for the same organism,
into the database. 
Some important things to note:

 * The coordinates in the AGP file are _not_ interbase. The loader will convert them to interbase.

 * The systematic IDs in the AGP file have to match the uniquenames in the database.
   For example, in mode 1, the contigs (or other child level features) will be searched for
   by name in order to create the toplevel features and hence they need to match exactly.
   
 * The AGP loader expects the files to contain 9 fields per line separated by a tab (or 
   8 fields in the case of gaps)
   
 * Input files can be compressed with gzip, in which case they should
   have the file extension .agp.gz. You can also pass a directory name
   rather than a file name, in which case all .agp and .agp.gz files
   in the directory and its subdirectories will be loaded.
   
 Any other questions, comments or problems, please contact the
 software development team on <path-help@sanger.ac.uk>.

HELP
}

doLoad() {
    organism=''
    topLevel=chromosome
    childLevel=contig
    mode=''
    createMissingContigs=no
	
    OPTIND=0
    while getopts "do:t:c:x:$stdopts" option; do
        case "$option" in
        d)  debug=true
            ;;
        o)  organism="$OPTARG"
            ;;
        t)  topLevel="$OPTARG"
            ;;
        c)  childLevel="$OPTARG"
            ;;
        x)  case "$OPTARG" in
            # Documented options
            createMissingContigs=yes)     ;;
            createMissingContigs=no)      ;;
            mode=1)                       ;;
            mode=2)                       ;;
            putUnusedContigsInBin=yes)    ;;
            putUnusedContigsInBin=no)     ;;
           
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

    read_password

    if $debug; then
        echo "Classpath:"
        echo "$CLASSPATH" | perl -0777 -ne 'for (split(/:/,$_)) {print"\t$_\n"}'
        set -x
    fi
        
    java -Xmx2000M -Dlog4j.configuration=log4j.loader.properties \
         -Dload.organismCommonName="$organism" \
         -DtopLevel="$topLevel" \
         -DchildLevel="$childLevel" \
         -Dload.inputDirectory="$file" \
         $properties $database_properties \
         org.genedb.db.loading.LoadAGP
         
}
