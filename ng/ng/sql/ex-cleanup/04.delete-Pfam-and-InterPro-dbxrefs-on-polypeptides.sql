delete from feature_dbxref
using feature polypeptide
    , dbxref
      join db using (db_id)
where feature_dbxref.feature_id = polypeptide.feature_id
and   feature_dbxref.dbxref_id = dbxref.dbxref_id
and polypeptide.type_id = 191 /*polypeptide*/
and db.name in ('Pfam', 'InterPro')
;
