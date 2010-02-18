#Checks if there are features with more than one colour associated with them that have the same rank

select feature.feature_id
     , feature.uniquename
     , fp1.featureprop_id as fp1_featureprop_id
     , fp1.value          as colour1
     , fp1.rank           as colour1_rank      
     , fp2.featureprop_id as fp2_featureprop_id
     , fp2.value          as colour2
     , fp2.rank           as colour2_rank
from feature
join featureprop fp1 using (feature_id)
join featureprop fp2 using (feature_id)
where fp1.featureprop_id < fp2.featureprop_id
and fp1.type_id = fp2.type_id
and fp2.type_id in (
    select cvterm.cvterm_id
    from cvterm join cv using (cv_id)
    where cv.name = 'genedb_misc'
    and cvterm.name = 'colour'
)
;
