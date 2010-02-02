#The colour assigned to exon should match the colour of the polypeptide.
#The complication in correcting this is deciding which colour is correct. It was decided that, in general, the exon colour
#should be assigned to the polypeptide (1.2.2010)


select organism.common_name as organism
     , transcript.uniquename as transcript
     , transcript_exon.rank
     , exon.uniquename as exon
     , exon_colour.featureprop_id as exon_colour_featureprop_id
     , exon_colour.value as exon_colour
     , polypeptide.uniquename as polypeptide
     , polypeptide_colour.value as polypeptide_colour
     , product.name as product
from feature transcript
join organism using (organism_id)
join feature_relationship transcript_exon on transcript_exon.object_id = transcript.feature_id
join feature exon on transcript_exon.subject_id = exon.feature_id
join feature_relationship transcript_polypeptide on transcript_polypeptide.object_id = transcript.feature_id
join feature polypeptide on transcript_polypeptide.subject_id = polypeptide.feature_id
join featureprop exon_colour on exon_colour.feature_id = exon.feature_id
join featureprop polypeptide_colour on polypeptide_colour.feature_id = polypeptide.feature_id
left join feature_cvterm polypeptide_product on polypeptide_product.feature_id = polypeptide.feature_id
left join cvterm product on polypeptide_product.cvterm_id = product.cvterm_id
where transcript.type_id = (
                  select cvterm.cvterm_id
                  from cvterm join cv on cvterm.cv_id = cv.cv_id
                  where cv.name = 'sequence'
                  and cvterm.name = 'mRNA')
and   exon.type_id = (
                  select cvterm.cvterm_id
                  from cvterm join cv on cvterm.cv_id = cv.cv_id
                  where cv.name = 'sequence'
                  and cvterm.name = 'exon')
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
and   (product.cv_id is null or product.cv_id = (
                  select cv_id 
                  from cv 
                  where name='genedb_products'))
and exon_colour.rank = polypeptide_colour.rank
and   (exon_colour.value is null <> polypeptide_colour.value is null
        or exon_colour.value <> polypeptide_colour.value)
order by organism.common_name
;