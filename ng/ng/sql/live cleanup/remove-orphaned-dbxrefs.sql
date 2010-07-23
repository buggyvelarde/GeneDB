delete from dbxref where dbxref_id in (
  select dbxref_id from dbxref join db using (db_id)
  where db.name in ('PRODUCT', 'PUBMED', 'PMID', 'null')
  except (
    select dbxref_id from cvterm
  union all
    select dbxref_id from cvterm_dbxref
  union all
    select dbxref_id from pub_dbxref
  union all
    select dbxref_id from feature where dbxref_id is not null
  union all
    select dbxref_id from feature_dbxref
  union all
    select dbxref_id from organism_dbxref
  union all
    select dbxref_id from feature_cvterm_dbxref
  union all
    select dbxref_id from phylotree
  )
);
