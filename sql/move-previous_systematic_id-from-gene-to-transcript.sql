select tr1.uniquename as transcript1
     , tr2.uniquename as transcript2
from feature gene
join feature_relationship tr1_gene on tr1_gene.object_id = gene.feature_id
join feature tr1 on tr1_gene.subject_id = tr1.feature_id
join feature_relationship tr2_gene on tr2_gene.object_id = gene.feature_id
join feature tr2 on tr2_gene.subject_id = tr2.feature_id
join feature_synonym gene_synonym on gene_synonym.feature_id = gene.feature_id
join synonym on gene_synonym.synonym_id = synonym.synonym_id
where gene.type_id = 792
and   gene.organism_id = 27
and   tr1.feature_id < tr2.feature_id
and   synonym.type_id = 26803
;

begin;

create temporary table feature_synonym_mod as
select transcript.feature_id as transcript_id
     , gene_synonym.feature_synonym_id
     , gene.uniquename
     , synonym.name as previous_systematic_id
from feature gene
join feature_relationship transcript_gene on transcript_gene.object_id = gene.feature_id
join feature transcript on transcript_gene.subject_id = transcript.feature_id
join feature_synonym gene_synonym on gene_synonym.feature_id = gene.feature_id
join synonym on gene_synonym.synonym_id = synonym.synonym_id
where synonym.type_id = 26803
and gene.type_id = 792
and gene.organism_id = 27
;

update feature_synonym
set feature_id = feature_synonym_mod.transcript_id
from feature_synonym_mod
where feature_synonym.feature_synonym_id = feature_synonym_mod.feature_synonym_id
;

commit;
