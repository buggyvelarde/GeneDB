delete from feature
where type_id in (
  select cvterm.cvterm_id
  from cvterm join cv on cvterm.cv_id = cv.cv_id
  where cv.name = 'sequence'
  and cvterm.name = 'protein_match'
)
and not exists (
  select *
  from featureloc
  where featureloc.feature_id = feature.feature_id
)
and 2 > (
    select count(*)
    from feature_relationship
    where feature_relationship.object_id = feature.feature_id
    and feature_relationship.type_id in (
      select cvterm.cvterm_id
      from cvterm join cv on cvterm.cv_id = cv.cv_id
      where cv.name = 'sequence'
      and cvterm.name in ('orthologous_to', 'paralogous_to')
    )
 )
;
