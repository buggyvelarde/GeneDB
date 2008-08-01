/*
Assuming every transcript has a rank-zero exon -- which is checked
by transcript-has-rank-zero-exon.sql -- it suffices to check that:

 1. The number of exons is equal to 1 + the maximum rank
 2. No two exons of the same transcript have the same rank

These two conditions hold if and only if the ranks for each transcript
constitute an initial segment of the natural numbers.
*/

select organism.common_name as organism, feature_id, uniquename
from feature transcript
join organism using (organism_id)
where transcript.type_id in (
      321 /*mRNA*/
    , 339 /*rRNA*/
    , 340 /*tRNA*/
    , 361 /*snRNA*/
)
and (
    select count(*)
    from feature_relationship exon_transcript
    join feature exon on exon_transcript.subject_id = exon.feature_id
    where exon_transcript.object_id = transcript.feature_id
    and   exon.type_id = 234 /*exon*/
    and   exon_transcript.rank = 0
) <> (
    select 1 + max(exon_transcript.rank)
    from feature_relationship exon_transcript
    join feature exon on exon_transcript.subject_id = exon.feature_id
    where exon_transcript.object_id = transcript.feature_id
    and   exon.type_id = 234 /*exon*/
    and   exon_transcript.rank = 0
)
;

select organism.common_name as organism
     , transcript.uniquename as transcript
     , transcript_exon1.rank
     , transcript_exon2.rank
     , transcript_exon1.feature_relationship_id as exon1_feature_relationship_id
     , exon1.uniquename as exon1
     , transcript_exon2.feature_relationship_id as exon2_feature_relationship_id
     , exon2.uniquename as exon2
from feature transcript
join organism using (organism_id)
join feature_relationship transcript_exon1 on transcript_exon1.object_id = transcript.feature_id
join feature exon1 on transcript_exon1.subject_id = exon1.feature_id
join feature_relationship transcript_exon2 on transcript_exon2.object_id = transcript.feature_id
join feature exon2 on transcript_exon2.subject_id = exon2.feature_id
where transcript.type_id = 321 --mRNA
and   exon1.type_id = 234 --exon
and   exon2.type_id = 234 --exon
and   transcript_exon1.feature_relationship_id < transcript_exon2.feature_relationship_id
and   transcript_exon1.rank = transcript_exon2.rank
;


/*
select organism.common_name as organism
     , transcript.uniquename as transcript
     , transcript_exon.rank
     , exon.feature_id as exon_feature_id
     , exon.uniquename as exon
from feature transcript
join organism using (organism_id)
join feature_relationship transcript_exon on transcript_exon.object_id = transcript.feature_id
join feature exon on transcript_exon.subject_id = exon.feature_id
where transcript.type_id = 321 --mRNA
and   exon.type_id = 234 --exon
and   transcript.feature_id = ?
;
*/