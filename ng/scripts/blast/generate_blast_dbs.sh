#!/bin/bash

logecho() {
    echo [`date '+%F %T'`] $1	
}

doeval() {
	command=$1
	logecho $command
	eval $command
	exitCode=$?    
	if [ $exitCode -ne 0 ];then
	    logecho "The script returned a status code of ${exitCode}. Exiting."
	    exit ${exitCode}
	fi
}

BLAST_DB_PATH = "/lustre/scratch101/blastdb/Pathogen/website/genedb/"

ORIGINAL_IFS=$IFS
IFS=$'\n'

GET_ORGANISMS_SQL="select distinct(o.common_name) from organism o, feature f where f.organism_id = o.organism_id and o.common_name != 'dummy'"
logecho ${GET_ORGANISMS_SQL}
ORGANISMS_COMMAND="ORGANISMS=\`psql -t -h pgsrv1.internal.sanger.ac.uk -U pathdb -c \"${GET_ORGANISMS_SQL}\" pathogens\`"
doeval $ORGANISMS_COMMAND
    
for organism in $ORGANISMS
do
	#regex / / to trim white spaces
	organism=${organism/ /} 
	if [[ $organism != 'dummy' ]]; then
     	#ORGANISMS_JOINED="$ORGANISMS_JOINED:$organism"
     	
     	logecho "Dumping ${organism} : "
     	
     	DUMP_PROTEINS="chado_dump_proteins -s -o ${organism} > $BLAST_DB_PATH/GeneDB_${organism}_Proteins"
     	doeval $DUMP_PROTEINS
     	
     	DUMP_PROTEINS="chado_dump_transcripts ${organism} $BLAST_DB_PATH/GeneDB_${organism}_Genes"
     	doeval $DUMP_PROTEINS
     	
     	DUMP_PROTEINS="chado_dump_genome -o ${organism} $BLAST_DB_PATH/GeneDB_${organism}_Contigs"
     	doeval $DUMP_PROTEINS
     	
     	logecho "Dumped!"
     	
    fi
done



    