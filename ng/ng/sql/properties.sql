select feature_type.name, property_type.name, count(*)
from feature
join cvterm feature_type on feature.type_id = feature_type.cvterm_id
join featureprop using (feature_id)
join cvterm property_type on featureprop.type_id = property_type.cvterm_id
--where property_type.name = 'colour'
group by feature_type.name, property_type.name
;
