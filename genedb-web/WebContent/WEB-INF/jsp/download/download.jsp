<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:url value="/" var="base"/>
<format:headerRound title="Download List" onLoad="initDownload('${base}','${history}')">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/YUI-2.5.2/fonts/fonts-min.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/YUI-2.5.2/datatable/assets/skins/sam/datatable.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/YUI-2.5.2/button/assets/skins/sam/button.css"/>" />
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/connection/connection-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/dragdrop/dragdrop-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/json/json-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/element/element-beta-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/button/button-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/datasource/datasource-beta-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/datatable/datatable-beta-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/connection/connection-min.js"/>"></script>
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/genedb/download.js"/>"></script>
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
		<div id="buttons" align="center">
		</div>
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