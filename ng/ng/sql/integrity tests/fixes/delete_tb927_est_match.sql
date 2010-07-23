-- delete the EST_match_parts

delete from feature
where feature_id in (
      select subject_id 
      from feature_relationship
      join feature on feature_id = object_id
      where feature.type_id = 756
      and organism_id = 19
      and feature_relationship.type_id = 42
      )
and type_id = 126;

-- delete the EST_matches
delete from feature
where type_id = 756
and organism_id = 19;