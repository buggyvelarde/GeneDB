select * from pub
where uniquename like 'PMID:%'
and not exists (
    select 8 from pub_dbxref where pub.pub_id = pub_dbxref.pub_id
);
