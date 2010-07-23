begin;

create temporary table new_top_level as
    select feature.feature_id
    from feature
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

insert into featureprop (feature_id, type_id, value) (
    select new_top_level.feature_id
         , cvterm.cvterm_id
         , 'true'
     from new_top_level
        , cvterm join cv using (cv_id)
     where cv.name = 'genedb_misc'
     and   cvterm.name = 'top_level_seq'
);
