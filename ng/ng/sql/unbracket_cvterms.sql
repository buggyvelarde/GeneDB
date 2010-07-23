begin;

create temporary table cvterm_brack as
select without.name
     , with.cvterm_id    AS with_cvterm_id
     , without.cvterm_id AS without_cvterm_id
from cvterm with
   , cvterm without
where with.name like '[%]'
and with.name = '[' || without.name || ']'
;

update feature_cvterm
set cvterm_id = cvterm_brack.without_cvterm_id
from cvterm_brack
where feature_cvterm.cvterm_id = cvterm_brack.with_cvterm_id
;

delete from cvterm
where cvterm_id in (
    select with_cvterm_id
    from cvterm_brack
)
;

update cvterm
set name = substring(name, 2, length(name)-2)
  , definition = substring(definition, 2, length(definition)-2)
where name like '[%]'
;
