#Any two exons should not have the same transcript

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
where transcript.type_id = 321 /*mRNA*/
and   exon1.type_id = 234 /*exon*/
and   exon2.type_id = 234 /*exon*/
and   transcript_exon1.feature_relationship_id < transcript_exon2.feature_relationship_id
and   transcript_exon1.rank = transcript_exon2.rank
;
