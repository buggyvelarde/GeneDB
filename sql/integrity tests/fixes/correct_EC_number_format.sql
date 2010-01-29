create temporary table wrong_EC_numbers as
select dbxref_id,
       accession
from dbxref
where accession not like '%.%.%.%'
and db_id = (
             select db_id 
             from db
             where name='EC')
;

update dbxref 
set accession = wrong_EC_numbers.accession || '.-'
from wrong_EC_numbers
where wrong_EC_numbers.accession like '%.%.%'
and dbxref.dbxref_id=wrong_EC_numbers.dbxref_id;

update dbxref 
set accession = wrong_EC_numbers.accession || '.-.-'
from wrong_EC_numbers
where wrong_EC_numbers.accession like '%.%'
and dbxref.dbxref_id=wrong_EC_numbers.dbxref_id;