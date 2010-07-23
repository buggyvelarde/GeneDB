delete from synonym where synonym_id in (
  select synonym_id from synonym
  except (
      select synonym_id from feature_synonym
      union
      select synonym_id from library_synonym
  )
);
