create view exon_temp as
select organism.common_name as organism
     , transcript.uniquename as transcript
     , exon.feature_id as exon_feature_id
     , exon.uniquename as exon
     , exon_colour.rank
     , exon_colour.value as exon_colour
from feature transcript
join organism using (organism_id)
join feature_relationship transcript_exon on transcript_exon.object_id = transcript.feature_id
join feature exon on transcript_exon.subject_id = exon.feature_id
join featureprop exon_colour on exon_colour.feature_id = exon.feature_id
where transcript.type_id = 321 /*mRNA*/
and   exon.type_id = 234 /*exon*/
and   exon_colour.type_id = 26768 /*colour*/
;
