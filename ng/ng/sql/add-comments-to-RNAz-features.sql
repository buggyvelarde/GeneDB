insert into featureprop (feature_id, type_id, value, rank) (
        select feature.feature_id, cvterm.cvterm_id, 'non-coding RNA', 1
        from feature
           , cvterm join cv using (cv_id)
        where feature.uniquename like 'RNAz%'
        and cvterm.name = 'comment'
        and cv.name = 'feature_property'
);
