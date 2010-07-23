-- The features_shrinkwrap_fmax test uncovered that there a lot of (over 1000) Tbrucei EST matches on the -ve strand that were one space shorter than they should be.
-- The comprising match parts always span to one space longer than the EST_match. The SQL below was considered easier to do than to reload all the EST
-- features from the vulgar file.

-- We cannot find an obvious problem with the Vulgar loader that would have done this. It is now just a matter of watching out for how this may have
-- happened.

begin;

create temporary table short_ESTs as
select organism.common_name
     , super.feature_id as super
     , super.uniquename
     , superloc.featureloc_id as super_loc
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
and superloc.strand = subloc.strand
and organism.common_name = 'Tbruceibrucei927'
and supertype.name = 'EST_match'
group by organism.common_name, super, super.uniquename, super_loc, super_max
having superloc.fmax < max(subloc.fmax)
order by organism.common_name
;


-- Change the featureloc
update featureloc
set fmax = (select max_sub_max
            from short_ESTs
            where featureloc_id = super_loc)
where featureloc_id in (select super_loc from short_ESTs);


-- Change the name
update feature
set uniquename = 
                (select substr(uniquename, 0, length(uniquename)-length(cast (super_max as varchar))) || max_sub_max || ')'
                 from short_ESTS
                 where super=feature_id)
where feature_id in 
                (select super 
                from short_ESTs);


drop table short_ESTs;

commit;
