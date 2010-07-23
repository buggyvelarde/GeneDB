#This checks that cvterms are not annotated with redundant EC numbers. EC numbers are 4 digits long; each digit separated by a dot.
#At times the same cvterm can be annotated with a general EC number like 1.2.3.- and also with 1.2.3.4 which is more specific.
#These are redundant EC numbers and we shuould get rid of the more general number in such situations.


select specific.cvterm_id
     , cvterm.name
     , EC_numbers.specific as specific_EC_number
     , specific.dbxref_id
     , general_dbxref.accession as general_EC_number
     , general.dbxref_id
from cvterm_dbxref specific
   , cvterm_dbxref general 
   , dbxref general_dbxref
   , cvterm
   , (select dbxref_id
             , accession as specific
             , (split(accession, '.'))[1] || '.-.-.-' as general1
             , (split(accession, '.'))[1] || '.' || (split(accession, '.'))[2] || '.-.-' as general2
             , (split(accession, '.'))[1] || '.' || (split(accession, '.'))[2] || '.' || (split(accession, '.'))[3] || '.-' as general3
       from dbxref 
       where accession not like '%-%' 
       and db_id=(
                  select db_id from db
                  where name='EC')) as EC_numbers       
 where specific.dbxref_id = EC_numbers.dbxref_id
 and specific.cvterm_id=cvterm.cvterm_id
 and general.cvterm_id=specific.cvterm_id
 and general.dbxref_id in (
                           select dbxref_id
                           from dbxref
                           where dbxref.accession=EC_numbers.general1 
                                 or dbxref.accession=EC_numbers.general2
                                 or dbxref.accession=EC_numbers.general3 )
and general.dbxref_id=general_dbxref.dbxref_id
order by specific.cvterm_id;
 
 
 
                
