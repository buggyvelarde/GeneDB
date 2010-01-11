<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="misc" uri="misc" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:headerRound title="Search Results">
	<st:init />
	<link rel="stylesheet" href="<misc:url value="/"/>includes/style/alternative.css" type="text/css"/>
	<link rel="stylesheet" href="<misc:url value="/"/>includes/style/wtsi.css" type="text/css"/>
	<link rel="stylesheet" href="<misc:url value="/"/>includes/style/frontpage1.css" type="text/css"/>
	<script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/ext-base.js"/>"></script>
    <script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/ext-all.js"/>"></script>
	 <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/extjs/ext-all.css"/>" />
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
		<div class="legend">Results</div>
			<p><a href="<misc:history />">Store these results in my history</a></p>
			<img src="<misc:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
			<display:table name="results" uid="tmp" pagesize="30" requestURI="/NameSearch" class="simple" cellspacing="0" cellpadding="4">
   				<display:column property="organism" title="Organism"/>
   				 <display:column property="type" title="Term"/>
   				 <display:column property="value" title="Value"/>
				<display:column property="feature" href="./Search/FeatureByName" paramId="name"/>
			</display:table>
	</td>
</tr>
</table>
<format:footer />