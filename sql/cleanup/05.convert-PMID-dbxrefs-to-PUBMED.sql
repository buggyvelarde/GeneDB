update dbxref
set db_id = (select db_id from db where name = 'PUBMED')
from db
where dbxref.db_id = db.db_id
and db.name = 'PMID'
;
