/*
 * This sql takes all the dangling transcripts in Etenella and adds the right link to its gene.
 * 
 */


begin;

create temporary table dangling_transcripts as
select substr(uniquename, 1, length(uniquename)-5) as gene
       , transcript.feature_id as object_id
       , transcript.uniquename as transcript
from feature transcript
join organism on transcript.organism_id=organism.organism_id
where transcript.type_id in (
    select cvterm.cvterm_id
    from cvterm join cv on cvterm.cv_id = cv.cv_id
    where cv.name = 'sequence'
    and cvterm.name in ('mRNA', 
                        'rRNA', 
                        'tRNA',
                        'snRNA',
                        'snoRNA',
                        'ncRNA',
                        'transcript'))
and not exists (
    select *
    from feature_relationship transcript_gene
    join feature gene on transcript_gene.object_id = gene.feature_id
    where transcript_gene.subject_id = transcript.feature_id
    and   gene.type_id = (
                          select cvterm.cvterm_id
                          from cvterm join cv on cvterm.cv_id = cv.cv_id
                          where cv.name = 'sequence'
                          and cvterm.name='gene'))
and transcript.organism_id=25
;



select feature_id, uniquename
from feature gene
where uniquename in (select gene from dangling_transcripts)
and type_id=792
and exists (select * from featureloc where feature_id=gene.feature_id)
;