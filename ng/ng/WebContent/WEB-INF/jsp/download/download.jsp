<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<misc:url value="/" var="base"/>
<format:headerRound title="Download List" onLoad="initDownload('${base}','${history}')">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/yui/build/fonts/fonts-min.css"/>" />
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/yui/build/datatable/assets/skins/sam/datatable.css"/>" />
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/yui/build/button/assets/skins/sam/button.css"/>" />
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/connection/connection-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/animation/animation-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/dragdrop/dragdrop-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/json/json-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/element/element-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/button/button-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/datasource/datasource-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/get/get-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/datatable/datatable-min.js"/>"></script>
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/genePage.css"/>" />
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/download.js"/>"></script>



14
15  <!-- OPTIONAL: Get Utility (enables dynamic script nodes for DataSource) -->
16  <script type="text/javascript" src="http://yui.yahooapis.com/2.7.0/build/get/get-min.js"></script>
20
21  <!-- OPTIONAL: Calendar (enables calendar editors) -->
22  <script type="text/javascript" src="http://yui.yahooapis.com/2.7.0/build/calendar/calendar-min.js"></script>


</format:headerRound>
<br>
<form:form id="errors" action="DownloadFeatures" commandName="download" method="get">
			<table>
				<tr>
					<td><form:errors path="*" /></td>
				</tr>
			</table>
</form:form>
<div id="third" align="center">
	<fieldset>
		<legend>Select Output Format</legend>
		<div id="outputFormat" align="center"></div>
	</fieldset>
</div>
<br>
<div id="second" align="center">
	<fieldset>
		<legend>
			Click on the buttons to show/hide columns
		</legend>
		<div class="workarea">
		  <ul id="ul-buttons">
		    <li id="org" class="buttons"><input id="organism.commonName" type="checkbox" name="organism" value="Organism" checked></li>
		    <li id="nam" class="buttons"><input id="uniqueName" type="checkbox" name="name" value="Systematic ID" checked></li>
		    <li id="syn" class="buttons"><input id="synonym" type="checkbox" name="synonym" value="Synonym" checked></li>
			<li id="chro"class="buttons" ><input id="chr" type="checkbox" name="chromosome" value="Chromosome" checked></li>
			<li id="loc" class="buttons"><input id="locs" type="checkbox" name="location" value="Location" checked></li>
			<li id="pro" class="buttons"><input id="product" type="checkbox" name="product" value="Product" checked></li>
			<li id="seq" class="buttons">
				<input type="submit" id="sequence" name="sequence" value="Sequence" disabled="disabled">
				<select id="sequenceselect" name="sequenceselect" multiple>
				    <option value="DNA (Unspliced sequence of CDS) or sequenced EST">UNSPLICED_DNA</option>
				    <option value="DNA (Spliced sequence)">SPLICED_DNA</option>
				    <option value="Intron sequence">INTRON</option>
					<option value="Protein sequence">PROTEIN</option>
					<option value="Intergenic Sequence (5' )">INTERGENIC_5</option>
					<option value="Intergenic Sequence (3' )">INTERGENIC_3</option>
					<option value="CDS/RNA with 5'/3' flanking sequence">CDS_RNA</option>
				</select>
			</li>
		  </ul>
		</div>
<!--		<div id="buttons" align="center" style="padding:5px;"></div>-->
		<div id="sequence" align="center" style="padding:5px;"></div>
		<div id="seqMenu" align="center"></div>
	</fieldset>
</div>
<br>
<div id="first" align="center">
	<fieldset>
		<legend>
			Drag the columns to arrange them
		</legend>
		<div id="download" align="center"></div>
	</fieldset>
</div>
<br>
<br>
<div id="submitButton" align="center"></div>
<br>
<div id="container" align="center"></div>
<format:footer/>
