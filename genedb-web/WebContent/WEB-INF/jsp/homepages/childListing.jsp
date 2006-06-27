<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header>Welcome to the GeneDB website<br />Version 4.0</format:header>

<p>  

</p>
<p><a href="<c:url value="/feature.html"/>">Look at gene page</a></p>
<p><a href="http://localhost:8080/genedb-web/genome/Trypanasoma_brucei_brucei/current">Common URL</a></p>
<p><a href="<c:url value="/dropDown.html"/>">Query drop-down</a></p>
<p><a href="<c:url value="/example-working?taxId=12345"/>">Boolean query page</a></p>
<p><a href="<c:url value="/pfam.html"/>">Pfam auto-complete</a></p>
<p><a href="<c:url value="/dwr/test/PfamLookup"/>">DWR Pfam page</a></p>


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
    <form action="/genedb/Dispatcher" method="GET" name="ohm1">
    <input type="hidden" name="formType" value="navBar">
    <br>
      <b>Search for</b>
      <br><b>gene by</b>
      <br><b>ID/description in</b>
      <br><select name="organism">
        <option value="All:*">All organisms
          <optgroup label="Fungi">
        <option value="asp">A. fumigatus
        <option value="cerevisiae">S. cerevisiae
        <option value="pombe">S. pombe
       </optgroup>
       <optgroup label="Protozoa">
         <option value="dicty">D. discoideum
         <option value="ehistolytica">E. histolytica
         <option value="etenella">E. tenella
         <option value="linfantum">L. infantum
         <option value="leish">L. major
         <option value="pberghei">P. berghei
         <option value="pchabaudi">P. chabaudi
         <option value="malaria">P. falciparum
         <option value="pknowlesi">P. knowlesi
         <option value="annulata">T. annulata
         <option value="tryp">T. brucei
         <option value="tcongolense">T. congolense
         <option value="tcruzi">T. cruzi
         <option value="tgambiense">T. b. gambiense
         <option value="tvivax">T. vivax
       </optgroup>
       <optgroup label="Parasitic Helminths">
         <option value="smansoni">S. mansoni
       </optgroup>
       <optgroup label="Bacteria">
                <option value="bronchi">B. bronchiseptica
                <option value="bfragilis">B. fragilis
                <option value="parapert">B. parapertussis
                <option value="pert">B. pertussis
                <option value="bpseudomallei">B. pseudomallei
                <option value="cabortus">C. abortus
                <option value="diphtheria">C. diphtheriae
                <option value="ecarot">E. carotovora
                <option value="saureusMRSA">S. aureus MRSA
                <option value="saureusMSSA">S. aureus MSSA
                <option value="scoelicolor">S. coelicolor
                <option value="styphi">S. typhi
          </optgroup>
          <optgroup label="Parasite Vectors">
                <option value="glossina">G. morsitans
          </optgroup>
          <optgroup label="Viruses">
                <option value="ehuxleyi">E. huxleyi virus 86
          </optgroup>
          <optgroup label="Group options">
        <option value="Yeast:pombe:cerevisiae:asp">Yeast
        <option value="Kinetoplastids:tryp:leish:linfantum">Kinetoplastids
          </optgroup>
      </select>
    <br><input type="text" size="14" name="name">
        <br>&nbsp;
    <br><input type="checkbox" checked name="desc" value="yes">Include description in search
    <br><input type="checkbox" name="wildcard" value="yes">Add wildcards to search term
    <br>&nbsp;
    <br><input type="submit" name="submit" value="Search">&nbsp;&nbsp;&nbsp;<input type=reset>
</form>
</td>


<!-- Column 2 -->
        <td>&nbsp;</td>

<!-- Column 3 -->
        <TD bgcolor="#FAFAD2" align="center" width="33%"><a href="/genedb/seqSearch.jsp">omniBLAST</a>
    <form action="/genedb/Dispatcher" method="GET" name="ohm2">
    <input type="hidden" name="formType" value="navBar">
    <br>(Multi-organism
    <br>BLAST)
        <br>&nbsp;
        <br><hr>
        <br>&nbsp;
    <br><input type="submit" name="pages" value="Go To"><b>&nbsp;single</b>
    <br><b>organism BLAST:</b>
    <br>
 <select name='ohmr'
onChange="document.location.href=document.forms['ohm2'].ohmr.options[document.forms['ohm2'].ohmr.selectedIndex].value">
<option selected value='/'>Choose...</option>
<optgroup label="Fungi">
<option value='/genedb/asp/blast.jsp'>A. fumigatus</option>
<option value='/genedb/cerevisiae/blast.jsp'>S. cerevisiae</option>
<option value='/genedb/pombe/blast.jsp'>S. pombe</option>
</optgroup>
<optgroup label="Protozoa">
<option value='/genedb/dicty/blast.jsp'>D. discoideum</option>
<option value='/genedb/ehistolytica/blast.jsp'>E. histolytica</option>
<option value='/genedb/etenella/blast.jsp'>E. tenella</option>
<option value='/genedb/linfantum/blast.jsp'>L. infantum</option>
<option value='/genedb/leish/blast.jsp'>L. major</option>
<option value='/genedb/pberghei/blast.jsp'>P. berghei</option>
<option value='/genedb/pchabaudi/blast.jsp'>P. chabaudi</option>
<option value='/genedb/malaria/blast.jsp'>P. falciparum</option>
<option value='/genedb/pknowlesi/blast.jsp'>P. knowlesi</option>
<option value='/genedb/annulata/blast.jsp'>T. annulata</option>
<option value='/genedb/tryp/blast.jsp'>T. brucei</option>
<option value='/genedb/tcongolense/blast.jsp'>T. congolense</option>
<option value='/genedb/tcruzi/blast.jsp'>T. cruzi</option>
<option value='/genedb/tgambiense/blast.jsp'>T. b. gambiense</option>
<option value='/genedb/tvivax/blast.jsp'>T. vivax</option>
</optgroup>
<optgroup label="Parasitic Helminths">
<option value='/genedb/smansoni/blast.jsp'>S. mansoni</option>
</optgroup>
<optgroup label="Bacteria">
<option value='/genedb/bronchi/blast.jsp'>B. bronchiseptica</option>
<option value='/genedb/bfragilis/blast.jsp'>B. fragilis</option>
<option value='/genedb/parapert/blast.jsp'>B. parapertussis</option>
<option value='/genedb/pert/blast.jsp'>B. pertussis</option>
<option value='/genedb/pseudomallei/blast.jsp'>B. pseudomallei</option>
<option value='/genedb/cabortus/blast.jsp'>C. abortus</option>
<option value='/genedb/diphtheria/blast.jsp'>C. diphtheriae</option>
<option value="/genedb/ecarot/blast.jsp">E. carotovora</option>
<option value='/genedb/saureusMRSA/blast.jsp'>S. aureus MRSA</option>
<option value='/genedb/saureusMSSA/blast.jsp'>S. aureus MSSA</option>
<option value='/genedb/scoelicolor/blast.jsp'>S. coelicolor</option>
<option value='/genedb/styphi/blast.jsp'>S. typhi</option>
</optgroup>
<optgroup label="Parasite Vectors">
<option value='/genedb/glossina/blast.jsp'>G. morsitans</option>
</optgroup>
<optgroup label="Viruses">
<option value='/genedb/ehuxleyi/blast.jsp'>E. huxleyi</option>
</optgroup>
</select>
</form>
</td>


<!-- Column 4 -->
        <td>&nbsp;</td>


<!-- Column 5 -->

          <TD bgcolor="#FAFAD2" align="center">
    <form action="/genedb/Dispatcher" method="GET" name="ohm3">
    <input type="hidden" name="formType" value="navBar">
        <b>Fungi</b>
        <br><input type="submit" name="fungiHomePage" value="Go To">
                <select name="fungiOrganism"
                 onChange="document.location.href=document.forms['ohm3'].fungiOrganism.options[document.forms['ohm3'].fungiOrganism.selectedIndex].value">
                  <option selected value='/'>Choose...</option>
                  <option value="/genedb/asp/">A. fumigatus
                  <option value="/genedb/cerevisiae/">S. cerevisiae
                  <option value="/genedb/pombe/">S. pombe
                </select>

        <br>&nbsp;
        <br><b>Protozoa</b>
        <br><input type="submit" name="protozoaHomePage" value="Go To">
                <select name="protozoaOrganism"
                        onChange="document.location.href=document.forms['ohm3'].protozoaOrganism.options[document.forms['ohm3'].protozoaOrganism.selectedIndex].value">
                  <option selected value='/'>Choose...</option>
                  <option value="/genedb/dicty/">D. discoideum
                  <option value='/genedb/ehistolytica/'>E. histolytica
                  <option value='/genedb/etenella/'>E. tenella
                  <option value='/genedb/linfantum/'>L. infantum
                  <option value="/genedb/leish/">L. major
                  <option value="/genedb/pberghei/">P. berghei
                  <option value="/genedb/pchabaudi/">P. chabaudi
                  <option value="/genedb/malaria/">P. falciparum
                  <option value='/genedb/pknowlesi'>P. knowlesi
                  <option value="/genedb/annulata/">T. annulata
                  <option value="/genedb/tryp/">T. brucei
                  <option value="/genedb/tcongolense/">T. congolense
                  <option value="/genedb/tcruzi/">T. cruzi
                  <option value="/genedb/tgambiense/">T. b. gambiense
                  <option value="/genedb/tvivax/">T. vivax
                </select>

        <br>&nbsp;
        <br><b>Parasitic Helminths</b>
        <br><input type="submit" name="helminthsHomePage" value="Go To">
                <select name="helminthsOrganism"
                 onChange="document.location.href=document.forms['ohm3'].helminthsOrganism.options[document.forms['ohm3'].helminthsOrganism.selectedIndex].value">
                  <option selected value='/'>Choose...</option>
                  <option value="/genedb/smansoni/">S. mansoni
                </select>

        <br>&nbsp;
        <br><b>Bacteria</b>
        <br><input type="submit" name="bacteriaHomePage" value="Go To">
                <select name="bacteriaOrganism"
                 onChange="document.location.href=document.forms['ohm3'].bacteriaOrganism.options[document.forms['ohm3'].bacteriaOrganism.selectedIndex].value">
                  <option selected value='/'>Choose...</option>
                  <option value='/genedb/bronchi/'>B. bronchiseptica</option>
                  <option value='/genedb/bfragilis/'>B. fragilis</option>
                  <option value='/genedb/parapert/'>B. parapertussis</option>
                  <option value='/genedb/pert/'>B. pertussis</option>
                  <option value='/genedb/bpseudomallei'>B. pseudomallei
                  <option value='/genedb/cabortus/'>C. abortus</option>
                  <option value='/genedb/diphtheria/'>C. diphtheriae</option>
                  <option value='/genedb/ecarot/'>E. carotovora</option>
                  <option value='/genedb/saureusMRSA/'>S. aureus MRSA</option>
                  <option value='/genedb/saureusMSSA/'>S. aureus MSSA</option>
                  <option value='/genedb/scoelicolor/'>S. coelicolor</option>
                  <option value='/genedb/styphi/'>S. typhi</option>
                </select>


        <br>&nbsp;
        <br><b>Parasite Vectors</b>
        <br><input type="submit" name="parasiteVectorsHomePage" value="Go To">
                <select name="parasiteVectorsOrganism"
                 onChange="document.location.href=document.forms['ohm3'].parasiteVectorsOrganism.options[document.forms['ohm3'].parasiteVectorsOrganism.selectedIndex].value">
                  <option selected value='/'>Choose...</option>
                  <option value='/genedb/glossina/'>G. morsitans</option>
                </select>

        <br>&nbsp;
        <br><b>Viruses</b>
        <br><input type="submit" name="virusesHomePage" value="Go To">
                <select name="virusesOrganism"
onChange="document.location.href=document.forms['ohm3'].virusesOrganism.options[document.forms['ohm3'].virusesOrganism.selectedIndex].value">
                  <option selected value='/'>Choose...</option>
                  <option value='/genedb/ehuxleyi/'>E. huxleyi virus 86</option>
                </select>
</form>
    </TD>
</tr>


      <tr><td colspan="5">&nbsp;</td></tr>

      <tr>
    <td colspan="5" align="left" bgcolor="#FAFAD2">
      <b><font face="Arial,Helvetica,Geneva">Go to our <a
href="/genedb/search.jsp">main search page</a>, <a
href="http://www.genedb.org/amigo/perl/go.cgi">AmiGO</a> or our <a
href="/genedb/idListForm.jsp">ID List Download</a></font></b>
    </TD>
      </TR>

    <tr><td colspan="5">&nbsp;</td></tr>

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

<p>GeneDB currently provides <a href="/genedb/help/allOrgsList.jsp">access to 32 genomes</a>, 
from various stages of the sequencing curation pipeline, from early access to partial genomes with
automatic annotation through to complete genomes with extensive manual curation.
We plan to add another 15 organisms over the next 6 months. (Details correct as
of March 2005)
                
<p><b>Note:</b>This site (data and/or code) is updated approximately
weekly. If things do not appear to be working - let us know, and we will try
and fix them. Please see our <a href="/genedb/help/linking.jsp">help page before
creating links</a> to our site. If you have any suggestions or requests about
the site please <a href="/genedb/feedback.jsp">contact the technical team</a>

      
    </td></tr>
    </table>
    
    
<H2><!-- <fmt:message key="welcome"/> -->Welcome To GeneDB</H2>
<P>
<A href="<c:url value="/findOwners.htm"/>">Find owner</A>
<P>
<A href="<c:url value="/vets.htm"/>">Display all veterinarians</A>
<P>
<A href="<c:url value="/html/petclinic.html"/>">Tutorial</A>
<P>
<A href="<c:url value="/docs/index.html"/>">Documentation</A>

    

<format:footer />
