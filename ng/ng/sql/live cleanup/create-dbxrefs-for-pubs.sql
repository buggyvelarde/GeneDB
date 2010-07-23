begin;

insert into pub_dbxref (pub_id, dbxref_id) (
    select pub.pub_id, dbxref.dbxref_id
    from pub, dbxref join db using (db_id)
    where db.name = 'PMID'
    and pub.uniquename = 'PMID:' || dbxref.accession
    and not exists (
        select 8 from pub_dbxref
        where pub_dbxref.pub_id = pub.pub_id
        and pub_dbxref.dbxref_id = dbxref.dbxref_id
    )
);

create temporary table missing_pmid_dbxrefs on commit drop as
select pub_id, substr(uniquename, 6) as accession
from pub
where pub.uniquename like 'PMID:%'
and not exists (
    select 8 from pub_dbxref
    where pub_dbxref.pub_id = pub.pub_id
);

insert into dbxref (db_id, accession) (
    select db.db_id, missing_pmid_dbxrefs.accession
    from missing_pmid_dbxrefs, db
    where db.name = 'PMID'
);

insert into pub_dbxref (pub_id, dbxref_id) (
    select pub.pub_id, dbxref.dbxref_id
    from pub, dbxref join db using (db_id)
    where db.name = 'PMID'
    and pub.uniquename = 'PMID:' || dbxref.accession
    and not exists (
        select 8 from pub_dbxref
        where pub_dbxref.pub_id = pub.pub_id
        and pub_dbxref.dbxref_id = dbxref.dbxref_id
    )
);

commit;
