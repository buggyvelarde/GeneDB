summary() {
    echo "load GO terms from a gene association file"
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` goa [options] <file>
Options:
  -x goTermErrorsAreNotFatal
    If this property is set, it is not a fatal error if a GO term mentioned in the
    input file does not exist.
USAGE
    standard_options
    echo
}

loaderHelp() {
    cat <<HELP
Load GO annotations from a gene association file.
HELP
}

doLoad() {
    options=''
    debug=false

    OPTIND=0
    while getopts "dx:$stdopts" option; do
        case "$option" in
        d)  debug=true
            ;;
        x)  case "$OPTARG" in
            goTermErrorsAreNotFatal)
                options="$options --go-term-errors-are-not-fatal"
                ;;
            *)  loaderUsage >&2
                exit 1
                ;;
            esac
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
    java -Xmx1G \
        $database_properties \
        org.genedb.db.loading.auxiliary.Load goaloader $options "$file"
    
    echo "Deleting redundant GO terms"
    java $database_properties org.genedb.db.loading.auxiliary.DeleteRedundantGOTerms
}
