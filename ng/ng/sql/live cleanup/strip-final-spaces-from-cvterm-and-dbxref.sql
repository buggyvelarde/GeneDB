begin;

create temporary table cvterm_spaces on commit drop as
select with_space.cvterm_id    as with_space_id
     , without_space.cvterm_id as without_space_id
from cvterm with_space
   , cvterm without_space
where with_space.cv_id = without_space.cv_id
and lower(with_space.name) = lower(without_space.name) || ' '
;

update feature_cvterm
set cvterm_id = (
    select without_space_id from cvterm_spaces
    where cvterm_spaces.with_space_id = feature_cvterm.cvterm_id
)
where cvterm_id in (
    select with_space_id from cvterm_spaces
);

delete from cvterm
using cv
where cv.cv_id = cvterm.cv_id
and cv.name = 'genedb_products'
and not exists (
    select 8 from feature_cvterm
    where feature_cvterm.cvterm_id = cvterm.cvterm_id
);

update cvterm
    set name = regexp_replace(name, E'\\s+$', '')
    where name like '% ';

update cvterm
    set definition = regexp_replace(definition, E'\\s+$', '')
    where definition like '% ';

select * from cvterm
where name like '% ' or definition like '% ';

\i remove-orphaned-dbxrefs.sql

create temporary table dbxref_spaces on commit drop as
select with_space.dbxref_id as with_space_id
     , without_space.dbxref_id as without_space_id
from dbxref with_space, dbxref without_space
where with_space.db_id = without_space.db_id
and   with_space.accession = without_space.accession || ' '
;

update cvterm as referent
set dbxref_id = (select without_space_id from dbxref_spaces
    where dbxref_spaces.with_space_id = referent.dbxref_id)
where dbxref_id in (select with_space_id from dbxref_spaces);

update cvterm_dbxref as referent
set dbxref_id = (select without_space_id from dbxref_spaces
    where dbxref_spaces.with_space_id = referent.dbxref_id)
where dbxref_id in (select with_space_id from dbxref_spaces);

update pub_dbxref as referent
set dbxref_id = (select without_space_id from dbxref_spaces
    where dbxref_spaces.with_space_id = referent.dbxref_id)
where dbxref_id in (select with_space_id from dbxref_spaces);

update feature as referent
set dbxref_id = (select without_space_id from dbxref_spaces
    where dbxref_spaces.with_space_id = referent.dbxref_id)
where dbxref_id in (select with_space_id from dbxref_spaces);

update feature_dbxref as referent
set dbxref_id = (select without_space_id from dbxref_spaces
    where dbxref_spaces.with_space_id = referent.dbxref_id)
where dbxref_id in (select with_space_id from dbxref_spaces);

update organism_dbxref as referent
set dbxref_id = (select without_space_id from dbxref_spaces
    where dbxref_spaces.with_space_id = referent.dbxref_id)
where dbxref_id in (select with_space_id from dbxref_spaces);

update feature_cvterm_dbxref as referent
set dbxref_id = (select without_space_id from dbxref_spaces
    where dbxref_spaces.with_space_id = referent.dbxref_id)
where dbxref_id in (select with_space_id from dbxref_spaces);

update phylotree as referent
set dbxref_id = (select without_space_id from dbxref_spaces
    where dbxref_spaces.with_space_id = referent.dbxref_id)
where dbxref_id in (select with_space_id from dbxref_spaces);

delete from dbxref where dbxref_id in (
    select with_space_id from dbxref_spaces
);

update dbxref set accession = regexp_replace(accession, E'\\s+$', '')
    where accession like '% ';
update dbxref set description = regexp_replace(description, E'\\s+$', '')
    where description like '% ';


commit;
