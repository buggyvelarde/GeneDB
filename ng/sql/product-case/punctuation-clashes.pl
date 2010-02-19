#!/usr/bin/perl

use strict;
use warnings FATAL => "all";

my %h;
while (<>) {
    chomp;
    push @{$h{normalise($_)}}, $_;
}

while (my($lc, $a) = each %h) {
    if (@$a > 1) {
        print map "$_\n", sort order @$a;
        print "\n";
    }
}

sub normalise {
    my ($s) = @_;
    $s = lc ($s);
    $s =~ s([^\w'/])()g;
    return $s;
}

sub order {
    my ($a_, $b_) = ($a, $b);
    $a_ =~ s([^\w'])()g;
    $b_ =~ s([^\w'])()g;
    
    my $cmp;
    
    if ($a =~ /^[A-Z][a-z]/ && $b =~ /^[a-z][a-z]/) {
        return +1;
    } elsif ($b =~ /^[A-Z][a-z]/ && $a =~ /^[a-z][a-z]/) {
        return -1;
    }

    $cmp = $a_ cmp $b_;
    return $cmp if $cmp;
    
    $cmp = ($a =~ / ,/) <=> ($b =~ / ,/);
    return $cmp if $cmp;
    
    $cmp = ($a =~ /- /) <=> ($b =~ /- /);
    return $cmp if $cmp;
    
    return (length($a)<=>length($b));
}