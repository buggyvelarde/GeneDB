select organism.common_name
     , region.uniquename
     , regionloc.fmin
     , regionloc.fmax
     , super.uniquename
     , regionproptype.name
     , regionprop.value
     , big.uniquename
from feature region
join organism using (organism_id)
left join featureloc regionloc using (feature_id)
left join feature super on regionloc.srcfeature_id = super.feature_id
left join featureprop regionprop on regionprop.feature_id = region.feature_id
left join cvterm regionproptype on regionprop.type_id = regionproptype.cvterm_id
left join feature_relationship region_big on region.feature_id = region_big.subject_id
left join feature big on region_big.object_id = big.feature_id
where region.type_id = 87 /*region*/
order by region.feature_id
;
