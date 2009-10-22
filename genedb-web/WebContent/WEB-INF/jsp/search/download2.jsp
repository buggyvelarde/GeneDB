<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<format:headerRound title="History Download">
	<st:init />
	<link rel="stylesheet" href="<misc:url value="/"/>includes/style/alternative.css" type="text/css"/>
	<link rel="stylesheet" href="<misc:url value="/"/>includes/style/wtsi.css" type="text/css"/>
	<link rel="stylesheet" href="<misc:url value="/"/>includes/style/frontpage1.css" type="text/css"/>
	<script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/ext-base.js"/>"></script>     <!-- ENDLIBS -->
    <script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/ext-all.js"/>"></script>
	 <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/extjs/ext-all.css"/>" />
	<script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/download.js"/>"></script>
	<script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/ext-history.js"/>"></script>
</format:headerRound>
<table width="100%">
<tr>
	<td width="20%">
		<format:searchOptions/>
		<format:history-small/>
	</td>
	<td width="100%">
		<div class="fieldset" align="center" style="width: 98%;">
		<br>
		<div class="legend">Download</div>
			<div id="download-grid"/>
	</td>
</tr>
</table>
<format:footer />