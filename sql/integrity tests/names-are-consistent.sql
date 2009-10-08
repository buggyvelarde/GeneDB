#Checks if feature names are consistent

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

