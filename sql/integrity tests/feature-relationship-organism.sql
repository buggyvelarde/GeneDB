#Checks that feature relationships with organisms are in order 

select *
from feature_relationship
join feature subject_feature on subject_feature.feature_id = feature_relationship.subject_id
join feature object_feature on object_feature.feature_id = feature_relationship.object_id
join cvterm reltype on feature_relationship.type_id = reltype.cvterm_id
where subject_feature.organism_id <> object_feature.organism_id
and   reltype.name not in ('orthologous_to', 'paralogous_to')
;
