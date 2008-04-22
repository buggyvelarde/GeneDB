<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:headerRound title="Gene Results List">
	<st:init />
	<script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-base.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-all.js"/>"></script>
	 <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/extjs/ext-all.css"/>" />
	<script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-history.js"/>"></script>
</format:headerRound>
<table width="100%">
<tr>
	<td width="20%">
		<format:searchOptions/>
		<div class="fieldset">
		<div class="legend">History</div>
			<br>
			<div id="topic-grid"></div>
		</div>
	</td>
	<td width="80%">
		<div class="fieldset" align="center" style="width: 98%;">
		<div class="legend">Results</div>
			 <p><a href="<misc:history />">Store these results in my history</a></p> 
			<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
			<display:table name="results" uid="tmp" pagesize="30" requestURI="/NamedFeature" class="simple" cellspacing="0" cellpadding="4">
   				<display:column property="organism" title="Organism"/>
   				<display:column property="type" title="Type"/> 
				<display:column property="name" href="./Search/FeatureByName" paramId="name"/>
			</display:table>
	</td>
</tr>
</table>
<format:footer />