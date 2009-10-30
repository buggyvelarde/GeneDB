create temporary table wrong_EC_numbers as
select accession 
from dbxref
where accession not like '%.%.%.%'
and db_id = (
             select db_id 
             from db
             where name='EC')
;

select dbxref.accession
     , wrong_EC_numbers.accession
from dbxref, wrong_EC_numbers 
where (dbxref.accession=wrong_EC_numbers.accession || '.-.-.-'
       or dbxref.accession=wrong_EC_numbers.accession || '.-.-'
       or dbxref.accession=wrong_EC_numbers.accession || '.-')
and dbxref.db_id=64;


select wrong_EC_numbers.accession
from wrong_EC_numbers
where not exists 
(select *
from dbxref
where (dbxref.accession=wrong_EC_numbers.accession || '.-.-.-'
       or dbxref.accession=wrong_EC_numbers.accession || '.-.-'
       or dbxref.accession=wrong_EC_numbers.accession || '.-')
and dbxref.db_id=64);