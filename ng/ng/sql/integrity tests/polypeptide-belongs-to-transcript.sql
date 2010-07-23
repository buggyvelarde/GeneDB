#A polypeptide must always belong to a transcript

select organism.common_name as organism
       ,polypeptide.feature_id as feature_id
       ,polypeptide.uniquename
from feature polypeptide
join organism using (organism_id)
where polypeptide.type_id = (
          select cvterm.cvterm_id
          from cvterm join cv on cvterm.cv_id = cv.cv_id
          where cv.name = 'sequence'
            and cvterm.name = 'polypeptide')
and not exists (
    select *
    from feature_relationship polypeptide_transcript
    join feature transcript on polypeptide_transcript.object_id = transcript.feature_id
    where polypeptide_transcript.subject_id = polypeptide.feature_id
    and transcript.type_id in (
          select cvterm.cvterm_id
          from cvterm join cv on cvterm.cv_id = cv.cv_id
          where cv.name = 'sequence'
            and cvterm.name in ('transcript', 'mRNA', 'pseudogenic_transcript'))

)
;
