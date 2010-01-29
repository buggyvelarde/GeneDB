#Checks if the fmax values of any features are within the fmax value
#of the feature it is contained in. In principle there could be legitimate cases where this fails.
#If any such arise, they should be systematically excluded, e.g. by type.

select organism.common_name
     , supertype.name
     , super.uniquename
     , superloc.fmax as super_max
     , max(subloc.fmax) as max_sub_max
from feature super
join featureloc superloc using (feature_id)
join organism using (organism_id)
join cvterm supertype on supertype.cvterm_id = super.type_id
join feature_relationship sub_super
    on sub_super.object_id = super.feature_id
join cvterm reltype on sub_super.type_id = reltype.cvterm_id
join feature sub on sub_super.subject_id = sub.feature_id
join featureloc subloc on subloc.feature_id = sub.feature_id
where reltype.name = 'part_of'
and superloc.locgroup = subloc.locgroup
and superloc.srcfeature_id = subloc.srcfeature_id
and organism.organism_id=27
group by organism.common_name, supertype.name, super.uniquename, superloc.fmax
having superloc.fmax < max(subloc.fmax)
order by organism.common_name
;
