
#Assuming every transcript has a rank-zero exon -- which is checked
#by transcript-has-rank-zero-exon.sql -- it suffices to check that:
#
#1. The number of exons is equal to 1 + the maximum rank
#2. No two exons of the same transcript have the same rank
#
#These two conditions hold if and only if the ranks for each transcript
#constitute an initial segment of the natural numbers.

select organism.common_name as organism, feature_id, uniquename
from feature transcript
join organism using (organism_id)
where transcript.type_id in (
      321 
    , 339 
    , 340 
    , 361 
)
and (
    select count(*)
    from feature_relationship exon_transcript
    join feature exon on exon_transcript.subject_id = exon.feature_id
    where exon_transcript.object_id = transcript.feature_id
    and   exon.type_id = 234 
    and   exon_transcript.rank = 0
) <> (
    select 1 + max(exon_transcript.rank)
    from feature_relationship exon_transcript
    join feature exon on exon_transcript.subject_id = exon.feature_id
    where exon_transcript.object_id = transcript.feature_id
    and   exon.type_id = 234 
    and   exon_transcript.rank = 0
)
;



