#Cases where transcript has more than one polypeptide.
#It should just have one.

select organism.common_name as organism
     , transcript.feature_id as transcript_id
     , transcript.uniquename as transcript_uniquename
     , polypeptides.count as polypeptide_count
from feature transcript
join organism using (organism_id)
join (
       select polypeptide_transcript.object_id as transcript_id, count(*)
       from feature_relationship polypeptide_transcript
       join feature polypeptide on polypeptide_transcript.subject_id = polypeptide.feature_id
       where polypeptide.type_id = (
          select cvterm.cvterm_id
          from cvterm join cv on cvterm.cv_id = cv.cv_id
          where cv.name = 'sequence'
            and cvterm.name = 'polypeptide'
       )
       group by polypeptide_transcript.object_id
   ) polypeptides
   on transcript.feature_id = polypeptides.transcript_id
where transcript.type_id in (
          select cvterm.cvterm_id
          from cvterm join cv on cvterm.cv_id = cv.cv_id
          where cv.name = 'sequence'
          and cvterm.name in ('mRNA', 'pseudogenic_transcript')
)
and polypeptides.count > 1
;


