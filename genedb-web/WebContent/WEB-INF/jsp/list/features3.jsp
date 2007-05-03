<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Gene Results List">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">

<display:table name="results" uid="tmp" id="row" pagesize="30" requestURI="${controllerPath}" class="simple" cellspacing="0" cellpadding="4">
	<display:column property="uniqueName" title="Id" href="NamedFeature?organism=${row.organism.abbreviation}" paramId="name"/>
	<display:column title="dummy">${_TNM}</display:column>
   	<display:column property="organism.abbreviation" title="Organism"/>
   	<display:column property="cvTerm.name" title="Type"/>

</display:table>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<format:footer />