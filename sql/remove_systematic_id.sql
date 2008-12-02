/* Take a copy of all systematic_id and temporary_systematic_id synonyms,
   Just In Case.
 */

create table _systematic_id_synonyms as
select feature.feature_id
     , feature.uniquename
     , synonym_type.cvterm_id as synonym_type_cvterm_id
     , synonym_type.name as synonym_type_name
     , synonym.name as synonym_name
     , synonym.synonym_sgml
from feature
join feature_synonym using (feature_id)
join synonym using (synonym_id)
join cvterm synonym_type on synonym_type.cvterm_id = synonym.type_id
where synonym_type.cvterm_id in (
    select cvterm_id
    from cvterm
    join cv using (cv_id)
    where cv.name = 'genedb_synonym_type'
    and cvterm.name in ('systematic_id', 'temporary_systematic_id')
)
;

comment on table _systematic_id_synonyms is
'These synonyms have been removed from the database, since they duplicate
the information in the uniquename. They are kept here temporarily in case
it turns out that any important information has been thereby lost.

Once a suitable period of time has elapsed, this table may be removed.

-- rh11, 2008-12-01'
;

begin;

/* We want to change all temporary_systematic_id synonyms to
   type systematic_id. However, in some cases there is both
   a systematic_id and a temporary_systematic_id synonym with
   the same name, so a simple update causes a constraint
   violation. */

create temporary table clashing_systematic_ids as
select systematic_id.name
     , systematic_id.synonym_id as systematic_id_synonym_id
     , temporary_systematic_id.synonym_id as temporary_systematic_id_synonym_id
from synonym systematic_id
   , synonym temporary_systematic_id
where systematic_id.name = temporary_systematic_id.name
and systematic_id.type_id in (
        select cvterm.cvterm_id
        from cvterm join cv using (cv_id)
        where cvterm.name = 'systematic_id'
        and cv.name = 'genedb_synonym_type'
)
and temporary_systematic_id.type_id in (
        select cvterm.cvterm_id
        from cvterm join cv using (cv_id)
        where cvterm.name = 'temporary_systematic_id'
        and cv.name = 'genedb_synonym_type'
);

/* For all these cases, change the feature_synonym to point to
   the systematic_id rather than the temporary_systematic_id
   version of the synonym. */
update feature_synonym
set synonym_id = (
    select clashing_systematic_ids.systematic_id_synonym_id
    from clashing_systematic_ids
    where feature_synonym.synonym_id
            = clashing_systematic_ids.temporary_systematic_id_synonym_id
)
where synonym_id in (
    select temporary_systematic_id_synonym_id
    from clashing_systematic_ids
);

/* Remove any unused synonyms, including the temporary_systematic_id
   synonyms that we have just reassigned. */
delete from synonym where synonym_id in (
  select synonym_id from synonym
  except (
      select synonym_id from feature_synonym
      union
      select synonym_id from library_synonym
  )
);

/* Finally update all temporary_systematic_id synonyms to
   become plain systematic_id. */
update synonym
set type_id = (
        select cvterm.cvterm_id
        from cvterm join cv using (cv_id)
        where cvterm.name = 'systematic_id'
        and cv.name = 'genedb_synonym_type'
)
where type_id = (
        select cvterm.cvterm_id
        from cvterm join cv using (cv_id)
        where cvterm.name = 'temporary_systematic_id'
        and cv.name = 'genedb_synonym_type'
);

/* Delete all *current* feature_synonyms to synonyms of type systematic_id */
delete from feature_synonym
using synonym
where feature_synonym.synonym_id = synonym.synonym_id
and synonym.type_id in (
        select cvterm.cvterm_id
        from cvterm join cv using (cv_id)
        where cvterm.name = 'systematic_id'
        and cv.name = 'genedb_synonym_type'
)
and feature_synonym.is_current
;

/* Delete the now unused CV term temporary_systematic_id */
delete from cvterm
using cv
where cvterm.cv_id = cv.cv_id
and cvterm.name = 'temporary_systematic_id'
and cv.name = 'genedb_synonym_type'
;

/* Rename the term 'systematic_id' to 'previous_systematic_id',
   now that it is only used for non-current synonyms. */
update cvterm
set name = 'previous_systematic_id'
where name = 'systematic_id'
and cv_id in (
    select cv_id from cv where name = 'genedb_synonym_type'
);

commit;