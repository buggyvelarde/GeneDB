
#Checks that transcripts and their exons have consistent colouring

select organism.common_name as organism
     , transcript.uniquename as transcript
     , exon1.uniquename as exon1
     , exon1_colour.value as exon1_colour
     , exon1.timelastmodified as exon1_timestamp
     , exon2.uniquename as exon2
     , exon2_colour.value as exon2_colour
     , exon2.timelastmodified as exon2_timestamp
from feature transcript
join organism using (organism_id)
join feature_relationship transcript_exon1 on transcript_exon1.object_id = transcript.feature_id
join feature exon1 on transcript_exon1.subject_id = exon1.feature_id
join featureprop exon1_colour on exon1_colour.feature_id = exon1.feature_id
join feature_relationship transcript_exon2 on transcript_exon2.object_id = transcript.feature_id
join feature exon2 on transcript_exon2.subject_id = exon2.feature_id
join featureprop exon2_colour on exon2_colour.feature_id = exon2.feature_id
where transcript.type_id = 321 /*mRNA*/
and   exon1.type_id = 234 /*exon*/
and   exon1_colour.type_id = 26768 /*colour*/
and   exon2.type_id = 234 /*exon*/
and   exon2_colour.type_id = 26768 /*colour*/
and   exon1.feature_id < exon2.feature_id
and   exon1_colour.value <> exon2_colour.value
order by organism.common_name
;
