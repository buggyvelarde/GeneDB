begin;

update dbxref
set db_id = (select db_id from db where name = 'PUBMED')
from db
where dbxref.db_id = db.db_id
and db.name = 'PMID'
;

create temporary table pmid_pubmed as
    select pmid.pub_id   as pmid_pub_id
         , pubmed.pub_id as pubmed_pub_id
    from pub pmid
       , pub pubmed
    where pmid.uniquename like 'PMID:%'
    and pubmed.uniquename like 'PUBMED:%'
    and substr(pmid.uniquename, 6) = substr(pubmed.uniquename, 8)
;

update feature_cvterm
    set pub_id = (select pubmed_pub_id from pmid_pubmed where pmid_pub_id = feature_cvterm.pub_id)
    where pub_id in (select pmid_pub_id from pmid_pubmed)
;

update feature_pub
    set pub_id = (select pubmed_pub_id from pmid_pubmed where pmid_pub_id = feature_pub.pub_id)
    where pub_id in (select pmid_pub_id from pmid_pubmed)
;

delete from pub
    where pub_id in (select pmid_pub_id from pmid_pubmed);


update pub
set uniquename = 'PUBMED:' || substr(uniquename, 6)
where uniquename like 'PMID:%'
;

create temporary table pubmed_accession as
    select coalesce(pub_id, nextval('pub_pub_id_seq'::regclass)) as pub_id
         , (pub_id is null) as pub_is_new
         , accession
    from dbxref join db using (db_id)
    left join pub on pub.uniquename = 'PUBMED:' || accession
    where db.name = 'PUBMED'
;

insert into pub (pub_id, uniquename, type_id) (
    select pub_id, 'PUBMED:' || accession, cvterm.cvterm_id
    from pubmed_accession
        , cvterm join cv using (cv_id)
        where cv.name = 'genedb_literature'
          and cvterm.name = 'unfetched'
          and pubmed_accession.pub_is_new
)
;
  
create temporary table feature_dbxref_for_pub as
    select pub_id
         , feature_dbxref_id
         , feature_id
         , dbxref_id
         , accession
    from feature_dbxref
    join dbxref using (dbxref_id)
    join db using (db_id)
    join pubmed_accession using (accession)
    where db.name = 'PUBMED'
;

delete from feature_dbxref
    where feature_dbxref_id in (
        select feature_dbxref_id
        from feature_dbxref_for_pub
    )
;

delete from pub_dbxref where exists (
    select 8
    from feature_dbxref_for_pub
    where pub_id  = pub_dbxref.pub_id
    and dbxref_id = pub_dbxref.dbxref_id
    )
;

insert into pub_dbxref (pub_id, dbxref_id)
    (select distinct pub_id, dbxref_id from feature_dbxref_for_pub)
;

delete from feature_pub where exists (
    select 8
    from feature_dbxref_for_pub
    where pub_id  = feature_pub.pub_id
    and feature_id = feature_pub.feature_id
    )
;

insert into feature_pub (feature_id, pub_id)
    (select feature_id, pub_id from feature_dbxref_for_pub)
;
