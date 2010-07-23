
/* get all teh wrong ids */
create temporary table wrong_EC_numbers as
select accession
       , dbxref_id
from dbxref
where accession not like '%.%.%.%'
and db_id = (
             select db_id 
             from db
             where name='EC')
;

/* find the right replacements */
create temporary table corrections as
select dbxref.dbxref_id as rightid
     , wrong_EC_numbers.dbxref_id as wrongid
from dbxref, wrong_EC_numbers 
where (dbxref.accession=wrong_EC_numbers.accession || '.-.-.-'
       or dbxref.accession=wrong_EC_numbers.accession || '.-.-'
       or dbxref.accession=wrong_EC_numbers.accession || '.-')
and dbxref.db_id=64;

/* replace ones that can be replaced */
update cvterm_dbxref set dbxref_id=corrections.rightid 
from  corrections
where cvterm_dbxref.dbxref_id=corrections.wrongid
and cvterm_dbxref.cvterm_id not in (select cvterm_id from cvterm_dbxref where dbxref_id=corrections.rightid);


/* look for things that still point to this list and delete */
delete from cvterm_dbxref where cvterm_dbxref_id in (select cvterm_dbxref.cvterm_dbxref_id from cvterm_dbxref, corrections
where dbxref_id in (select wrongid from corrections)
and cvterm_id in (select cvterm_id from cvterm_dbxref where dbxref_id=corrections.rightid));

/* delete the dbxrefs that are no longer pointed to by anything */

delete from dbxref where dbxref_id in (select dbxref_id
from wrong_EC_numbers 
where not exists (select * from cvterm_dbxref where dbxref_id=wrong_EC_numbers.dbxref_id)
and not exists (select * from feature_dbxref where dbxref_id=wrong_EC_numbers.dbxref_id));

