
#Look for features that are marked as top_level_seq but really should not be as they are located on something else

select organism.common_name
         , feature.feature_id
         , feature.uniquename
from feature
join featureprop using (feature_id)
join cvterm on featureprop.type_id = cvterm.cvterm_id
join organism using (organism_id)
join cv using (cv_id)
where featureprop.feature_id = feature.feature_id
and cv.name = 'genedb_misc'
and cvterm.name = 'top_level_seq'
and exists (
        select *
        from featureloc
        where featureloc.feature_id = feature.feature_id
        and featureloc.srcfeature_id is not null
    )
;
