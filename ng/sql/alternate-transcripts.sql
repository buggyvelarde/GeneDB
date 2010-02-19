/*
 * Change the form of the uniquename of alternate transcripts
 * from foo.1:mRNA to plain foo.1
 */

begin;

create temporary table alternately_spliced_gene on commit drop as
select gene.feature_id, gene.uniquename
from feature gene
where gene.type_id = (
  select cvterm.cvterm_id
  from cvterm join cv using (cv_id)
  where cv.name = 'sequence' and cvterm.name = 'gene'
)
and 1 < (
  select count(*) from feature_relationship
  where object_id = gene.feature_id
)
;

create temporary table alternately_spliced_transcript on commit drop as
select transcript.organism_id
     , transcript.feature_id as transcript_id
     , transcript.uniquename as transcript_uniquename
     , alternately_spliced_gene.feature_id as gene_id
     , alternately_spliced_gene.uniquename as gene_uniquename
from feature transcript
join feature_relationship transcript_gene on transcript_gene.subject_id = transcript.feature_id
join alternately_spliced_gene on transcript_gene.object_id = alternately_spliced_gene.feature_id
;

create temporary table candidates_for_renaming on commit drop as
select organism_id
     , transcript_id
     , transcript_uniquename
     , substring(transcript_uniquename for length(transcript_uniquename)-5) as new_uniquename
from alternately_spliced_transcript
where gene_uniquename = substring(transcript_uniquename for length(gene_uniquename))
and substring(transcript_uniquename from 1+length(gene_uniquename)) ~ E'^\\.[0-9]+:mRNA$'
;

select *
from feature join candidates_for_renaming
    on feature.uniquename = candidates_for_renaming.new_uniquename
;

update feature
set uniquename = candidates_for_renaming.new_uniquename
from candidates_for_renaming
where feature.feature_id = candidates_for_renaming.transcript_id
and feature.uniquename = candidates_for_renaming.transcript_uniquename
;

rollback;
