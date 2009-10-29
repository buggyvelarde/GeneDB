#!/usr/local/bin/perl
######################################################################
# author nds
# team 81
# started 5.10.2009

# This script should:
# 1) Locate each of the sql files that need to be run
# 2) Run them and, for each, write out the results in html format
# 3) Place the html pages in the right folders 
#######################################################################

use strict;
use Tie::File;
use DBI;

#First we start off an html file to contain the summary of the results. 
#All html files created in this script will be labelled with today's
#date so that it is possible to organise them in an archive and search/delete
#as necessary. As a sanity check, we delete any files in the current folder 
#that have the same name. This step should probably be changed later as there
#is a chance that we may delete something irrelevant to this script 
#accidentally.  


my @timeData = localtime(time);
my $year = $timeData[5]+1900;
my $month = $timeData[4]+1;
my $day = $timeData[3];
my $start_time = "$timeData[2]:$timeData[1]";

my $summary_file =  "/tmp/$year\_$month\_$day\_summary.html";

system("rm -f /tmp/$year\_$month\_$day\_*") and die "Could not delete existing files starting with name $year\_$month\_$day\_! in tmp directory\n";

open(SUMMARY_PAGE, ">>$summary_file") or die "Unable to open the necessary html file $summary_file! \n";
print SUMMARY_PAGE "<HTML>";
print SUMMARY_PAGE "<HEAD>";
print SUMMARY_PAGE "<TITLE>Summary of SQL integrity tests: $day.$month.$year</TITLE>";
print SUMMARY_PAGE "</HEAD>";
print SUMMARY_PAGE "<H1>Summary of SQL integrity tests: $day.$month.$year</H1>";
print SUMMARY_PAGE "<BODY>";
print SUMMARY_PAGE "<TABLE BORDER=1>";
print SUMMARY_PAGE "<TH>Status</TH><TH>Query</TH><TH>Results</TH><TH>Description</TH>";

#Also try setting up a connection to the database. At the moment, connection & directory details are
#hard-coded. It is possible later to grab these from the command line or from a config file

my $dbname = 'pathogens';
my $dbhost = 'pgsrv1';
my $dbport = '5432';
my $dbuser = 'genedb';
my $dbpass = 'genedb';
my $dbi_connect = "DBI:Pg:dbname=$dbname;host=$dbhost;port=$dbport";
my $dbh = DBI->connect($dbi_connect, $dbuser, $dbpass) or die "Can't connect to database: $DBI::errstr\n";

#Iterate through all the SQL files in the specified directory, parse them, 
#run them and print the output appropriately

my $count = 0;
my $failed_tests = 0;
chdir('./sql/integrity tests');
opendir(DIR, '.') or die "Couldn't open directory, $!";
foreach (sort grep(/^.*\.sql$/,readdir(DIR))){
	$count++;
	my $file_name = $_;
	my @values = parse_sql_file("$file_name");
	my $sth = run_sql($dbh, $values[1]);
	print_results($file_name, $values[0], $sth);
}
closedir DIR;

@timeData = localtime(time);
my $end_time = "$timeData[2]:$timeData[1]";

#Close & move files, tidy up

$dbh->disconnect;

print SUMMARY_PAGE "</TABLE>";
print SUMMARY_PAGE "<HR /><P>Ran $count tests. Started: $start_time Ended: $end_time</P>";
print SUMMARY_PAGE "</BODY>";
print SUMMARY_PAGE "</HTML>";
close SUMMARY_PAGE;

#First move whatever is in current/ to archive/, and then copy over the new results to current/
print "Moving old html files from current/ archive/ \n";
system("mv /nfs/pathdata/jira/httpd-2.2.9/htdocs/DBIntegrity/current/*.html /nfs/pathdata/jira/httpd-2.2.9/htdocs/DBIntegrity/archive/");
print "Removing old sql results files from the archive directory \n";
system("rm -rf /nfs/pathdata/jira/httpd-2.2.9/htdocs/DBIntegrity/archive/*.sql.html");
print "Copying latest results into current/ \n";
system("mv /tmp/$year\_$month\_$day\_*.html /nfs/pathdata/jira/httpd-2.2.9/htdocs/DBIntegrity/current") and die "Could not move files starting with $year\_$month\_$day\_ from /tmp folder to /nfs/pathdata/jira/httpd-2.2.9/htdocs/DBIntegrity/current!\n";
print "Finished. See results at http://developer.genedb.org/DBIntegrity/ \n";

if ($failed_tests > 0){
	exit ($failed_tests);
}else{
	exit (0);
}




##############################################################
#This sub-routine parses the .sql file given to it.
#We expect there to be some explanatory text - each line 
#starting with a # - followed by the SQL statement. 
#Both these items get bundled into an array and gets returned 
#by this sub-routine. Tiefile is used here inorder to have 
#extra flexibility when reading lines from the file.
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
				$line =~ s/\/\*\s*\S*\s*\*\///g; #Remove any inserted comments within the sql statement
				$sql = $sql.$line." ";
			}		
		}
		$linenum++;
		
	}
	my @info = ($text, $sql);
	return @info;		
}


#################################################################
#This sub-routine runs the SQL statement and returns the results,
#if there are any.
#################################################################

sub run_sql{
	my $dbh = shift;
	my $sql_statement = shift;
	print "Running: $sql_statement \n";
	my $sth = $dbh -> prepare($sql_statement);
	$sth->execute();
	return $sth;
}

#################################################################
#This sub-routine prints out the results in the appropriate HTML
#files. We print a summary of the query execution in the summary
#page and lists out the rows from the database query (if any) in
#the releveant results page. Since there were too many rows
#returned to make them work at a reasonable speed in one html
#file, we create an html file per resultset. This decision will
#be reviewed later.
#################################################################

sub print_results{
	my $query_name = shift;
	my $exp_text = shift; #Explanatory text
	my $sth = shift;
	my $status = "Failed";
	my $results_file = "/tmp/$year\_$month\_$day\_$query_name.html";
	if($sth->rows==0){
		$status = "Passed";
	}else{
		#Create and open an html file for the rows
		$failed_tests++;
		open(RESULTS_PAGE, ">>$results_file") or die "Unable to open the necessary html file $results_file! \n";
		print RESULTS_PAGE "<HTML>";
		print RESULTS_PAGE "<HEAD>";
		print RESULTS_PAGE "<TITLE>Results of $query_name: $day.$month.$year</TITLE>";
		print RESULTS_PAGE "</HEAD>";
		print RESULTS_PAGE "<H1>Results of '$query_name': $day.$month.$year</H1>";
		print RESULTS_PAGE "<BODY>";

	}
	
	print SUMMARY_PAGE "<TR>";
	if($status eq "Passed"){
		print SUMMARY_PAGE "<TD><IMG SRC=\"../images/passed.png\" /></TD>";
	}else{
		print SUMMARY_PAGE "<TD><IMG SRC=\"../images/failed.png\" /></TD>";
	}

	print SUMMARY_PAGE "<TD><A HREF=\"http://github.com/sanger-pathogens/GeneDB/blob/master/sql/integrity%20tests/$query_name\" TARGET=\"_blank\">$query_name</A></TD>";
	
	if($status eq "Passed"){
		print SUMMARY_PAGE "<TD>N/A</TD>";
	}else{
		print SUMMARY_PAGE "<TD><A HREF=\"http://developer.genedb.org/DBIntegrity/current/$results_file\" TARGET=\"_blank\">".$sth->rows." rows</A></TD>";
		
		#Print the rows in the results page
		print RESULTS_PAGE "<P>$query_name</P>";
		print RESULTS_PAGE "<TABLE BORDER=1>";
		my $num_of_fields = $sth->{NUM_OF_FIELDS};
		my $i = 0;
		for(0..$num_of_fields-1){	
			print RESULTS_PAGE "<TH>".$sth->{NAME}->[$i]."</TH>";
			$i++;
		} 
		while (my @data = $sth->fetchrow_array()) {
			print RESULTS_PAGE "<TR>";	
			foreach my $field (@data){
				print RESULTS_PAGE "<TD>$field</TD>";
			}
           	print RESULTS_PAGE "</TR>";
        }
        print RESULTS_PAGE "</TABLE>";
        print RESULTS_PAGE "</BODY>";
        print RESULTS_PAGE "</HTML>";
        close RESULTS_PAGE;
		
		
	}
	print SUMMARY_PAGE "<TD>$exp_text</TD>";
	print SUMMARY_PAGE "</TR>";
	
	
}
