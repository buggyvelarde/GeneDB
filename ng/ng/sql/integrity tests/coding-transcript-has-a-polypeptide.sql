#Looks for cases where a transcript (mRNA) has no polypeptides.
#It should have atleast one.

select organism.common_name as organism
     , transcript.feature_id as transcript_id
     , transcript.uniquename as transcript_uniquename
from feature transcript
join organism using (organism_id)
where transcript.type_id = (
    select cvterm.cvterm_id
    from cvterm join cv on cvterm.cv_id = cv.cv_id
    where cv.name = 'sequence'
    and cvterm.name = 'mRNA'
)
and not exists (
    select *
    from feature_relationship polypeptide_transcript
    join feature polypeptide on polypeptide_transcript.subject_id = polypeptide.feature_id
    where polypeptide_transcript.object_id = transcript.feature_id
    and polypeptide.type_id = (
        select cvterm.cvterm_id
        from cvterm join cv on cvterm.cv_id = cv.cv_id
        where cv.name = 'sequence'
        and cvterm.name = 'polypeptide'
    )
)
order by organism
;


