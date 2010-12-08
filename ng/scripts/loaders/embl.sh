#!/bin/bash

summary() {
    echo "load an EMBL file"
}

loaderUsage() {
    cat <<USAGE
Usage: `basename $0` embl -o <organism> -t <type> [options] <file or directory>
Options:
  -o organism
    The common name of the organism. You can get a list of organisms
    by running the command: chado_dump_genome --list
  -t type, possible values are: 
    -t chromosome
    -t plasmid
    -t supercontig
    -t contig
    -t EST
    -t BAC_end
  -r
    Reload. If this option is specified, all genomic data for the specified
    organism are deleted before the load begins. Use with caution!
  -x overwriteExisting=yes
    Overwrite the existing feature with the same identifier, if there is one
  -x overwriteExisting=merge
    Merge the features in this file onto the existing feature with the
    same identifier. In this case, the sequence in the input file is
    ignored.
  -x ignoreFeatures='type1,type2,...'
    Ignore (i.e. archive) features of the specified types.
  -x ignoreQualifiers='qual1,qual2,...'
    Ignore (i.e. archive) qualifiers of the specified types.
    These can be restricted by feature type as well: for example
    you can specify -x ignoreQualifiers=CDS:similarity to archive
    all /similarity qualifiers on CDS features.
  	
USAGE
    standard_options
    echo
}

loaderHelp() {
    cat <<HELP
The EMBL loader will load one or several EMBL files, for the same organism,
into the database. The format expected is not standard EMBL, but rather the
in-house variant that has developed in Sanger Pathogen Genomics over the years.
There is some half-baked documentation on the wiki:

 http://mediawiki.internal.sanger.ac.uk/wiki/index.php/PSU_Standardised_Features
 http://mediawiki.internal.sanger.ac.uk/wiki/index.php/PSU_Standardised_Qualifiers

There's also some (slightly better) documentation of how the qualifiers are
mapped to Chado, here:

 http://mediawiki.internal.sanger.ac.uk/wiki/index.php/Chado_Data_Storage

Some important things to understand:

 * The chromosome (or contig/supercontig) name is taken from the ID
   line of the input file. Ideally this should be formatted in the
   standard way, as per the EMBL manual:
   
   http://www.ebi.ac.uk/embl/Documentation/User_manual/usrman.html#3_4_1
   
   If the ID line is properly formatted, then the taxonomic division
   does currently have a subtle effect on the behaviour of the loader.
   (Whether or not this is a good idea is questionable, and feel free
   to question it. It can be changed.) If the taxonomic division is
   PRO, indicating a prokaryotic genome, then /gene qualifiers indicate
   synonyms rather than primary names. That means you can have more
   than one /gene on each CDS, which prokaryotic EMBL files quite often
   do, but the downside is that the primary name might be wrongly stored
   as a synonym.
   
   Whether or not it's properly formatted, the first word on the ID line
   should be the systematic name of the chromosome/contig/supercontig.
 
 * If you want to load a supercontig together with the scaffolding
   information (describing how the supercontig is constructed from
   contigs) then the input file must be constructed in a special way.
   The data class must be ANN, and there should be a CO line that
   describes the scaffolding. See Section 3.4.14 of the EMBL manual
   for details:
   
   http://www.ebi.ac.uk/embl/Documentation/User_manual/usrman.html#3_4_14
 
 * Features or qualifiers that are not understood by the loader are
   recorded in the database in archived form. That means the information
   can be extracted later if necessary. Archived qualifiers appear in
   Artemis as /EMBL_qualifier; archived features can be seen if you
   turn on the display of obsolete features.

 Input files can be compressed with gzip, in which case they should
 have the file extension .embl.gz. You can also pass a directory name
 rather than a file name, in which case all .embl and .embl.gz files
 in the directory and its subdirectories will be loaded.
 
 If there's an error, then an error message will pop up in a window.
 You have the option to correct the error and retry, which can be a
 big timesaver when you're loading a number of files at once.
 
 
 Any other questions, comments or problems, please contact the
 software development team on <psu-dev@sanger.ac.uk>.

HELP
}

doLoad() {
    organism=''
    topLevel=''
    properties=''
    debug=false
    reload=false
	overwriteExisting=no
	
    OPTIND=0
    while getopts "do:t:x:r$stdopts" option; do
        case "$option" in
        d)  debug=true
            ;;
        o)  organism="$OPTARG"
            ;;
        t)  topLevel="$OPTARG"
            ;;
        x)  case "$OPTARG" in
            # Documented options
            overwriteExisting=yes)     ;;
            overwriteExisting=merge)   
            	overwriteExisting=merge
            	;;
            overwriteExisting=no)      ;;
            ignoreFeatures=*)          ;;
            ignoreQualifiers=*)        ;;
            
            # Undocumented options that do something:
            goTermErrorsAreNotFatal=*) ;;
            sloppyControlledCuration=*) ;;

            *) loaderUsage >&2
               exit 1
               ;;
            esac

            properties="$properties -Dload.$OPTARG"
            ;;
        r)  reload=true
            ;;
        *)  process_standard_options "$option"
            ;;
        esac
    done
    shift $[ $OPTIND - 1 ]
    
    if [ -z "$topLevel" -o -z "$organism" ]; then
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
    	
    	if [ "$overwriteExisting" != "merge" ]; then

	        export PGHOST="$dbhost" PGPORT="$dbport" PGDATABASE="$dbname" PGUSER="$dbuser"
	        psql --no-psqlrc <<SQL2
	        delete from feature where organism_id in (
	            select organism_id from organism where common_name = '${organism}'
	        );

    	    delete from synonym where synonym_id in (
    	      select synonym_id from synonym
    	      except (
    	          select synonym_id from feature_synonym
    	          union
    	          select synonym_id from library_synonym
    	      )
    	    );
SQL2
		fi
    fi

    read_password

    if $debug; then
        echo "Classpath:"
        echo "$CLASSPATH" | perl -0777 -ne 'for (split(/:/,$_)) {print"\t$_\n"}'
        set -x
    fi
    
    java -Xmx2000M -Dlog4j.configuration=log4j.loader.properties \
        -Dload.organismCommonName="$organism" -Dload.topLevel="$topLevel" \
         -Dload.inputDirectory="$file" \
         $properties $database_properties \
         org.genedb.db.loading.LoadEmbl
         
   #  java -Xmx1G -Dlog4j.configuration=log4j.loader.properties \
   #     -Dload.organismCommonName="$organism" \
   #      $properties $database_properties \
   #      org.genedb.db.fixup.FixResidues        
}
