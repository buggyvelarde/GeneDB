#Looks for cases where a transcript has no polypeptides.

select organism.common_name as organism
     , transcript.feature_id as transcript_id
     , transcript.uniquename as transcript_uniquename
from feature transcript
join organism using (organism_id)
where transcript.type_id = 321 /*mRNA*/
and not exists (
    select *
    from feature_relationship polypeptide_transcript
    join feature polypeptide on polypeptide_transcript.subject_id = polypeptide.feature_id
    where polypeptide_transcript.object_id = transcript.feature_id
    and   polypeptide.type_id = 191 /*polypeptide*/
)
;


