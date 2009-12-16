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
my @common_names;
while (<$f_organism_names>) {
    chomp;
    our ($common_name, $genus, $species) = split /\t/, $_;
    our $common_name_lc = lc($common_name);
    
    next unless exists $organisms{$common_name};
    
    push @common_names, $common_name;
    
    my $text = $template;
    $text =~ s/\$\{(common_name(_lc)?|genus|species)\}/no strict "refs"; $$1/eg;
    print $text, "\n\n";
}
close $f_organism_names;


my ($transcript_databases, $protein_databases) = ("", "");
for my $common_name (@common_names) {
    $transcript_databases .= qq(                             "genedb/GeneDB_${common_name}_Genes",\n);
    $protein_databases    .= qq(                             "genedb/GeneDB_${common_name}_Proteins",\n);
}

print <<END;
# OmniBLAST
'genedb_proteins' => {
        'home'          =>  'http://beta.genedb.org/Homepage',
        'name'         =>  'GeneDB proteins',
        'action'      =>  '/blast/blast_server',
        'prologue'      =>  qq(
These databases contain the polypeptide sequences of all the GeneDB organisms.
        ),

     'databases'     =>  [
$protein_databases
         ],
},
'genedb_transcripts' => {
        'home'          =>  'http://beta.genedb.org/Homepage',
        'name'         =>  'GeneDB transcripts',
        'action'      =>  '/blast/blast_server',
        'prologue'      =>  qq(
These databases contain the transcript sequences of all the GeneDB organisms.
        ),

     'databases'     =>  [
$transcript_databases
         ],
},
END
