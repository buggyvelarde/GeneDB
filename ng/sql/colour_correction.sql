/*
 If a transcript has mixed colours (some exons have one colour and some have another)
 then reset the colours of all exons to the colour of the corresponding polypeptide.
 */

begin;

create temporary table transcripts_with_mixed_colours as
select distinct transcript.feature_id
from feature transcript
join feature_relationship transcript_exon1 on transcript_exon1.object_id = transcript.feature_id
join feature exon1 on transcript_exon1.subject_id = exon1.feature_id
join featureprop exon1_colour on exon1_colour.feature_id = exon1.feature_id
join feature_relationship transcript_exon2 on transcript_exon2.object_id = transcript.feature_id
join feature exon2 on transcript_exon2.subject_id = exon2.feature_id
join featureprop exon2_colour on exon2_colour.feature_id = exon2.feature_id
where transcript.organism_id = 27 /*Pfalciparum*/
and   transcript.type_id = 321 /*mRNA*/
and   exon1.type_id = 234 /*exon*/
and   exon1_colour.type_id = 26768 /*colour*/
and   exon2.type_id = 234 /*exon*/
and   exon2_colour.type_id = 26768 /*colour*/
and   exon1.feature_id < exon2.feature_id
and   exon1_colour.value <> exon2_colour.value
;

create temporary table exon_colour_corrections as
select exon.feature_id as exon_feature_id
     , exon_colour.value as exon_current_colour
     , polypeptide_colour.value as polypeptide_colour
from transcripts_with_mixed_colours
join feature transcript using (feature_id)
join feature_relationship transcript_polypeptide on transcript_polypeptide.object_id = transcript.feature_id
join feature polypeptide on transcript_polypeptide.subject_id = polypeptide.feature_id
join featureprop polypeptide_colour on polypeptide_colour.feature_id = polypeptide.feature_id
join feature_relationship transcript_exon on transcript_exon.object_id = transcript.feature_id
join feature exon on transcript_exon.subject_id = exon.feature_id
join featureprop exon_colour on exon_colour.feature_id = exon.feature_id
where transcript.type_id = 321 /*mRNA*/
and   polypeptide.type_id = 191 /*polypeptide*/
and   polypeptide_colour.type_id = 26768 /*colour*/
and   exon.type_id = 234 /*exon*/
and   exon_colour.type_id = 26768 /*colour*/
and   exon_colour.value <> polypeptide_colour.value
;

update featureprop
set value = (
        select polypeptide_colour
        from exon_colour_corrections
        where exon_feature_id = featureprop.feature_id)
where exists (
        select polypeptide_colour
        from exon_colour_corrections
        where exon_feature_id = featureprop.feature_id)
and   type_id = 26768 /*colour*/
;

commit;