#Checks that all pubmed ids have dbxrefs. A dbxref links the pubmedid to the database it came from

select * from pub
where uniquename like 'PMID:%'
and not exists (
    select * from pub_dbxref where pub.pub_id = pub_dbxref.pub_id
);
