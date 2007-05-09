<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<format:header name="GeneDB">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>

<p>  

    <!-- Main content part of page -->
  
    <table align="center" border="0" cellspacing="0"
      cellpadding="2" width="90%">
      
      <tr bgcolor="navy">
	<td align="center" colspan="5">
	  <font face="Arial,Helvetica,Geneva,sans-serif"><FONT color="white"><B>Database Entry Point</B></FONT></font>
	</TD>
      </tr>
      <tr>
	<td align="center" colspan="5">&nbsp;</TD>
      </tr>
      
      <tr>
	<TD bgcolor="navy" align="center"><font face="Arial,Helvetica,Geneva,sans-serif"><FONT color="white"><B>Searches</B></FONT></font></TD>
        <td>&nbsp;</td>
	<TD bgcolor="navy" align="center" width="33%"><font face="Arial,Helvetica,Geneva,sans-serif"><FONT color="white"><B>Sequence Searches</B></FONT></font></TD>
        <td>&nbsp;</td>
	<TD bgcolor="navy" align="center" width="33%"><font face="Arial,Helvetica,Geneva,sans-serif"><FONT color="white"><B>Datasets</B></FONT></font></TD>
      </tr>
      






<!-- Column 1 -->

      <TR>
	<TD bgcolor="#FAFAD2" align="center">
    <form action="NamedFeature" method="GET" name="ohm1">
	<br>
	  <b>Search for</b>
	  <br><b>gene by</b>
	  <br><b>ID/description in</b>
	  <br><db:simpleselect />
	<br><input type="text" size="14" name="name">
        <br>&nbsp;
	<br><input type="checkbox" checked name="desc" value="yes">Include description in search
	<br><input type="checkbox" name="addWildcard" value="yes">Add wildcards to search term
	<br>&nbsp;
	<br><input type="submit" name="submit" value="Search">&nbsp&nbsp;&nbsp;<input type=reset>
</form>
</td>


<!-- Column 2 -->
        <td>&nbsp;</td>

<!-- Column 3 -->
<td  bgcolor="#FAFAD2" align="center">Yep, we'll still offer BLAST searches</td>


<!-- Column 4 -->
        <td>&nbsp;</td>


<!-- Column 5 -->

          <TD bgcolor="#FAFAD2" align="center">
<b><db:simplehomepageselect top="Fungi"/>
<br><db:simplehomepageselect top="Protozoa"/>
<br><db:simplehomepageselect top="Helminths"/>
<br><db:simplehomepageselect top="bacteria" />
<br><b>Parasite Vectors</b>
<br><b>Viruses</b>

	</TD>
</tr>


      <tr><td colspan="5">&nbsp;</td></tr>

      <tr>
	<td colspan="5" align="left" bgcolor="#FAFAD2">
	  <b><font face="Arial,Helvetica,Geneva">Go to our <a
href="/BrowseCategory">browsable list page</a>, <a href="/???">full-text search page</a>, <a
href="/gusapp/Complex">complex querying page</a>, <a
href="/amigo/perl/go.cgi">AmiGO</a> or <a
href="/genedb/History">History/Custom Downloads</a></font></b>
	</TD>
      </TR>

    <tr><td colspan="5">&nbsp;</td></tr>
    
<!--      <tr bgcolor="#FAFAD2"">
	<td align="center" colspan="5">
	  <font face="Arial,Helvetica,Geneva,sans-serif"><B>Version 3</B></FONT></font>
        </td></tr>
      <tr bgcolor="#FAFAD2"">
	<td colspan="5">
<ul>
<li>9 new organisms added: <a href="/genedb/tcruzi/"><i>T.&nbsp;cruzi</a>, <a href="/genedb/tvivax/"><i>T.&nbsp;vivax</i></a>, <a href="/genedb/bronchi/"><i>B.&nbsp;bronchiseptica</i></a>, <a href="/genedb/parapert/"><i>B.&nbsp;parapertussis</i></a>, <a href="/genedb/pert/"><i>B.&nbsp;pertussis</i></a>, <a href="/genedb/pberghei/"><i>P.&nbsp;berghei</i></a>, <a href="/genedb/pchabaudi/"><i>P.&nbsp;chabaudi</i></a>, <a href="/genedb/diphtheria"><i>C. diphtheriae</i></a> and <a href="/genedb/annulata/"><i>T.&nbsp;annulata</i></a>
</ul>
	</TD>
      </tr>
    <tr><td colspan="5">&nbsp;</td></tr>
-->
  </table>
    
    
    <table align="center" border="0" cellspacing="0" cellpadding="2" width="90%">
      
      <tr>
	<td align="center" bgcolor="navy">
	  <font face="Arial,Helvetica,Geneva,sans-serif"><FONT color="white"><B>Information</B></FONT></font>
	</TD>
	<td>&nbsp;</td>
	<td align="center" bgcolor="navy" colspan="2">
	  <font face="Arial,Helvetica,Geneva,sans-serif"><FONT color="white"><B>Links</B></FONT></font>
	</TD>
      </tr>
      
      <TR>
	<TD bgcolor="#FAFAD2">
	  <a href="/genedb/navHelp.jsp">Guide to GeneDB</a>
	</TD>
	<td>&nbsp;</td>
	<TD bgcolor="#FAFAD2">PSU Sequencing Projects</TD>
	<TD bgcolor="#FAFAD2">
	  <A href="http://www.sanger.ac.uk/Projects/Microbes/">Prokaryotes</A>
	</TD>
      </TR>
      
      <TR>
	<TD bgcolor="#FAFAD2">
	  &nbsp;&nbsp;&nbsp;What is GeneDB, and what's in it?
	</TD>
	<td>&nbsp;</td>
	<TD bgcolor="#FAFAD2">&nbsp;</TD>
	<TD bgcolor="#FAFAD2">
	  <A href="http://www.sanger.ac.uk/Projects/Protozoa/">Eukaryotes (Protozoa)</A>
	</TD>
      </TR>
      
      <TR>
	<TD bgcolor="#FAFAD2">
	&nbsp;&nbsp;&nbsp;Navigating/Searching GeneDB
	</TD>
	<td>&nbsp;</td>
	<TD bgcolor="#FAFAD2">&nbsp;</TD>
	<TD bgcolor="#FAFAD2">
	  <A href="http://www.sanger.ac.uk/Projects/Fungi/">Eukaryotes (Fungi)</A>
	</TD>
      </TR>
      
      <TR>
	<TD bgcolor="#FAFAD2">
	  &nbsp;&nbsp;&nbsp;Contacting Us/Feedback
	</TD>
	<td>&nbsp;</td>
	<TD bgcolor="#FAFAD2">Software</TD>
	<TD bgcolor="#FAFAD2">
	  <A href="http://www.sanger.ac.uk/Software/ACT">ACT</A>
	</TD>
      </TR>
      
      <TR>
	<TD bgcolor="#FAFAD2">
	  &nbsp;&nbsp;&nbsp;Privacy Policy
	</TD>
	<td>&nbsp;</td>
	<TD bgcolor="#FAFAD2">&nbsp;</TD>
	<TD bgcolor="#FAFAD2">
	  <A href="http://www.sanger.ac.uk/Software/Artemis/">Artemis</A>
	</TD>
      </TR>
      
      
      <TR>
	<TD bgcolor="#FAFAD2">
	  <a href="/genedb/generalPolicy.jsp">Data Release Policy</a>
	</TD>
	<td>&nbsp;</td>
	<TD bgcolor="#FAFAD2">&nbsp;</TD>
	<TD bgcolor="#FAFAD2">&nbsp;</TD>
      </TR>
      

    
      <tr>
	<td colspan="4">

<p>&nbsp;<br>The GeneDB project is a core part of the Sanger Institute Pathogen
Sequencing Unit's (PSU) activities. Its primary goals are:
<ul>
 <li>to provide reliable access to the latest sequence data and
annotation/curation for the whole range of organisms sequenced by the PSU.
 <li>to develop the website and other tools to aid the community in accessing
and obtaining the maximum value from this data.  
</ul>

<p>GeneDB currently provides <a
href="/genedb/help/allOrgsList.jsp">access to 37 genomes</a>, 
from various stages of the sequencing curation pipeline, from early access to partial genomes with
automatic annotation through to complete genomes with extensive manual curation.
<!--We plan to add another 15 organisms over the next 6 months.--> (Details correct as
of May 2006)
                
<p><b>Note:</b>This site (data and/or code) is updated approximately
weekly. If things do not appear to be working - let us know, and we will try
and fix them. Please see our <a href="/genedb/help/linking.jsp">help page before
creating links</a> to our site. If you have any suggestions or requests about
the site please <a href="/genedb/feedback.jsp">contact the technical team</a>

	  
	</td></tr>
    </table>
    <p>
      
    <format:footer />
    