select organism.common_name as organism
     , transcript.uniquename as transcript
     , transcript_exon.rank
     , exon.uniquename as exon
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
where transcript.type_id = 321 /*mRNA*/
and   exon.type_id = 234 /*exon*/
and   polypeptide.type_id = 191 /*polypeptide*/
and   exon_colour.type_id = 26768 /*colour*/
and   polypeptide_colour.type_id = 26768 /*colour*/
and   product.cv_id = 25 /*genedb_products*/
and   exon_colour.value <> polypeptide_colour.value
;
