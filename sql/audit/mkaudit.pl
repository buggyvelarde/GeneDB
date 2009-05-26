#!/usr/bin/perl

# mkaudit.pl - generate SQL code that installs audit tables and triggers
#              for a Chado database.
#
# (rh11, 2009-03-23)

use warnings; use strict;
use DBI;

# These database connection settings need to point to a Chado
# database; it need not be the database on which the audit
# schema will be installed, of course.

my $dbhost = "localhost";
my $dbport = 11111;
my $dbuser = "pathdb";
my $dbname = "pathogens";
my $dbpass = "Pyrate_1";

# For each audited table, we distinguish read-only columns,
# which should not be updated once the row exists, from
# read-write columns, which may be updated. The primary key
# column is always treated as read-only. The following hash
# maps each audited table to a list of read-only columns,
# beginning with the primary key.
my $audit = {
    feature        => [qw(feature_id timeaccessioned)],
    map {($_ => ["${_}_id"])} qw(
        organism
            organismprop
        featureprop
            featureprop_pub
        feature_cvterm
            feature_cvtermprop
            feature_cvterm_dbxref
            feature_cvterm_pub
        feature_pub
        feature_dbxref
        featureloc
        feature_relationship
        feature_synonym
        pub
            pub_dbxref
        dbxref
        cvterm
            cvterm_dbxref
        synonym
    )
};

my %exclude_cols = (
    feature => { residues => 1 },
);

# This model assumes that each table has a non-compound primary key,
# which is true for Chado.)

# The audit policy is:
# - read-only columns are forced to be read-only by the "on update" trigger
# - the primary key, and read-write columns, have their values logged on every operation
# - other read-only columns only have their values logged when the row is deleted.

# Thus the audit supertable (e.g. audit.feature) has columns for the primary
# key and for the read-write columns; the update subtable has additional old_
# columns for the read-write columns; and the delete subtable has additional
# columns for the non-primary read-only columns, if any.

# This representation is highly redundant. For example, if a row is inserted
# and later updated, it must be the case that the inserted values are equal to
# the old values of the update. Because the audit trail is archived
# and immutable, this does not cause consistency problems, and it makes the
# audit trail easier to use: for example, having the old and new values
# in the same table row makes it trivial to find out when a particular table
# cell (i.e. a particular column of a particular row) was changed. It does
# use more space though, which could become a problem eventually. However
# it would be easy to purge entries more than a year old every month, say.

my $dbh = DBI->connect(
    sprintf("dbi:Pg:dbname=%s;host=%s;port=%d", $dbname, $dbhost, $dbport),
    $dbuser, $dbpass);

print "Creating audit.sql\n";
create_file("audit.sql", <<END);
drop schema audit cascade;
create schema audit;
grant usage on schema audit to chado_rw_role, chado_ro_role;
create sequence audit.audit_seq;
grant usage on sequence audit.audit_seq to chado_rw_role, chado_ro_role;

create table audit.audit (
  audit_id   integer default nextval('audit.audit_seq' :: regclass) not null primary key
, type       character varying not null
, username   character varying not null default user
, time       timestamp without time zone not null default now()
);
grant select on audit.audit to chado_ro_role;
grant select on audit.audit to chado_rw_role;

create table audit.checkpoint (
  key      character varying not null primary key
, audit_id integer not null
);
grant select, insert, update on audit.checkpoint to chado_ro_role;
grant all on audit.checkpoint to chado_rw_role;

END

for my $table_name (sort keys %$audit) {
    my $ro_cols = $audit->{$table_name};
    my $pk_col  = shift @$ro_cols;
    write_audit_sql($table_name, $pk_col, $ro_cols);
}



sub write_audit_sql {
    my ($table_name, $pk_col, $ro_cols) = @_;
    
    print "Creating audit_$table_name.sql\n";
    create_file("audit_$table_name.sql", generate_audit_sql($table_name, $pk_col, $ro_cols));
    append_to_file("audit.sql", "\\i audit_$table_name.sql\n");
}

sub generate_audit_sql {
    my ($table_name, $pk_col, $ro_cols) = @_;
    return audit_table_sql($table_name, $pk_col, $ro_cols)."\n\n"
        .audit_insert_sql($table_name, $pk_col, $ro_cols) ."\n\n"
        .audit_update_sql($table_name, $pk_col, $ro_cols) ."\n\n"
        .audit_delete_sql($table_name, $pk_col, $ro_cols);
}

sub audit_table_sql {
    my ($table_name, $pk_col, $ro_cols) = @_;
    my $create_table_sql = "create table audit.$table_name (\n    ";
    $create_table_sql .= columns_sql($table_name, exclude => $ro_cols);
    $create_table_sql .= ") inherits (audit.audit);\n";
    
    my $create_trigger_sql = <<END;
create or replace function audit.audit_${table_name}_insert_proc()
returns trigger
as \$\$
BEGIN
  raise exception 'Cannot insert directly into audit.${table_name}. Use one of the child tables.';
END;
\$\$ language plpgsql;
create trigger ${table_name}_insert_tr before insert on audit.${table_name}
    for each statement execute procedure audit.audit_${table_name}_insert_proc();
END

    my $grant_privs_sql = <<END;
grant select on audit.${table_name} to chado_ro_role;
grant select, insert on audit.${table_name} to chado_rw_role;
grant execute on function audit.audit_${table_name}_insert_proc() to chado_rw_role;
END
    return "$create_table_sql\n$create_trigger_sql$grant_privs_sql";
}

sub audit_insert_sql {
    my ($table_name, $pk_col, $ro_cols) = @_;
    my @cols = column_names($table_name);

    my $create_table_sql = audit_subtable_sql($table_name, "insert",
        columns_sql($table_name, include => $ro_cols));
    
    my $cols_commasep = join ", ", @cols;
    my $new_dot_cols  = join ", ", map "new.$_", @cols;
    my $create_trigger_sql = <<ENDSQL;
create or replace function audit.public_${table_name}_insert_proc()
returns trigger
as \$\$
BEGIN
  insert into audit.${table_name}_insert (
      $cols_commasep
  ) values (
      $new_dot_cols
  );
  return new;
END;
\$\$ language plpgsql;
create trigger ${table_name}_audit_insert_tr after insert on public.${table_name}
    for each row execute procedure audit.public_${table_name}_insert_proc();
grant execute on function audit.public_${table_name}_insert_proc() to chado_rw_role;
ENDSQL
    
    return $create_table_sql . $create_trigger_sql;
}

sub audit_update_sql {
    my ($table_name, $pk_col, $ro_cols) = @_;
    my @cols = column_names($table_name);
    my $mapping = {map {("old_$_" => $_)} @cols};
    my $columns_sql = columns_sql($table_name, exclude => [$pk_col, @$ro_cols], mapping => $mapping);
    my $create_table_sql = audit_subtable_sql($table_name, "update", $columns_sql);
    
    my $create_trigger_sql = <<ENDSQL;
create or replace function audit.public_${table_name}_update_proc()
returns trigger
as \$\$
BEGIN
ENDSQL

    for my $col ($pk_col, @$ro_cols) {
        $create_trigger_sql .= <<ENDSQL
  if old.$col <> new.$col or old.$col is null <> new.$col is null then
    raise exception 'If you want to change ${table_name}.$col (do you really?) then disable the audit trigger ${table_name}_audit_update_tr';
  end if;
ENDSQL
    }
    
    my %ro_cols; @ro_cols{$pk_col, @$ro_cols} = ();
    my @rw_cols = grep {!exists $ro_cols{$_}} @cols;

    my $cols_commasep = join ", ", $pk_col, @rw_cols;
    my $new_dot_cols = join ", ", map "new.$_", $pk_col, @rw_cols;
    
    my $oldcols_commasep = join ", ", map "old_$_", @rw_cols;
    my $old_dot_cols = join ", ", map "old.$_", @rw_cols;

    $create_trigger_sql .= <<ENDSQL;
  insert into audit.${table_name}_update (
      $cols_commasep,
      $oldcols_commasep
   ) values (
       $new_dot_cols,
       $old_dot_cols
   );
  return new;
END;
\$\$ language plpgsql;
create trigger ${table_name}_audit_update_tr after update on public.${table_name}
    for each row execute procedure audit.public_${table_name}_update_proc();
grant execute on function audit.public_${table_name}_update_proc() to chado_rw_role;
ENDSQL
    
    return $create_table_sql . $create_trigger_sql;
}

sub audit_delete_sql {
    my ($table_name, $pk_col, $ro_cols) = @_;
    my @cols = column_names($table_name);
    
    my $create_table_sql = audit_subtable_sql($table_name, "delete",
        columns_sql($table_name, include => $ro_cols));
    
    my $cols_commasep = join ", ", @cols;
    my $old_dot_cols  = join ", ", map "old.$_", @cols;
    my $create_trigger_sql = <<ENDSQL;
create or replace function audit.public_${table_name}_delete_proc()
returns trigger
as \$\$
BEGIN
  insert into audit.${table_name}_delete (
      $cols_commasep
  ) values (
      $old_dot_cols
  );
  return old;
END;
\$\$ language plpgsql;
create trigger ${table_name}_audit_delete_tr after delete on public.${table_name}
    for each row execute procedure audit.public_${table_name}_delete_proc();
grant execute on function audit.public_${table_name}_delete_proc() to chado_rw_role;
ENDSQL
    
    return $create_table_sql . $create_trigger_sql;
}

sub audit_subtable_sql {
    my ($table_name, $type, $cols_sql) = @_;
    
    if (length $cols_sql) {
        $cols_sql = "  , $cols_sql";
    }
    
    my $uc_type = uc($type);
    return <<END;
create table audit.${table_name}_$type (
    constraint ${table_name}_${type}_ck check (type = '$uc_type')
$cols_sql) inherits (audit.${table_name});
alter table audit.${table_name}_$type alter type set default '$uc_type';
grant select on audit.${table_name}_$type to chado_ro_role;
grant select, insert on audit.${table_name}_$type to chado_rw_role;

END
}

sub columns_sql {
    my ($table_name, %args) = @_;
    
    my %exclude_cols = ();
    if (exists $args{exclude}) {
        for my $col (@{$args{exclude}}) {
            $exclude_cols{$col}++;
        }
    }
    
    my $mapping = $args{mapping};
    if (!defined $mapping) {
        # Process exclusions
        my @column_names = exists $args{include} ? @{$args{include}} : column_names($table_name);
        $mapping = { map {($_ => $_)} @column_names };
    }
    
    my $sql = "";
    for my $col (keys %$mapping) {
        next if exists $exclude_cols{$mapping->{$col}};
        $sql .= "  , " if 0 < length $sql;
        $sql .= "$col " . full_column_type($table_name, $mapping->{$col}) . "\n";
    }
    
    return $sql;
}

sub column_names {
    my ($table_name) = @_;
    
    my $sth = $dbh->column_info(undef, "public", $table_name, "%");
    my @column_names;
    while (my $column_metadata = $sth->fetchrow_hashref()) {
        my $column_name = $column_metadata->{COLUMN_NAME};
        
        # If the name of a column is a keyword (e.g. organism.comment) then
        # the COLUMN_NAME returned by column_info is surrounded by double quotes.
        # We need to remove those here.
        $column_name =~ s/^"(.*)"$/$1/;
        
        next if exists $exclude_cols{$table_name}{$column_name};
        push @column_names, $column_name;
    }
    return @column_names;
}

sub full_column_type {
    my ($table_name, $column_name) = @_;
    
    my $sth = $dbh->column_info(undef, "public", $table_name, $column_name);
    my $column_metadata = $sth->fetchrow_hashref();
    
    if (!$column_metadata) {
        die "Could not find column '$column_name' in table '$table_name'\n";
    }
    
    # TYPE_NAME is the official DBI column, but pg_type is more useful because it includes size info
    my $column_type = $column_metadata->{pg_type};
    
    $column_type .= " not null" if !$column_metadata->{NULLABLE};
    
    return $column_type;
}


sub create_file {
    my ($filename, $contents) = @_;
    open my $fh, ">", $filename
        or die "Failed to open $filename for writing: $!\n";
    
    print $fh "-- Autogenerated on ".localtime()." by $0\n\n";
    print $fh $contents;
    
    close $fh or die "Failed to close $filename: $!\n";
}

sub append_to_file {
    my ($filename, $contents) = @_;
    open my $fh, ">>", $filename
        or die "Failed to open $filename for appending: $!\n";
    
    print $fh $contents;
    close $fh or die "Failed to close $filename: $!\n";
}
