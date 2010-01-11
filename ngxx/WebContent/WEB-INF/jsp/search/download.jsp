<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<script src="<misc:url value="/includes/scripts/script.aculo.us/prototype.js"/>" type="text/javascript"></script>
<script src="<misc:url value="/includes/scripts/script.aculo.us/scriptaculous.js"/>" type="text/javascript"></script>
<style type="text/css">.infoMacro { border-style: solid; border-width: 1px; border-color: #c0c0c0; background-color: #ffffff; text-align:left;}.informationMacroPadding { padding: 5px 0 0 5px; }</style>
<script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/ext-base.js"/>"></script>     <!-- ENDLIBS -->
    <script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/ext-all.js"/>"></script>
	 <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/ext-all.css"/>" />
	 <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/grid.css"/>" />
	<script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/download.js"/>"></script>
	<style id="topic-grid-cssrules" type="text/css">
		#topic-grid .x-grid-col-topic {
		white-space:normal;width:489px;
		}
		#topic-grid .x-grid-hd-topic {
		width:489px;}
		#topic-grid .x-grid-td-topic {

		}
		#topic-grid .x-grid-split-topic {

		}
		#topic-grid .x-grid-col-1 {
		width:99px;
		}
		#topic-grid .x-grid-hd-1 {
		width:99px;}
		#topic-grid .x-grid-td-1 {
		display:none;
		}
		#topic-grid .x-grid-split-1 {
		display:none;
		}
		#topic-grid .x-grid-col-last {
		width:149px;
		}
		#topic-grid .x-grid-hd-last {
		width:149px;}
		#topic-grid .x-grid-td-last {

		}
		#topic-grid .x-grid-split-last {

		}
	</style>
<script type="text/javascript">
<!--
function hideSection(sectionId) {
  	document.getElementById(sectionId).style.display='none';
  	document.getElementById(sectionId).style.visibility='hidden';
}

function showSection(sectionId) {
  	document.getElementById(sectionId).style.display='block';
  	document.getElementById(sectionId).style.visibility='visible';
}
//-->
</script>

<format:header title="History Download">
	<st:init />
	<link rel="stylesheet" href="<misc:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>


<p>Please note: not all options shown are possible depending upon which output format
is chosen (further below).

<form:form commandName="downloadOptions" action="DownloadFeatures" method="POST">
<form:hidden path="historyItem" />
<form:hidden path="version" />

<div class='informationMacroPadding' align="center">
<table cellpadding='5' width='90%' cellspacing='0' class='infoMacro' border='0'>
<tr><td>
<h3>Select data for download</h3>
<table width="100%" border="0">
<tr bgcolor="FAFAD2">
<td width="33%"><form:checkbox path="outputOption" value="ORGANISM"/>Organism<font color="red">(Not implemented)</font></td>
<td width="33%"><form:checkbox path="outputOption" value="SYS_ID"/>Systematic ID<font color="red">(Not implemented)</font></td>
<td width="33%"><form:checkbox path="outputOption" value="PRIMARY_NAME"/>Primary Name<font color="red">(Not implemented)</font></td>
</tr>
<tr>
<td width="33%"><form:checkbox path="outputOption" value="PRODUCT"/>Product<font color="red">(Not implemented)</font></td>
<td width="33%"><form:checkbox path="outputOption" value="SYNONYMS"/>Synonyms<font color="red">(Not implemented)</font></td>
<td width="33%"><span id="fieldOptionsOpener"><a href="javascript:showSection('fieldOptions');hideSection('fieldOptionsOpener')">See more options</a></span></td>
</tr>
</table>
<div id="fieldOptions" style="visibility: hidden; display: none">
<table width="100%">
<tr bgcolor="FAFAD2">
<td width="33%"><form:checkbox path="outputOption" value="PREV_SYS_ID"/>Previous Systematic ID<font color="red">(Not implemented)</font></td>
<td width="33%"><form:checkbox path="outputOption" value="CHROMOSOME"/>Chromosome<font color="red">(Not implemented)</font></td>
<td width="33%"><form:checkbox path="outputOption" value="LOCATION"/>Location (coordinates)<font color="red">(Not implemented)</font></td>
</tr>
<tr>
<td width="33%"><form:checkbox path="outputOption" value="EC_NUMBERS"/>EC Numbers<font color="red">(Not implemented)</font></td>
<td width="33%"><form:checkbox path="outputOption" value="NUM_TM_DOMAINS"/>No. of TM domains<font color="red">(Not implemented)</font></td>
<td width="33%"><form:checkbox path="outputOption" value="SIG_P"/>Presence of signal peptide<font color="red">(Not implemented)</font></td>
</tr>
<tr bgcolor="FAFAD2">
<td width="33%"><form:checkbox path="outputOption" value="GPI_ANCHOR"/>Presence of GPI anchor<font color="red">(Not implemented)</font></td>
<td width="33%"><form:checkbox path="outputOption" value="MOL_WEIGHT"/>Molecular weight<font color="red">(Not implemented)</font></td>
<td width="33%"><form:checkbox path="outputOption" value="ISOELECTRIC_POINT"/>Isoelectric point<font color="red">(Not implemented)</font></td>
<tr>
<td width="33%"><form:checkbox path="outputOption" value="GO_IDS"/>GO IDs<font color="red">(Not implemented)</font></td>
<td width="33%"><form:checkbox path="outputOption" value="PFAM_IDS"/>Pfam IDs<font color="red">(Not implemented)</font></td>
<td width="33%"><form:checkbox path="outputOption" value="INTERPRO_IDS"/>Interpro IDs<font color="red">(Not implemented)</font></td>
</tr>
</table>
<span id="fieldOptionsOpener"><a href="javascript:showSection('fieldOptionsOpener');hideSection('fieldOptions')">Hide extra options</a></span>
</div>
<p><b>Sequence Options</b>
<table width="100%">
<tr><td><form:checkbox path="outputOption" value="SEQ_PROTEIN"/>Protein sequence<font color="red">(Not implemented)</font></td></tr>
<tr><td><form:checkbox path="outputOption" value="SEQ_UNSPLICED"/>Nucleotide sequence of CDS (and introns)<font color="red">(Not implemented)</font></td></tr>
</table>
<div id="sequenceOptionsOpener"><a href="javascript:showSection('sequenceOptions');hideSection('sequenceOptionsOpener')">Extra sequence options</a></div>
<div id="sequenceOptions" style="visibility: hidden; display: none">
<table width="100%">
<tr><td colspan="3"><form:checkbox path="outputOption" value="SEQ_SPLICED"/>Nucleotide (without introns)<font color="red">(Not implemented)</font></td></tr>
<tr><td colspan="3"><form:checkbox path="outputOption" value="SEQ_5P_UTR"/>5' UTR<font color="red">(Not implemented)</font></td></tr>
<tr><td colspan="3"><form:checkbox path="outputOption" value="SEQ_3P_UTR"/>3' UTR<font color="red">(Not implemented)</font></td></tr>
<tr bgcolor="FAFAD2"><td><form:checkbox path="outputOption" value="SEQ_5P_INTERGENIC"/>Intergenic Sequence (5'&nbsp;)<font color="red">(Not implemented)</font></td>
<td rowspan="3" align="left">
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

<tr bgcolor="FAFAD2">
<td>
<input type="hidden" name="includeRNA" value="false">
<form:checkbox path="outputOption" value="SEQ_3P_INTERGENIC"/>Intergenic Sequence (3' )<font color="red">(Not implemented)</font></td>
</tr>

<tr><td><form:checkbox path="outputOption" value="SEQ_INTERGENIC"/>CDS/RNA with 5'/3' flanking sequence<font color="red">(Not implemented)</font></td>

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
<option value="-1">To next CDS/RNA</option>
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
<tr bgcolor="FAFAD2"><td colspan="3"><form:checkbox path="outputOption" value="SEQ_INTRON"/>Intron sequence<font color="red">(Not implemented)</font></td></tr>
<tr><td><a href="javascript:showSection('sequenceOptionsOpener');hideSection('sequenceOptions')">Hide extra sequence options</a></td></tr></table>
</td></tr></table></div>

<div class='informationMacroPadding' align="center">
<table cellpadding='5' width='90%' cellspacing='0' class='infoMacro' border='0'>
<tr><td>
<h3>Output Format</h3>
<table width="100%">
<tr>
<td><form:radiobutton path="outputFormat" value="CSV" />"Tab"-delimited file<font color="red">(Not implemented)</font></td>
<td><form:radiobutton path="outputFormat" value="EXCEL" />Excel (.XLS) format<font color="red">(Not implemented)</font></td>
<td><form:radiobutton path="outputFormat" value="ODF" />OpenDocument spreadsheet format<font color="red">(Not implemented)</font></td>
</tr>
<tr>
<td><form:radiobutton path="outputFormat" value="HTML" />HTML Table<font color="red">(Not implemented)</font></td>
<td><form:radiobutton path="outputFormat" value="GO_ASSOC" />GO association file<font color="red">(Not implemented)</font></td>
<td><form:radiobutton path="outputFormat" value="FASTA" />FASTA<font color="red">(Not implemented)</font></td>
</tr>
</table>
</td></tr></table></div>


<div class='informationMacroPadding' align="center">
<table cellpadding='5' width='90%' cellspacing='0' class='infoMacro' border='0'>
<tr><td>
<h3>Select display options</h3>
<p id="displayOptionsOpener">Sensible defaults have been chosen for this section. If you want fine-control
of the output separators etc, <a href="javascript:showSection('displayOptions');hideSection('displayOptionsOpener')">see options</a></p>

<div id="displayOptions" style="visibility: hidden; display: none">
<p><a href="javascript:showSection('displayOptionsOpener');hideSection('displayOptions')">Hide this section</a></p>
<table width="100%">
<tr bgcolor="FAFAD2">
<td>Column Headers:<font color="red">(Not implemented)</font></td>

<td><input type="radio" name="cust_header" value="yes">Yes</td>
<td><input type="radio" name="cust_header" value="no" checked>No</td>
<td><small>Applies to tab-delimited and HTML formats</small></td>
</tr>
<tr>
<td>Separator between columns<font color="red">(Not implemented)</font></td>
<td colspan="2"><select name="field_sep">
<option value="default" selected>Default</option>
<option value="tab">TAB</option>
<option value="|">|</option>

<option value=",">,</option>
</select></td>
<td><small>Applies to tab-delimited (default TAB) and FASTA (default |) formats</small></td>
</tr>
<tr bgcolor="FAFAD2">
<td>Empty columns:<font color="red">(Not implemented)</font></td>
<td colspan="2"><select name="field_blank">
<option value="blank" selected>EMPTY</option>
<option value="-">-</option>
</select></td>
<td><small>Applies to tab-delimited and FASTA formats</small></td>

</tr>
<tr>
<td>Separator within columns:<font color="red">(Not implemented)</font></td>
<td colspan="2"><select name="field_intsep">
<option value="," selected>,</option>
<option value="|">|</option>
</select></td>
<td><small>Applies to all formats</small></td>
</tr>
</table>
</div>
</td></tr></table></div>

<div class='informationMacroPadding' align="center">
<table cellpadding='5' width='90%' cellspacing='0' class='infoMacro' border='0'>
<tr><td>
<h3>Output destination</h3>
<p>Display in the browser window is the default output destination. The "Save as..." option will force your browser to save the file directly.

<P>An option to have download results e-mailed directly, is included.</p>
<table width="100%">
<tr bgcolor="FAFAD2">

<td><form:radiobutton path="outputDestination" value="TO_BROWSER" />&nbsp;To Browser</td>
</tr>
<tr>
<td><form:radiobutton path="outputDestination" value="TO_FILE" />&nbsp;To File...<font color="red">(Not implemented)</font></td>
</tr>
<tr bgcolor="FAFAD2">
<td><form:radiobutton path="outputDestination" value="TO_EMAIL" />&nbsp;To e-mail<font color="red">(Not implemented)</font></td>
</tr>
</table>
</td></tr></table></div>

<div class='informationMacroPadding' align="center">
<table cellpadding='5' width='90%' cellspacing='0' class='infoMacro' border='0'>

<tr><td>
<table width="100%">
<tr>
<td>&nbsp;</td>
<td><input type="submit"></td>
<td>&nbsp;</td>
<td><input type="reset"></td>
<td>&nbsp;</td>
</tr>
</table>
</td></tr></table></div>


<p>&nbsp;</p><p>&nbsp;</p>
<div class='informationMacroPadding' align="center">
<table cellpadding='5' width='90%' cellspacing='0' class='infoMacro' border='0'>
<tr><td>

<h3>Notes</h3>
<ol>
<li><a name="locnote">Location field:</a> For FASTA options the locations will
be the genomic coordinates.</li>
<li><a name="orth_pasteback">Orthologue pasteback:</a> This option returns the List Download form with the input IDs plus the IDs of all orthologues for those IDs.</li>
<li><a name="orthologue">Orthologues:</a> Orthologue datasets are currently only available for some organisms in GeneDB.</li>
<li><a name="delimeters">Delimiters:</a> The default column separators are the recommended standard. When changing them, bear in mind (i) that the two should differ and (ii) some chosen download fields may contain the delimiter eg. a product may contain a comma.</li>

<li><a name="intra-delimiter">Separator within columns:</a> This denotes the character used to separate items in columns which may contain more than one value eg. synonym.</li>
</ol>
</td></tr></table></div>


</form:form>

<format:footer />