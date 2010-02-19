#!/usr/bin/perl -00ln

BEGIN { print "begin;\n\n" }

my ($canonical, @others) = split/\n/, $_;
$canonical =~ s/'/''/g;
for my $other (@others) {
    $other =~ s/'/''/g;
    print <<SQL;
delete from feature_cvterm
using (
    select fc_canonical.feature_cvterm_id as canonical_feature_cvterm_id
         , fc_other.feature_cvterm_id     as other_feature_cvterm_id
    from feature_cvterm fc_canonical
       , feature_cvterm fc_other
    where fc_canonical.feature_id = fc_other.feature_id
    and fc_canonical.cvterm_id = (
        select cvterm_id
        from cvterm join cv on cvterm.cv_id = cv.cv_id
        where cv.name = 'genedb_products'
        and cvterm.name = '$canonical'
    )
    and fc_other.cvterm_id = (
        select cvterm_id
        from cvterm join cv on cvterm.cv_id = cv.cv_id
        where cv.name = 'genedb_products'
        and cvterm.name = '$other'
    )
) clashes
    where feature_cvterm.feature_cvterm_id = clashes.other_feature_cvterm_id;
update feature_cvterm
    set cvterm_id = (
        select cvterm_id
        from cvterm join cv on cvterm.cv_id = cv.cv_id
        where cv.name = 'genedb_products'
        and cvterm.name = '$canonical'
    )
    where cvterm_id = (
        select cvterm_id
        from cvterm join cv on cvterm.cv_id = cv.cv_id
        where cv.name = 'genedb_products'
        and cvterm.name = '$other'
    );
delete from cvterm
    where cv_id = (
        select cv_id
        from cv
        where name = 'genedb_products'
    )
    and name = '$other';
SQL
}

END {
    print "create unique index cvterm_idx_genedb1 on cvterm (lower(name), cv_id, is_obsolete);\n";
    print "commit;\n";
}
