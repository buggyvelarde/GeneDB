<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Download">
<script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/historyEdit2.js"/>"></script>
</format:header>
<format:page>
<br>

    <!-- Main content part of page -->


<form action="<misc:url value="/Download/${historyItem}"/>" method="POST">

<center>

<format:genePageSection>
<h2>Output Format</h2>
<table width="100%" cellpadding="0" cellspacing="4" border="0" class="sequence-table">
<tr>
<td><input type="radio" name="cust_format" value="TAB" checked onclick="javascript:tabOutputChosen()"> "Tab"-delimited file</td>
<td><input type="radio" name="cust_format" value="HTML" onclick="javascript:htmlOutputChosen()"> HTML Table</td>
<td><input type="radio" name="cust_format" value="FASTA" onclick="javascript:fastaOutputChosen()"> FASTA</td>
</tr>
</table>
</format:genePageSection>

<format:genePageSection>
  <h2><span id="selectFieldTitle">Select fields for tabular columns or FASTA header</span></h2>
  <table width="100%" cellpadding="0" cellspacing="4" border="0" class="sequence-table">
    <tr>
      <td><input type="checkbox" name="cust_field" value="ORGANISM"> Organism</td>
      <td><input type="checkbox" name="cust_field" value="SYS_ID" checked> Systematic ID</td>
      <td><input type="checkbox" name="cust_field" value="GENE_TYPE"> Gene Type</td>
    </tr>
    <tr>
      <td><input type="checkbox" name="cust_field" value="PRIMARY_NAME" > Primary Name</td>
      <td><input type="checkbox" name="cust_field" value="SYNONYMS" > Synonyms</td>
      <td><input type="checkbox" name="cust_field" value="PREV_SYS_ID"> Previous Systematic ID</td>
    </tr>
    <tr>
      <td><input type="checkbox" name="cust_field" value="CHROMOSOME" > Chromosome</td>
      <td><input type="checkbox" name="cust_field" value="LOCATION" > Location (coordinates)</td>
      <td><input type="checkbox" name="cust_field" value="PRODUCT"> Product</td>
    </tr>
    <tr>
      <td><input type="checkbox" name="cust_field" value="EC_NUMBERS" > EC Numbers</td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
  </table>
</format:genePageSection>

<format:genePageSection>
<h2>Select display options</h2>
<table width="100%" cellpadding="0" cellspacing="4" border="0" class="sequence-table">


<tr class="noSequenceOutput tabOutput htmlOutput">
<td>Column Headers:</td>
<td><input type="radio" name="cust_header" value="yes"> Yes</td>
<td><input type="radio" name="cust_header" value="no" checked> No</td>
<td><small>Applies to tab-delimited and HTML formats</small></td>
</tr>

<tr class="sequenceOutput tabOutput noHtmlOutput">
<td>Separator between columns</td>
<td colspan="2"><select name="field_sep">
<option value="default" selected> Default</option>
<option value="tab"> TAB</option>
<option value="|"> |</option>
<option value=","> ,</option>
</select></td>
<td><small>Applies to tab-delimited (default TAB) and FASTA (default |) formats</small></td>
</tr>

<tr class="sequenceOutput tabOutput noHtmlOutput">
<td>Empty columns:</td>
<td colspan="2"><select name="field_blank">
<option value="blank" selected> EMPTY</option>
<option value="-"> -</option>
</select></td>
<td><small>Applies to tab-delimited and FASTA formats</small></td>
</tr>

<tr>
<td>Separator within columns:</td>
<td colspan="2"><select name="field_intsep">

<option value="," selected> ,</option>
<option value="|"> |</option>
</select></td>
<td><small>Applies to all formats</small></td>
</tr>
</table>
</format:genePageSection>

<div class="sequenceOutput noTabOutput noHtmlOutput">
<format:genePageSection>
<h2>Sequence type for FASTA option</h2>
<table width="100%" cellpadding="0" cellspacing="4" border="0" class="sequence-table">
<tr><td colspan="2"><INPUT TYPE=RADIO NAME="sequenceType" checked VALUE="UNSPLICED_DNA"> DNA (Unspliced sequence of CDS) or sequenced EST</td></tr>
<tr><td colspan="2"><INPUT TYPE=RADIO NAME="sequenceType" VALUE="SPLICED_DNA"> DNA (Spliced sequence)</td></tr>
<tr><td colspan="2"><INPUT TYPE=RADIO NAME="sequenceType" VALUE="INTRON_AND_EXON"> Exon and intron sequence (exons capitalized)</td></tr>
<tr><td colspan="2"><INPUT TYPE=RADIO NAME="sequenceType" VALUE="PROTEIN"> Protein sequence</td></tr>
<tr><td><INPUT TYPE=RADIO NAME="sequenceType" VALUE="INTERGENIC_5"> Intergenic Sequence (5'&nbsp;)</td>
<td rowspan="2" align="left">
<input type="hidden" name="includeRNA" value="false">
&nbsp;&nbsp;&nbsp;Number of bases:
<select name="primeX" >
<option>20</option>

<option>50</option>
<option>100</option>
<option>150</option>
<option>200</option>
<option>300</option>
<option>500</option>
<option>1000</option>
<option>2000</option>
<option value="-1">To next CDS/RNA</option>

</select>
</td></tr>

<tr>
<td>
<input type="hidden" name="includeRNA" value="false">
<INPUT TYPE=RADIO NAME="sequenceType" VALUE="INTERGENIC_3"> Intergenic Sequence (3' )</td>
</tr>

<tr><td><INPUT TYPE=RADIO NAME="sequenceType" VALUE="INTERGENIC_3and5">CDS/RNA with 5'/3' flanking sequence</td>
<td align="left">
<input type="hidden" name="includeRNA" value="true">
&nbsp;&nbsp;&nbsp;5' distance:
<select name="prime5" >
<option>0</option>

<option>20</option>
<option>50</option>
<option>100</option>
<option>150</option>
<option>200</option>
<option>300</option>
<option>500</option>
<option>1000</option>
<option>2000</option>

<option value="-1"> To next CDS/RNA</option>
</select>
&nbsp;&nbsp;&nbsp;3' distance:
<select name="prime3" >
<option>0</option>
<option>20</option>
<option>50</option>
<option>100</option>
<option>150</option>
<option>200</option>

<option>300</option>
<option>500</option>
<option>1000</option>
<option>2000</option>
<option value="-1">To next CDS/RNA</option>
</select>
</td></tr>
</table>
</format:genePageSection>
</div>

<format:genePageSection>
<h2>Output Destination</h2>
<table width="100%">
<tr>
<td><input type="radio" name="output_dest" value="TO_BROWSER" checked>&nbsp;Normal page</td>
</tr>
<tr>
<td><input type="radio" name="output_dest" value="TO_FILE">&nbsp;Save as...</td>
</tr>
<tr>
<td><input type="radio" name="output_dest" value="TO_EMAIL">&nbsp;Email to <input type="text" name="email"></td>
</tr>
</table>
</format:genePageSection>

<format:genePageSection>
<table width="100%" cellpadding="0" cellspacing="4" border="0" class="sequence-table">
<tr>
<td>&nbsp;</td>
<td><input type="submit"></td>
<td>&nbsp;</td>

<td><input type="reset"></td>
<td>&nbsp;</td>
</tr>
</table>
</format:genePageSection>

<p>&nbsp;</p><p>&nbsp;</p>
<format:genePageSection>
<h2>Notes</h2>
<div align="left">
<ol>
<li><a name="locnote">Location field:</a> For FASTA options the locations will be the genomic coordinates.</li>
<li><a name="delimeters">Delimiters:</a> The default column separators are the recommended standard. When changing them, bear in mind (i) that the two should differ and (ii) some chosen download fields may contain the delimiter eg. a product may contain a comma.</li>
<li><a name="intra-delimiter">Separator within columns:</a> This denotes the character used to separate items in columns which may contain more than one value eg. synonym.</li>
</ol></div>
</format:genePageSection>
</center>
</form>
</format:page>
