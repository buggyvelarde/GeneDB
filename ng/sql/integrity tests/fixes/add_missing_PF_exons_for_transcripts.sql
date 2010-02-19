/* Add the missing Pfalciparum exons */

begin;

create temporary table pf_transcripts as
select organism.common_name as organism
     , organism.organism_id as organism_id
     , substr(transcript.uniquename, 0, length(transcript.uniquename)-10) || ':exon' as uniquename
     , transcript.feature_id as transcript_id
     , chromosome.uniquename as chromosome
     , chromosome.feature_id as chromosome_id
     , featureloc.fmin as fmin
     , featureloc.fmax as fmax
     , featureloc.strand as strand
from feature transcript
join organism using (organism_id)
join featureloc using (feature_id)
join feature chromosome on featureloc.srcfeature_id = chromosome.feature_id
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
                from feature_relationship exon_transcript
                join feature exon on exon_transcript.subject_id = exon.feature_id
                where exon_transcript.object_id = transcript.feature_id
                and exon.type_id = (
                         select cvterm.cvterm_id
                         from cvterm join cv on cvterm.cv_id=cv.cv_id
                         where cv.name = 'sequence'
                         and cvterm.name='exon')
                and exon_transcript.rank = 0)
and transcript.organism_id=27
and transcript.uniquename like '%:transcript'
order by organism.common_name
;

/* Add the exons, featurelocs, featurerelationships */

select count(*) from pf_transcripts;

insert into feature (organism_id, uniquename, type_id, is_analysis) 
select organism_id, uniquename, '234', 't' from pf_transcripts;

insert into featureloc (feature_id, srcfeature_id, fmin, fmax, strand)
select feature.feature_id, pf_transcripts.chromosome_id, pf_transcripts.fmin, pf_transcripts.fmax, pf_transcripts.strand
from feature
join pf_transcripts on feature.uniquename=pf_transcripts.uniquename
where feature.type_id=234;

insert into feature_relationship (subject_id, object_id, type_id)
select feature.feature_id, pf_transcripts.transcript_id, '42' from pf_transcripts, feature
where feature.type_id=234 and feature.uniquename=pf_transcripts.uniquename;


