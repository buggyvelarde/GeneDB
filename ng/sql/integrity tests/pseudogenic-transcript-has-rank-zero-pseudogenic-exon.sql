
#Each psuedogenic transcript has a rank zero pseudogenic exon

select organism.common_name as organism
     , transcript.uniquename as transcript_uniquename
     , chromosome.uniquename as chromosome
     , featureloc.fmin
     , featureloc.fmax
from feature transcript
join organism using (organism_id)
join featureloc using (feature_id)
join feature chromosome on featureloc.srcfeature_id = chromosome.feature_id
where transcript.type_id = (
                            select cvterm.cvterm_id 
                            from cvterm join cv using (cv_id)
                            where cv.name = 'sequence'
                            and cvterm.name='pseudogenic_transcript'
                           )
and not exists (
    select *
    from feature_relationship exon_transcript
    join feature exon on exon_transcript.subject_id = exon.feature_id
    where exon_transcript.object_id = transcript.feature_id
    and   exon.type_id = (
                            select cvterm.cvterm_id 
                            from cvterm join cv using (cv_id)
                            where cv.name = 'sequence'
                            and cvterm.name='pseudogenic_exon'
                         )
    and   exon_transcript.rank = 0
)
order by organism.common_name
;
