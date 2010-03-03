# This is a check to see if the colour fields are all filled in

select organism.common_name,
       feature.feature_id as feature_id,
       feature.uniquename as uniquename,
       featureprop.type_id,
       featureprop.value
from feature
join featureprop using (feature_id)
join organism on feature.organism_id = organism.organism_id
where featureprop.type_id = (
                            select cvterm_id
                            from cvterm
                            where name='colour'
                            )
and featureprop.value is null
order by organism.common_name;