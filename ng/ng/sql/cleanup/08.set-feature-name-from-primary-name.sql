begin;

/* View defined in names-are-consistent.sql */
create or replace temporary view feature_name as
select feature.feature_id
     , feature.type_id
     , feature.name
     , feature.uniquename
     , feature_systematic_id.systematic_id
     , feature_temporary_systematic_id.temporary_systematic_id
     , feature_primary_name.primary_name
from feature
left join (
        select feature_id, synonym.synonym_sgml as systematic_id
        from feature_synonym
        join synonym using (synonym_id)
        where synonym.type_id = 26803 /*systematic_id*/
        and is_current = true
) feature_systematic_id using (feature_id)
left join (
        select feature_id, synonym.synonym_sgml as temporary_systematic_id
        from feature_synonym
        join synonym using (synonym_id)
        where synonym.type_id = 26804 /*temporary_systematic_id*/
        and is_current = true
) feature_temporary_systematic_id using (feature_id)
left join (
        select feature_id, synonym.synonym_sgml as primary_name
        from feature_synonym
        join synonym using (synonym_id)
        where synonym.type_id = 26801 /*primary_name*/
        and is_current = true
) feature_primary_name using (feature_id)
;

update feature
set name = primary_name
from feature_name where feature.feature_id = feature_name.feature_id
and primary_name is not null and (
    feature.name is null
        or
    feature.name <> primary_name
)
;

commit;
