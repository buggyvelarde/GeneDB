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
        where synonym.type_id = 26803
        and is_current = true
) feature_systematic_id using (feature_id)
left join (
        select feature_id, synonym.synonym_sgml as temporary_systematic_id
        from feature_synonym
        join synonym using (synonym_id)
        where synonym.type_id = 26804
        and is_current = true
) feature_temporary_systematic_id using (feature_id)
left join (
        select feature_id, synonym.synonym_sgml as primary_name
        from feature_synonym
        join synonym using (synonym_id)
        where synonym.type_id = 26801
        and is_current = true
) feature_primary_name using (feature_id)
;

/* Every gene or pseudogene has a systematic or temporary systematic ID */
select * from feature_name
where type_id in (
    792 /*gene*/,
    423 /*pseudogene*/
)
and systematic_id is null
and temporary_systematic_id is null
;

/* Systematic IDs are unique */
select a.*, b.*
from feature_name a, feature_name b
where a.feature_id < b.feature_id
and a.systematic_id = b.systematic_id
;

/* No feature has more than one (temporary) systematic ID */
select a.*, b.*
from feature_name a, feature_name b
where a.feature_id = b.feature_id
and coalesce(a.temporary_systematic_id, a.systematic_id, 'NONE') <> coalesce(b.temporary_systematic_id, b.systematic_id, 'NONE')
;

/* No feature has more than one primary name */
select a.*, b.*
from feature_name a, feature_name b
where a.feature_id = b.feature_id
and a.primary_name <> b.primary_name
;

/* systematic_id and temporary_systematic_id are mutually exclusive */
select * from feature_name
where systematic_id is not null
and temporary_systematic_id is not null
;

/* The name should be equal to the primary_name, if there is a primary_name */
select * from feature_name
where primary_name is not null
and name <> primary_name
;
