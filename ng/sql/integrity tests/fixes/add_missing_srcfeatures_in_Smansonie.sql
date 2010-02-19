begin;

create temporary table missing_BAC_ends as
select organism.common_name 
       , organism.organism_id
       , feature.feature_id
       , (split(substr(feature.uniquename, 7), '@'))[1] as possible_BAC_end
       , featureloc.featureloc_id as flid
from feature
join organism using (organism_id)
join featureloc using (feature_id)
where featureloc.srcfeature_id is null
;



update featureloc
set srcfeature_id=foo.srcfeature_id
from
        (select missing_BAC_ends.flid as featureloc_id, BAC.feature_id as srcfeature_id, BAC.uniquename
        from feature BAC, missing_BAC_ends
        where BAC.uniquename=missing_BAC_ends.possible_BAC_end || '.1'
        or BAC.uniquename=missing_BAC_ends.possible_BAC_end || '.2'
        and BAC.type_id=1087
        and BAC.organism_id=missing_BAC_ends.organism_id) as foo
where featureloc.featureloc_id=foo.featureloc_id;

commit;