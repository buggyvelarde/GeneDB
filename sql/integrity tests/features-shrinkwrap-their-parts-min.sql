#In principle there could be legitimate cases where this fails.
#If any such arise, they should be systematically excluded, e.g. by type.

select organism.common_name
     , super.uniquename
     , super.type_id
     , superloc.fmin as super_min
     , min(subloc.fmin) as min_sub_min
from feature super
join organism using (organism_id)
join featureloc superloc using (feature_id)
join cvterm supertype on supertype.cvterm_id = super.type_id
join feature_relationship sub_super
    on sub_super.object_id = super.feature_id
join cvterm reltype on sub_super.type_id = reltype.cvterm_id
join feature sub on sub_super.subject_id = sub.feature_id
join featureloc subloc on subloc.feature_id = sub.feature_id
where reltype.name = 'part_of'
and superloc.locgroup = subloc.locgroup
and superloc.srcfeature_id = subloc.srcfeature_id
group by organism.common_name, super.uniquename, super.type_id, superloc.fmin
having superloc.fmin > min(subloc.fmin)
order by organism.common_name
;

