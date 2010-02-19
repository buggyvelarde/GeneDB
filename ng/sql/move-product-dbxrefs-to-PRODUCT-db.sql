begin;

create temporary table dbxref_clash as
select product_xref.dbxref_id as product_dbxref_id
     , null_xref.dbxref_id    as null_dbxref_id
from dbxref product_xref join db product_db using (db_id)
   , dbxref null_xref    join db null_db    using (db_id)
where product_db.name = 'PRODUCT'
and   null_db.name = 'null'
and product_xref.accession = null_xref.accession
;

create temporary table cvterm_clash as
select product_cvterm.cvterm_id as product_cvterm_id
     , null_cvterm.cvterm_id    as null_cvterm_id
from cvterm product_cvterm, cvterm null_cvterm
, dbxref_clash
where product_cvterm.dbxref_id = dbxref_clash.product_dbxref_id
and null_cvterm.dbxref_id = dbxref_clash.null_dbxref_id
;

update feature_cvterm
set cvterm_id = (
    select product_cvterm_id
    from cvterm_clash
    where feature_cvterm.cvterm_id = cvterm_clash.null_cvterm_id)
where cvterm_id in (select null_cvterm_id from cvterm_clash)
;

delete from cvterm
    where cvterm_id in (
        select null_cvterm_id from cvterm_clash
    );

update cvterm
    set dbxref_id = (
        select product_dbxref_id from dbxref_clash
        where cvterm.dbxref_id = dbxref_clash.null_dbxref_id)
    where dbxref_id in (
        select null_dbxref_id from dbxref_clash
    );

delete from dbxref
    where dbxref_id in (
        select null_dbxref_id from dbxref_clash
    );

update dbxref
    set db_id = (select db_id from db where name = 'PRODUCT')
    from cvterm join cv using (cv_id)
    where cvterm.dbxref_id = dbxref.dbxref_id
    and cv.name = 'genedb_products'
;

-- commit;