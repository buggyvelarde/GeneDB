/*
 Remove top-level feature marker from BAC_ends and ESTs
*/
delete from featureprop where featureprop_id in (
    select featureprop.featureprop_id
    from feature
    join organism using (organism_id)
    join featureprop using (feature_id)
    join cvterm as cvterm1 on featureprop.type_id = cvterm1.cvterm_id
    join cvterm as cvterm2 on feature.type_id = cvterm2.cvterm_id
    where cvterm2.name in ('BAC_end', 'EST')
    and cvterm1.name = 'top_level_seq'
);
