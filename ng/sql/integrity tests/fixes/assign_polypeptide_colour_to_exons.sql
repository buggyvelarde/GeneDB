-- This bit of SQL assigns the polypeptide colour to the exon when there is a mismatch in the colours
-- We do this now for the Leishmanias and Tryps only (as requested by Matt Rogers). 2.2.2010


begin;

create temporary table exon_colours as
select distinct exon_colour.featureprop_id 
     , polypeptide_colour.value as polypeptide_colour
from feature transcript
join feature_relationship transcript_exon on transcript_exon.object_id = transcript.feature_id
join feature exon on transcript_exon.subject_id = exon.feature_id
join feature_relationship transcript_polypeptide on transcript_polypeptide.object_id = transcript.feature_id
join feature polypeptide on transcript_polypeptide.subject_id = polypeptide.feature_id
join featureprop exon_colour on exon_colour.feature_id = exon.feature_id
join featureprop polypeptide_colour on polypeptide_colour.feature_id = polypeptide.feature_id
where transcript.type_id in (
                  select cvterm_id 
                  from cvterm
                  where name in ('snRNA', 'tRNA', 'transcript', 'pseudogenic_transcript', 'rRNA', 'mRNA'))
and   exon.type_id in (
                  select cvterm_id
                  from cvterm 
                  where cvterm.name in ('exon', 'pseudogenic_exon'))
and   polypeptide.type_id = (
                  select cvterm.cvterm_id
                  from cvterm join cv on cvterm.cv_id = cv.cv_id
                  where cv.name = 'sequence'
                  and cvterm.name = 'polypeptide')
and   exon_colour.type_id = (
                  select cvterm.cvterm_id
                  from cvterm join cv on cvterm.cv_id = cv.cv_id
                  where cv.name = 'genedb_misc'
                  and cvterm.name = 'colour')
and   polypeptide_colour.type_id = (
                  select cvterm.cvterm_id
                  from cvterm join cv on cvterm.cv_id = cv.cv_id
                  where cv.name = 'genedb_misc'
                  and cvterm.name = 'colour')
and exon_colour.rank = polypeptide_colour.rank
and   (exon_colour.value is null <> polypeptide_colour.value is null
        or exon_colour.value <> polypeptide_colour.value)
and transcript.organism_id in (14, 15, 16, 22, 19)
;


update featureprop
set value = (select polypeptide_colour
             from exon_colours
             where featureprop.featureprop_id = exon_colours.featureprop_id)
where featureprop_id in (select featureprop_id 
                      from exon_colours);
                      
drop table exon_colours;
                      
                      
commit;


     
