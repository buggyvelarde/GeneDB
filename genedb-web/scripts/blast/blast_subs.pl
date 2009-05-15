#!/usr/bin/perl

use warnings;
use strict;

my $template;
open my $f_template, "<", "blast_sub.template"
    or die "Failed to open blast_sub.template: $!\n";
$template = join "", <$f_template>;
close $f_template;

my %organisms;
open my $f_dbs, "<", "fastas.txt"
    or die "Failed to open fastas.txt: $!\n";
while (<$f_dbs>) {
    /^GeneDB_(.*)_(Genes|Proteins)$/ or die "$_??";
    $organisms{$1} = undef;
}
close $f_dbs;

open my $f_organism_names, "<", "organisms.tsv"
    or die "Failed to open organisms.tsv: $!\n";
while (<$f_organism_names>) {
    chomp;
    our ($common_name, $genus, $species) = split /\t/, $_;
    
    next unless exists $organisms{$common_name};
    
    my $text = $template;
    $text =~ s/\$\{(common_name|genus|species)\}/no strict "refs"; $$1/eg;
    print $text, "\n\n";
}
close $f_organism_names;
