-- This SQL makes the LBrazielensis genes the same size as their transcripts.
-- It looks as if the transcripts were extended to include the exons but the corresponding gene was not extended.
-- Was this done using Artemis?

-- nds 26.1.2010

select gene.feature_id as gene_id
     , gene.uniquename as gene
     , geneloc.featureloc_id as  geneloc
     , geneloc.fmin as genefmin
     , geneloc.fmax as genefmax
     , transcript.feature_id as transcript
     , transcriptloc.fmin as transcriptfmin
     , transcriptloc.fmax as transcriptfmax
from feature gene
join featureloc geneloc using (feature_id)
join feature_relationship transcript_gene on gene.feature_id=transcript_gene.object_id
join feature transcript on transcript.feature_id = transcript_gene.subject_id
join featureloc transcriptloc on transcriptloc.feature_id = transcript.feature_id
where transcript_gene.type_id = (
                                 select cvterm_id
                                 from cvterm
                                 where name='part_of'
                                 )
and geneloc.srcfeature_id = transcriptloc.srcfeature_id
and (geneloc.fmin != transcriptloc.fmin
    or geneloc.fmax != transcriptloc.fmax)
order by gene
;

     