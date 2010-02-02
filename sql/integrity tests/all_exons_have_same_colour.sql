#This test checks if all the exons for a given transcript have the same rank=0 colour.

select organism.common_name as organism
     , transcript.feature_id as transcript_id
     , transcript.uniquename as transcript_name
     , count(distinct exon_colour.value) as number_of_exon_colours
from feature transcript
join feature_relationship exon_transcript on exon_transcript.object_id = transcript.feature_id
join feature exon on exon_transcript.subject_id = exon.feature_id
join featureprop exon_colour on exon_colour.feature_id = exon.feature_id
join organism on transcript.organism_id=organism.organism_id
where transcript.type_id in (select cvterm_id 
                             from cvterm
                             where name in ('snRNA', 'tRNA', 'transcript', 'pseudogenic_transcript', 'rRNA', 'mRNA'))
and exon.type_id in select cvterm_id
                    from cvterm
                    where name in ('exon', 'pseudogenic_exon')
and exon_colour.type_id = (select cvterm_id
                           from cvterm 
                           join cv using (cv_id)
                           where cvterm.name='colour'
                           and cv.name='genedb_misc')
and exon_colour.rank = 0
group by organism, transcript_id, transcript_name
having count(distinct exon_colour.value) > 1; 
                            
                            
                            
