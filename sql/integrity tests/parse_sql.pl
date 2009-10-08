#!/usr/local/bin/perl
######################################################################
# author nds
# team 81
# started 8.10.2009

#This script just parses the sql files to make sure they are in the 
#right format.
#######################################################################

use strict;
use Tie::File;

opendir(DIR, '.') or die "Couldn't open directory, $!";
foreach (sort grep(/^.*\.sql$/,readdir(DIR))){
	my $file_name = $_;
	parse_sql_file("$file_name");
	}
closedir DIR;


##############################################################
#This sub-routine parses the .sql file given to it.
#We expect there to be some explanatory text within /* and */
#followed by the SQL statement. Both these items get bundled
#into an array and gets returned by this sub-routine. Tiefile
#is used here inorder to have extra flexibility when reading
#lines from the file.
##############################################################

sub parse_sql_file{
	
	my $text;
	my $sql;
	my $file = shift;
	tie my @sql_file, 'Tie::File', $file or die "Could not tie $file!";
	my $linenum = 0;
	my $totallines = @sql_file;
	print "$file \n";
	
	while ($linenum < $totallines) {
		
		if($sql_file[$linenum] =~ /\#\s*\S*/){
			my $comment = $sql_file[$linenum];
			$comment =~ s/\#(\s*\S*)/$1/g;
			$text = $text.$comment." ";
		}else{
			if($sql_file[$linenum] ne ""){
				my $line = $sql_file[$linenum];
				$line =~ s/\/\*\s*\S*\s*\*\///g; #Remove any inserted comments
				$sql = $sql.$line." ";
			}		
		}
		$linenum++;
		
	}
		
	print "$text \n";
	print "$sql \n";
	
}

