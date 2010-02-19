#This test checks if there are any dbxrefs that still refer to PUBMED instead of PMID

select * from dbxref 
where db_id = (
               select db_id from db where name='PUBMED');