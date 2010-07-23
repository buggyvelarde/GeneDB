select organism.common_name as organism_common_name
     , feature_type.name  as feature_type
     , feature.uniquename as feature_uniquename
     , colour1.featureprop_id as colour1_id
     , colour1.value      as colour1
     , colour2.featureprop_id as colour2_id
     , colour2.value      as colour2
from feature
join organism using (organism_id)
join cvterm feature_type on feature.type_id = feature_type.cvterm_id
join featureprop colour1 using (feature_id)
join featureprop colour2 using (feature_id)
where colour1.featureprop_id < colour2.featureprop_id
and colour1.type_id = 26768 and colour2.type_id = 26768
;


select organism.common_name as organism_common_name
     , feature_type.name  as feature_type
     , feature.feature_id
     , feature.uniquename as feature_uniquename
     , colour.value      as colour
from feature
join organism using (organism_id)
join cvterm feature_type on feature.type_id = feature_type.cvterm_id
join featureprop colour using (feature_id)
where colour.type_id = 26768
limit 100
;
