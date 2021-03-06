-- Autogenerated on Fri May  8 09:42:01 2009 by mkaudit.pl

create table audit.featureprop (
    feature_id integer not null
  , featureprop_id integer not null
  , value text
  , type_id integer not null
  , rank integer not null
) inherits (audit.audit);

create or replace function audit.audit_featureprop_insert_proc()
returns trigger
as $$
BEGIN
  raise exception 'Cannot insert directly into audit.featureprop. Use one of the child tables.';
END;
$$ language plpgsql;
create trigger featureprop_insert_tr before insert on audit.featureprop
    for each statement execute procedure audit.audit_featureprop_insert_proc();
grant select on audit.featureprop to chado_ro_role;
grant select, insert on audit.featureprop to chado_rw_role;
grant execute on function audit.audit_featureprop_insert_proc() to chado_rw_role;


create table audit.featureprop_insert (
    constraint featureprop_insert_ck check (type = 'INSERT')
) inherits (audit.featureprop);
alter table audit.featureprop_insert alter type set default 'INSERT';
grant select on audit.featureprop_insert to chado_ro_role;
grant select, insert on audit.featureprop_insert to chado_rw_role;

create or replace function audit.public_featureprop_insert_proc()
returns trigger
as $$
BEGIN
  insert into audit.featureprop_insert (
      featureprop_id, feature_id, type_id, value, rank
  ) values (
      new.featureprop_id, new.feature_id, new.type_id, new.value, new.rank
  );
  return new;
END;
$$ language plpgsql;
create trigger featureprop_audit_insert_tr after insert on public.featureprop
    for each row execute procedure audit.public_featureprop_insert_proc();
grant execute on function audit.public_featureprop_insert_proc() to chado_rw_role;


create table audit.featureprop_update (
    constraint featureprop_update_ck check (type = 'UPDATE')
  , old_type_id integer not null
  , old_rank integer not null
  , old_value text
  , old_feature_id integer not null
) inherits (audit.featureprop);
alter table audit.featureprop_update alter type set default 'UPDATE';
grant select on audit.featureprop_update to chado_ro_role;
grant select, insert on audit.featureprop_update to chado_rw_role;

create or replace function audit.public_featureprop_update_proc()
returns trigger
as $$
BEGIN
  if old.featureprop_id <> new.featureprop_id or old.featureprop_id is null <> new.featureprop_id is null then
    raise exception 'If you want to change featureprop.featureprop_id (do you really?) then disable the audit trigger featureprop_audit_update_tr';
  end if;
  insert into audit.featureprop_update (
      featureprop_id, feature_id, type_id, value, rank,
      old_feature_id, old_type_id, old_value, old_rank
   ) values (
       new.featureprop_id, new.feature_id, new.type_id, new.value, new.rank,
       old.feature_id, old.type_id, old.value, old.rank
   );
  return new;
END;
$$ language plpgsql;
create trigger featureprop_audit_update_tr after update on public.featureprop
    for each row execute procedure audit.public_featureprop_update_proc();
grant execute on function audit.public_featureprop_update_proc() to chado_rw_role;


create table audit.featureprop_delete (
    constraint featureprop_delete_ck check (type = 'DELETE')
) inherits (audit.featureprop);
alter table audit.featureprop_delete alter type set default 'DELETE';
grant select on audit.featureprop_delete to chado_ro_role;
grant select, insert on audit.featureprop_delete to chado_rw_role;

create or replace function audit.public_featureprop_delete_proc()
returns trigger
as $$
BEGIN
  insert into audit.featureprop_delete (
      featureprop_id, feature_id, type_id, value, rank
  ) values (
      old.featureprop_id, old.feature_id, old.type_id, old.value, old.rank
  );
  return old;
END;
$$ language plpgsql;
create trigger featureprop_audit_delete_tr after delete on public.featureprop
    for each row execute procedure audit.public_featureprop_delete_proc();
grant execute on function audit.public_featureprop_delete_proc() to chado_rw_role;
