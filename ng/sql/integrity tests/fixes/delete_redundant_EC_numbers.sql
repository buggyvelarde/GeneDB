-- Deletes redundant GO terms (i.e., removes any references to general EC numbers when a term already has a more specific EC
-- number

delete from cvterm_dbxref where cvterm_dbxref_id in (
select general.cvterm_dbxref_id
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
and general.dbxref_id=general_dbxref.dbxref_id);