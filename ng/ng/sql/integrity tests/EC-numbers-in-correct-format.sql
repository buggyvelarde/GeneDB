#This test checks if the EC numbers all the have the expected format (i.e., number-number-number-number)
#In many cases, the correct version will be in the database. In that case, the wrong dbxref needs to be deleted
#and all annotations pointed at the right dbxref. e.g. '4' (wrong) and '4.-.-.-' (right)

select * 
from dbxref
where accession not like '%.%.%.%'
and db_id = (
             select db_id 
             from db
             where name='EC')
;