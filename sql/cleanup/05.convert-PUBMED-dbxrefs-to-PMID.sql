begin;

update dbxref
set db_id = (select db_id from db where name = 'PMID')
from db
where dbxref.db_id = db.db_id
and db.name = 'PUBMED'
and accession not in (
                select accession from dbxref where db_id=46 )
;

create temporary table pmid_pubmed on commit drop as
    select pmid.pub_id   as pmid_pub_id
         , pubmed.pub_id as pubmed_pub_id
    from pub pmid
       , pub pubmed
    where pmid.uniquename like 'PMID:%'
    and pubmed.uniquename ilike 'PUBMED:%'
    and substr(pmid.uniquename, 6) = substr(pubmed.uniquename, 8)
;

update feature_cvterm
    set pub_id = (select pmid_pub_id from pmid_pubmed where pubmed_pub_id = feature_cvterm.pub_id)
    where pub_id in (select pubmed_pub_id from pmid_pubmed)
;

update feature_pub
    set pub_id = (select pmid_pub_id from pmid_pubmed where pubmed_pub_id = feature_pub.pub_id)
    where pub_id in (select pubmed_pub_id from pmid_pubmed)
;

delete from pub
    where pub_id in (select pubmed_pub_id from pmid_pubmed);


update pub
set uniquename = 'PMID:' || substr(uniquename, 6)
where uniquename ilike 'PUBMED:%'
;

create temporary table pmid_accession on commit drop as
    select coalesce(pub_id, nextval('pub_pub_id_seq'::regclass)) as pub_id
         , (pub_id is null) as pub_is_new
         , accession
    from dbxref join db using (db_id)
    left join pub on pub.uniquename = 'PMID:' || accession
    where db.name = 'PMID'
;

insert into pub (pub_id, uniquename, type_id) (
    select pub_id, 'PMID:' || accession, cvterm.cvterm_id
    from pmid_accession
        , cvterm join cv using (cv_id)
        where cv.name = 'genedb_literature'
          and cvterm.name = 'unfetched'
          and pmid_accession.pub_is_new
)
;
  
create temporary table feature_dbxref_for_pub on commit drop as
    select pub_id
         , feature_dbxref_id
         , feature_id
         , dbxref_id
         , accession
    from feature_dbxref
    join dbxref using (dbxref_id)
    join db using (db_id)
    join pmid_accession using (accession)
    where db.name = 'PMID'
;

delete from feature_dbxref
    where feature_dbxref_id in (
        select feature_dbxref_id
        from feature_dbxref_for_pub
    )
;

insert into pub_dbxref (pub_id, dbxref_id)
    (select distinct pub_id, dbxref_id from feature_dbxref_for_pub
        where not exists (
            select 8
            from pub_dbxref
            where pub_dbxref.pub_id = feature_dbxref_for_pub.pub_id
            and pub_dbxref.dbxref_id = feature_dbxref_for_pub.dbxref_id
    ))
;

insert into feature_pub (feature_id, pub_id)
    (select feature_id, pub_id from feature_dbxref_for_pub
        where not exists (
            select 8
            from feature_pub
            where feature_pub.feature_id = feature_dbxref_for_pub.feature_id
            and feature_pub.pub_id = feature_dbxref_for_pub.pub_id
    ))
;

commit;
