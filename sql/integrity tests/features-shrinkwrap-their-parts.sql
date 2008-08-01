/*
In principle there could be legitimate cases where this fails.
If any such arise, they should be systematically excluded, e.g. by type.
*/

select super.uniquename
     , superloc.fmin as super_min
     , min(subloc.fmin) as min_sub_min
from feature super
join featureloc superloc using (feature_id)
join feature_relationship sub_super
    on sub_super.object_id = super.feature_id
join cvterm reltype on sub_super.type_id = reltype.cvterm_id
join feature sub on sub_super.subject_id = sub.feature_id
join featureloc subloc on subloc.feature_id = sub.feature_id
where reltype.name = 'part_of'
group by super.uniquename, superloc.fmin
having superloc.fmin <> min(subloc.fmin)
;


select super.uniquename
     , superloc.fmax as super_max
     , max(subloc.fmax) as max_sub_max
from feature super
join featureloc superloc using (feature_id)
join feature_relationship sub_super
    on sub_super.object_id = super.feature_id
join cvterm reltype on sub_super.type_id = reltype.cvterm_id
join feature sub on sub_super.subject_id = sub.feature_id
join featureloc subloc on subloc.feature_id = sub.feature_id
where reltype.name = 'part_of'
group by super.uniquename, superloc.fmax
having superloc.fmax <> max(subloc.fmax)
;
