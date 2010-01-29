-- This sql identifies the transcripts that need to be extended to encompass their exons & utrs, and their corresponding genes. 
-- nds (26.1.2010)

begin; 

-- All the transcripts that do not encompass their components fully, and the corresponding genes

create temporary table short_features as
select  transcript.uniquename as transcript
     , transcriptloc.featureloc_id as transcriptloc
     , transcriptloc.fmin as transcriptfmin
     , transcriptloc.fmax as transcriptfmax
     , gene.feature_id as gene_id
     , gene.uniquename as gene
     , geneloc.featureloc_id as geneloc
     , min(subloc.fmin) as new_start
     , max(subloc.fmax) as new_end
from feature gene
join feature_relationship transcript_gene on gene.feature_id=transcript_gene.object_id
join feature transcript on transcript.feature_id=transcript_gene.subject_id 
join feature_relationship sub_transcript on sub_transcript.object_id=transcript.feature_id
join feature sub on sub_transcript.subject_id = sub.feature_id -- any feature that is part of the transcript
join featureloc transcriptloc on transcriptloc.feature_id = transcript.feature_id
join featureloc subloc on subloc.feature_id = sub.feature_id
join featureloc geneloc on geneloc.feature_id=gene.feature_id
where transcript.type_id in ( select cvterm_id 
                              from cvterm
                              where name in ('snRNA', 'tRNA', 'transcript', 'pseudogenic_transcript', 'rRNA', 'mRNA'))
and gene.type_id in (select cvterm_id
                    from cvterm
                    where name in ('gene', 'pseudogene'))
and transcriptloc.srcfeature_id = subloc.srcfeature_id 
and geneloc.srcfeature_id = transcriptloc.srcfeature_id
and transcriptloc.strand = subloc.strand
and sub_transcript.type_id = ( select cvterm_id
                               from cvterm
                               where name='part_of')
group by transcript, transcriptloc, transcriptfmin, transcriptfmax, gene_id, gene, geneloc
having transcriptloc.fmin > min(subloc.fmin) or
       transcriptloc.fmax < max(subloc.fmax)
;


-- Extend the transcripts  - fmin
update featureloc 
set fmin = (select new_start
            from short_features
            where short_features.transcriptloc=featureloc_id)
where featureloc_id in (select transcriptloc
                     from short_features);
                     
-- Extend the transcripts  - fmax
update featureloc 
set fmax = (select new_end
            from short_features
            where short_features.transcriptloc=featureloc_id)
where featureloc_id in (select transcriptloc
                     from short_features);
                     
-- Do a quick check to see if these genes all have just one transcript. If not, the gene has to be as long as the longest transcript

--select gene_id 
--from short_features
--where 1 < 
--(select count(*) 
--        from feature_relationship 
--        where gene_id = object_id
--        and type_id = 42);

                     
            
-- Extend the genes  - fmin
update featureloc 
set fmin = (select new_start
            from short_features
            where short_features.geneloc=featureloc_id)
where featureloc_id in (select geneloc
                        from short_features);
 
-- Extend the genes  - fmax
update featureloc 
set fmax = (select new_end
            from short_features
            where short_features.geneloc=featureloc_id)
where featureloc_id in (select geneloc
                        from short_features); 

