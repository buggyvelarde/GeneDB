begin;

create temporary table exon_colours as
select exon_colour.featureprop_id as exon_colour_featureprop_id
     , polypeptide_colour.value as polypeptide_colour
from feature transcript
join organism using (organism_id)
join feature_relationship transcript_exon on transcript_exon.object_id = transcript.feature_id
join feature exon on transcript_exon.subject_id = exon.feature_id
join feature_relationship transcript_polypeptide on transcript_polypeptide.object_id = transcript.feature_id
join feature polypeptide on transcript_polypeptide.subject_id = polypeptide.feature_id
join featureprop exon_colour on exon_colour.feature_id = exon.feature_id
join featureprop polypeptide_colour on polypeptide_colour.feature_id = polypeptide.feature_id
where transcript.type_id = 321 /*mRNA*/
and   exon.type_id = 234 /*exon*/
and   polypeptide.type_id = 191 /*polypeptide*/
and   exon_colour.type_id = 26768 /*colour*/
and   polypeptide_colour.type_id = 26768 /*colour*/
and   (exon_colour.value is null <> polypeptide_colour.value is null
        or exon_colour.value <> polypeptide_colour.value)
;

update featureprop
set value = exon_colours.polypeptide_colour
from exon_colours
where exon_colours.exon_colour_featureprop_id = featureprop.featureprop_id
;

