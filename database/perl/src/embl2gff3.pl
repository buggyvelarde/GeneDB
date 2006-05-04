#!/usr/local/bin/perl

=head1 NAME

embl2gff3 - takes an annotated embl file and converts to gff3 format
          - file ready for direct chado loading

=head1 SYNOPSIS

Examples:

 embl2gff3 -e <file> -d <dataset>

=head1 DESCRIPTION

This script takes an annotated EMBL file (stream or single seq) and creates
a chado ready gff3 file.

=head1 METHODS

None

=head1 AUTHOR

Ellen Schofield (es2@sanger.ac.uk)

Paul Mooney (pjm@sanger.ac.uk)

=head1 COPYRIGHT

Copyright (C) 2005 Genome Research Limited. All Rights Reserved.

=head1 DISCLAIMER

This software is provided "as is" without warranty of any kind. It may
be used, redistributed and/or modified under the same conditions as
Perl itself.

=cut

use strict;
use warnings;

use Getopt::Std;

use Bio::Tools::GFF;
use Bio::SeqIO;
use Bio::Coordinate::GeneMapper;

my $usage = "
 Usage : $0 -h (displays help)
    or : $0 -e <EMBL file> -s chromosome -d GeneDB_Pfalciparum

Options
   -e <file> : Annotated EMBL seq file
   -d <dataset> : Dataset (e.g. GeneDB_Tcongo)

";

my $help = "
This script takes an annotated EMBL file (stream or single seq) and creates
a chado ready gff3 file.
";

my $file;
my $database = "";
my $source_type = '';

my %options = ();
getopts( "he:d:s:", \%options );

defined $options{h} and die "$help$usage";
defined $options{e} or die "\nNo seq file supplied\n$usage";
defined $options{s} and $source_type = $options{s};

defined $options{d} and $database = $options{d};

if ( $database eq "" ) {
    print STDERR "Which GeneDB Dataset is this [GeneDB_Tcongo]? ";
    $database = <STDIN>;
    chomp $database;
}

my %progs = (
             'pfam'    => 'PFAM',
             'tmhmm'   => 'TMHMM',
             'prosite' => 'PROSITE',
             'prints'  => 'PRINTS',
             'prodom'  => 'PRODOM',
             'tigrfam' => 'TIGRFAM',
             'smart'   => 'SMART',
             'sigp'    => 'SIGNALP',
             'fatsa'   => 'FASTA'
             );

my %allowed = (
               'ID'            => 1,
               'Name'          => 1,
               'Alias'         => 1,
               'Dbxref'        => 1,
               'Derives_from'  => 1,
               'Parent'        => 1,
               'Note'          => 1,
               'Ontology_term' => 1,
               'product'       => 1,
               'curation'      => 1,
               'colour'        => 1,
               'ortholog'      => 1,
               'chromosome'    => 1,
               'contig'        => 1,
               );

my $seqio = new Bio::SeqIO(
                           -format => 'embl',
                           -file   => $options{e}
                           );

my %feats    = ();
my %peps     = ();
my %cdss     = ();
my %features = ();
my @genes;
my @g_ids;

open START, ">partial_start.txt"
  or die "Unable to open file partial_start.txt\n";
open STOP, ">partial_stop.txt" or die "Unable to open file partial_stop.txt\n";
open ANALYSIS, ">analysis.txt" or die "Unable to open file analysis.txt\n";

my $fasta = new Bio::SeqIO(
                           -file   => ">seq.fa",
                           -format => 'fasta'
                           );

while ( my $seq = $seqio->next_seq ) {

    my @non_cds = ();

    # defined a default name
    my $fname = sprintf( "%s", $seq->display_id );

    print STDERR "Converting $fname...\n";

    my $gffout_hits = '';

    #	= new Bio::Tools::GFF(
    #		-file        => ">$fname.matches.gff",
    #		-gff_version => 3
    #	);

    $fasta->write_seq($seq);

    foreach my $f ( $seq->top_SeqFeatures() ) {
        my $pseudo = 0;

        $f->source_tag($database);

        #Feature Keys
        foreach my $tag ( $f->get_all_tags() ) {
            $f->add_tag_value( 'Dbxref', $f->remove_tag($tag) )   if ( $tag eq 'db_xref' || $tag eq 'dbxref' );
            $f->add_tag_value( 'Note', $f->remove_tag($tag) )     if ( $tag eq 'note' );
            $f->add_tag_value( 'ID', $f->remove_tag($tag) )       if ( $tag eq 'systematic_id'
                                                                      || $tag eq 'temporary_systematic_id' );
            $f->add_tag_value( 'Name', $f->remove_tag($tag) )     if ( $tag eq 'primary_name' );
            $f->add_tag_value( 'Alias', $f->remove_tag($tag) )    if ( $tag eq 'synonym' );
            $f->add_tag_value( 'ortholog', $f->remove_tag($tag) ) if ( $tag eq 'orthologue' );

            if ( $tag =~ /go/i ) {
                foreach ( $f->remove_tag($tag) ) {
                    $f->add_tag_value( 'Ontology_term', $1 )
                        if ( $_ =~ /GOid=(GO:\d+)/ );
                }
            }
            $pseudo = 1 && $f->remove_tag($tag) if ( $tag eq 'pseudo' );
        }

        if ( $f->has_tag('similarity') ){
            $f->add_tag_value(
                              'Dbxref',
                              writeSimilarities(
                                                $gffout_hits, $f, ${ [ $f->get_tag_values('ID') ] }[0]
                                                )
                              );
            $f->remove_tag('similarity');
        }

        my %tagValues = &get_all_tag_values($f);

        unless ( $f->primary_tag eq 'CDS' ) {
            push( @non_cds, $f );
            next;
        }

        my $id = ${ [ $f->get_tag_values('ID') ] }[0];
        $cdss{$id} = $f->location();

        my $gene = new Bio::SeqFeature::Generic(
                                                -primary_tag => 'gene',
                                                -source_tag  => $database,
                                                -seq_id      => $fname
                                                );
        $gene->primary_tag('pseudogene') if $pseudo;

        $gene->location(
			Bio::Location::Simple->new(
                                                   -start  => $f->start,
                                                   -end    => $f->end,
                                                   -strand => $f->strand
                                                   )
                        );

        foreach my $t ( keys %tagValues ) {
            $gene->add_tag_value( $t, @{ $tagValues{$t} } );
            
            #print "*****$t\t";
        }

        #print "\n";
        push( @{ $features{ $gene->primary_tag } }, $gene );

        #$gffout_genes->write_feature($gene);
        push( @genes, $gene );
        push( @g_ids, $id );

        my $mrna = new Bio::SeqFeature::Generic(
                                                -primary_tag => 'transcript',
                                                -source_tag  => $database,
                                                -seq_id      => $fname
                                                );
        $mrna->primary_tag('pseudogenic_transcript') if $pseudo;

        $mrna->location(
			Bio::Location::Simple->new(
                                                   -start  => $f->start,
                                                   -end    => $f->end,
                                                   -strand => $f->strand
                                                   )
                        );

        foreach my $t ( keys %tagValues ) {
            $mrna->add_tag_value( $t, @{ $tagValues{$t} } );
        }

        $mrna->remove_tag('ID') if $mrna->has_tag('ID');
        $mrna->add_tag_value( 'Parent', "$id" );
        $mrna->add_tag_value( 'ID',     "mrna.$id" );

        push( @{ $features{ $mrna->primary_tag } }, $mrna );

        unless ($pseudo) {
            my $cds = new Bio::SeqFeature::Generic(
                                                   -primary_tag => 'CDS',
                                                   -source_tag  => $database,
                                                   -seq_id      => $fname
                                                   );

            $cds->location(
                           Bio::Location::Simple->new(
                                                      -start  => $f->start,
                                                      -end    => $f->end,
                                                      -strand => $f->strand
                                                      )
                           );

            foreach my $t ( keys %tagValues ) {
                $cds->add_tag_value( $t, @{ $tagValues{$t} } );
            }
            $cds->remove_tag('ID') if $cds->has_tag('ID');
            $cds->add_tag_value( 'Parent', "mrna.$id" );
            $cds->add_tag_value( 'ID',     "cds.$id" );

            push( @{ $features{ $cds->primary_tag } }, $cds );

            #$gffout_cds->write_feature($cds);
        }

        my $count  = 1;
        my $length = 0;
        foreach my $e ( $f->location->each_Location() ) {

            my $exon = new Bio::SeqFeature::Generic(
                                                    -primary_tag => 'exon',
                                                    -source_tag  => $database,
                                                    -seq_id      => $fname
                                                    );
            $exon->primary_tag('pseudogenic_exon') if $pseudo;

            $exon->location(
                            Bio::Location::Simple->new(
                                                       -start  => $e->start,
                                                       -end    => $e->end,
                                                       -strand => $e->strand
                                                       )
                            );

            foreach my $t ( keys %tagValues ) {
                $exon->add_tag_value( $t, @{ $tagValues{$t} } );
            }
            $exon->remove_tag('ID')     if $exon->has_tag('ID');
            $exon->remove_tag('Parent') if $exon->has_tag('Parent');
            $exon->add_tag_value( 'Parent', "mrna.$id" );
            $exon->add_tag_value( 'ID',     "$id\_$count" );

            push( @{ $features{ $exon->primary_tag } }, $exon );

            $length += $exon->length;
            $count++;
        }

        unless ($pseudo) {

            my $pep = new Bio::SeqFeature::Generic(
                                                   -primary_tag => 'polypeptide',
                                                   -source_tag  => $database,
                                                   -seq_id      => "pep.$id"
                                                   );

            $pep->location(
                           Bio::Location::Simple->new(
                                                      -start  => 1,
                                                      -end    => int $length / 3,
                                                      -strand => 0
                                                      )
                           );

            foreach my $t ( keys %tagValues ) {
                $pep->add_tag_value( $t, @{ $tagValues{$t} } );
            }
            $pep->remove_tag('ID') if $pep->has_tag('ID');
            $pep->add_tag_value( 'Derives_from', "cds.$id" );
            $pep->add_tag_value( 'ID',           "pep.$id" );
            $pep->seq_id("pep.$id");

            $peps{$id} = $pep->location();

            push( @{ $features{ $pep->primary_tag } }, $pep );

            #$gffout_peps->write_feature($pep);
        }
    }

    #NON CDS FEATURES
    foreach my $f (@non_cds) {
        my %tagValues = &get_all_tag_values($f);

        # DEBUG
        #foreach my $key (keys %tagValues) {
        #    print STDERR "$key = ", $tagValues{$key}, "\n";
        #}

        if ( $f->primary_tag eq 'misc_feature' ) {

            #warn "no gene name found for feature at "
            #  . $f->start . ".."
            #  . $f->end
            #  . " - guessing using coordinates\n"
            #  unless $f->has_tag('gene');

            my $id = '';
            if ( $f->has_tag('gene') ) {
                $id = ${ [ $f->get_tag_values('gene') ] }[0];
            }
            else {
                my $c = -1;
                foreach my $g (@genes) {
                    $c++;
                    next unless $g->contains($f);
                    $id = $g_ids[$c];
                    last;
                }
            }
            if (
                $f->has_tag('type')
                && grep /pfam|tmhmm|prosite|prints|prodom|tigrfam|smart/i,
                $f->get_tag_values('type')
                )
            {
                my $type = lc ${ [ $f->get_tag_values('type') ] }[0];
                $feats{$id}{$type}++;
                
                foreach my $e ( $f->location->each_Location(1) ) {
                    my $dom = new Bio::SeqFeature::Generic(
                                                           -primary_tag => 'polypeptide_domain',
                                                           -source_tag  => $progs{$type}
                                                           );

                    $dom->score( ${ [ $f->remove_tag('score') ] }[0] )
                        if ( $f->has_tag('score') );

                    foreach my $t ( keys %tagValues ) {
                        $dom->add_tag_value( $t, @{ $tagValues{$t} } );
                    }
                    $dom->location($e);
                    map_chr2pep( $dom, $id ) unless ( $id eq '' );
                    $dom->remove_tag('ID')   if $dom->has_tag('ID');
                    $dom->remove_tag('type') if $dom->has_tag('type');
                    $dom->add_tag_value( 'Parent', "pep.$id" );
                    $dom->add_tag_value( 'ID',
                                         "$type.$id\_$feats{$id}{$type}" );
                    $dom->seq_id("pep.$id");
                    push( @{ $features{ $dom->primary_tag } }, $dom );
                    $feats{$id}{$type}++;
                }

                #$gffout_misc->write_feature($dom);
            }
            else {
                my $dom = new Bio::SeqFeature::Generic(
                                                       -primary_tag => 'remark',
                                                       -source_tag  => $database,
                                                       -seq_id      => $fname
                                                       );

                foreach my $t ( keys %tagValues ) {
                    $dom->add_tag_value( $t, @{ $tagValues{$t} } );
                }
                $dom->location(
                               Bio::Location::Simple->new(
                                                          -start  => $f->start,
                                                          -end    => $f->end,
                                                          -strand => $f->strand
                                                          )
                               );
                push( @{ $features{ $dom->primary_tag } }, $dom );

                #$gffout_misc->write_feature($dom);
            }
        }
        elsif ( $f->primary_tag eq 'sig_peptide' ) {
            warn "no gene name found for feature at "
                . $f->start . ".."
                . $f->end && next
                unless $f->has_tag('gene');

            my $id = ${ [ $f->get_tag_values('gene') ] }[0];
            $feats{$id}{'sigp'}++;

            foreach my $e ( $f->location->each_Location(1) ) {

                my $dom = new Bio::SeqFeature::Generic(
                                                       -primary_tag => 'signal_peptide',
                                                       -source_tag  => $progs{'sigp'}
                                                       );

                $dom->score( ${ [ $f->remove_tag('score') ] }[0] )
                    if ( $f->has_tag('score') );

                foreach my $t ( keys %tagValues ) {
                    $dom->add_tag_value( $t, @{ $tagValues{$t} } );
                }

                $dom->location($e);
                map_chr2pep( $dom, $id );
                $dom->remove_tag('ID')   if $dom->has_tag('ID');
                $dom->remove_tag('type') if $dom->has_tag('type');
                $dom->add_tag_value( 'Parent', "pep.$id" );
                $dom->add_tag_value( 'ID',     "sigp.$id\_$feats{$id}{sigp}" );
                $dom->seq_id("pep.$id");

                push( @{ $features{ $dom->primary_tag } }, $dom );
            }

            #$gffout_misc->write_feature($dom);
        }
        elsif ( $f->primary_tag eq 'source' ) {
            my $source = new Bio::SeqFeature::Generic(
                                                      -primary_tag => 'TEST',
                                                      -source_tag  => $database,
                                                      -seq_id      => $fname
                                                      );
            $source->location(
                              Bio::Location::Simple->new(
                                                         -start  => $f->start,
                                                         -end    => $f->end,
                                                         -strand => $f->strand
                                                         )
                              );

            $source->seq_id($fname);

            foreach my $t ( keys %tagValues ) {
                #print STDERR "source $t ", $tagValues{$t}, "\n";
                $source->add_tag_value( $t, @{ $tagValues{$t} } );
            }

            #print STDERR "\n";

            if ( $source->has_tag('chromosome') ) {
                $source->primary_tag('chromosome');
                $source->add_tag_value( 'ID', $fname );
            }
            elsif ( $source->has_tag('contig') ) {
                $source->primary_tag('contig');

                my $tmp_id = ${ [ $source->remove_tag('contig') ] }[0];
                $source->add_tag_value( 'ID', $tmp_id );

                my $tmpseq = Bio::Seq->new(
                                           -display_id => $tmp_id,
                                           -seq        => $f->seq->seq
                                           );
                $fasta->write_seq($tmpseq);

                print STDERR "Changed source for contig with ID = $tmp_id\n";

            }
            elsif ($source_type ne '' && $source->has_tag('ID')){
                $source->primary_tag($source_type);
            }

            push( @{ $features{ $source->primary_tag } }, $source );

            #$gffout_sources->write_feature($source);
        }
        elsif ( $f->primary_tag =~ /mrna/i ) {
            my $mrna = new Bio::SeqFeature::Generic(
                                                    -primary_tag => 'mRNA',
                                                    -source_tag  => $database,
                                                    -seq_id      => $fname
                                                    );
            $mrna->location( $f->location() );

            my $id = ${ [ $f->get_tag_values('ID') ] }[0];

            foreach my $t ( keys %tagValues ) {
                if ( exists $allowed{$t} ) {
                    $mrna->add_tag_value( $t, @{ $tagValues{$t} } );
                }
                else {
                    foreach ( $f->get_tag_values($t) ) {
                        printf ANALYSIS "%s\t%s\t%s\n", $id, $t, $_;
                    }
                }
            }

            printf START "%s\n", $id
                if ( $f->location->start_pos_type eq 'BEFORE' );
            printf STOP "%s\n", $id
                if ( $f->location->end_pos_type eq 'AFTER' );
            
            push( @{ $features{ $mrna->primary_tag } }, $mrna );
        }
    }
}

&gffFeatures( \%features );

exit;

sub gffFeatures() {
    my $features = shift;

    my %contigs = ();
    my %seen    = ();

    my @order =
        qw/chromosome contig gene pseudogene transcript pseudogenic_transcript mRNA exon pseudogenic_exon CDS polypeptide signal_peptide polypeptide_domain/;

    foreach my $key (@order) {
        $seen{$key}++;

        if ( exists $$features{$key} ) {

            my $gffout = new Bio::Tools::GFF(
                                             -file        => ">$key.gff",
                                             -gff_version => 3
                                             );

            foreach ( @{ $$features{$key} } ) {
                $gffout->write_feature($_);
            }
        }
    }
    foreach my $key ( keys %{$features} ) {
        next if exists $seen{$key};

        my $gffout = new Bio::Tools::GFF(
                                         -file        => ">$key.gff",
                                         -gff_version => 3
                                         );

        foreach ( @{ $$features{$key} } ) {
            $gffout->write_feature($_);
        }
    }
}

sub map_chr2pep {
	my ( $feat, $id ) = @_;

	my @locs;
	my @exons;

	my $cds = $cdss{$id};

	if ( $cds->isa('Bio::Location::Split') ) {
		@exons = $cds->sub_Location;
	}
	else {
		push( @exons, $cds );
	}

	my $dna = Bio::Coordinate::GeneMapper->new(
		-in             => 'chr',
		-out            => 'peptide',
		-cds            => $cds,
		-exons          => \@exons,
		-peptide_offset => 0
	);

	$feat->location( $dna->map($feat) );
	return;
}

#similarity="type; database:id; organism; product; gene name; length; identity; ungapped id; e-value; score; overlap; q overlap; s overlap"
sub writeSimilarities {
	my ( $gffout, $feat, $id ) = @_;
	my @dbxrefs;

	my ( $start, $end, $strand ) = ( $feat->start, $feat->end, $feat->strand );

	my %prog2so = (
		'BLASTP'  => 'protein_match',
		'TBLASTX' => 'translated_nucleotide_match',
		'BLASTN'  => 'nucleotide_match',
		'TBLASTN' => 'protein_match',
		'BLASTX'  => 'translated_nucleotide_match',
		'FASTA'   => 'protein_match',
	);

	my @values = $feat->get_tag_values('similarity');
	my $count  = 1;

	foreach (@values) {

		printf ANALYSIS "%s\t%s\t%s\n", $id, 'similarity', $_;

		#
		#		$_ =~ s/;\s+/;/g;
		my @tmp = split( ";", $_ );

		#		my $prog = uc( $tmp[0] );
		#		my $so   = $prog2so{$prog};
		#
		#		my $score = 0;
		#		if ( $tmp[9] =~ /score=(.*)/ ) {
		#			$score = $1;
		#		}
		#		elsif ( $tmp[8] =~ /E\(\)=(.*)/ ) {
		#			$score = $1;
		#		}
		#
		#		my $sim = new Bio::SeqFeature::Generic(
		#			-primary_tag => $so,
		#			-source_tag  => $prog
		#		);
		#
		#		$sim->location(
		#			Bio::Location::Simple->new(
		#				-start  => $start,
		#				-end    => $end,
		#				-strand => $strand
		#			)
		#		);
		#
		#		$sim->add_tag_value( 'ID', "sim.$id\_$count" );
		#		$sim->score($score);
		#		$sim->seq_id( $feat->seq_id );
		#
		#		if (   $prog eq 'BLASTN'
		#			|| $prog eq 'TBLASTN'
		#			|| $prog eq 'TBLASTX' )
		#		{
		#			$sim->add_tag_value( 'Parent', $id );
		#		}
		#		else {
		#			$sim->add_tag_value( 'Parent', "pep.$id" );
		#		}

		my $db = $tmp[1];
		if ( $db =~ /\s+\(/ ) {
			($db) = $db =~ /\s?(.*)\s+\(/;
		}
		$db = "UniProt:$1" if ( $db =~ /SWALL:(.*)/ );
		$db = "UniProt:$1" if ( $db =~ /SPTREMBL:(.*)/ );

		unless ( $db eq '' ) {
			push( @dbxrefs, $db );

			#$sim->add_tag_value( 'Target', "$db $start $end $strand" );
		}

		#$gffout->write_feature($sim);
		$count++;
	}
	return @dbxrefs;
}

sub get_all_tag_values() {
	my $f   = shift;
	my %tmp = ();

	foreach my $tag ( $f->get_all_tags() ) {
		@{ $tmp{$tag} } = $f->get_tag_values($tag) if ( exists $allowed{$tag} );

		#print "$tag\t";
	}

	#print "\n";

	return %tmp;
}
