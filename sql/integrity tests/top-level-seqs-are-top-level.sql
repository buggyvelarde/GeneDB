
# Look for features that:
#  - have a sequence
#  - are a source feature
#  - do not have a source feature
#  - are not marked as top_level_seq


select organism.common_name
     , feature.feature_id
     , feature.uniquename
from feature
join organism using (organism_id)
where feature.residues is not null and length(feature.residues) > 0
and exists (
    select *
    from featureloc
    where featureloc.srcfeature_id = feature.feature_id
)
and not exists (
    select *
    from featureloc
    where featureloc.feature_id = feature.feature_id
    and featureloc.srcfeature_id is not null
)
and not exists (
    select *
    from featureprop
    join cvterm on featureprop.type_id = cvterm.cvterm_id
    join cv using (cv_id)
    where featureprop.feature_id = feature.feature_id
    and cv.name = 'genedb_misc'
    and cvterm.name = 'top_level_seq'
)
;
