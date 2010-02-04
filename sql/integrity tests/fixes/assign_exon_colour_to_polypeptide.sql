-- This bit of SQL assigns the exon colour to the polypeptide when there is a mismatch in the colours


begin;

create temporary table pep_colours as
select distinct polypeptide_colour.featureprop_id 
     , exon_colour.value as exon_colour
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
;


update featureprop
set value = (select exon_colour
             from pep_colours
             where featureprop.featureprop_id = pep_colours.featureprop_id)
where featureprop_id in (select featureprop_id 
                      from pep_colours);
                      
                      
commit;


     
