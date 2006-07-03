<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Gene Results List">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<sp:form commandName="ResultBean">
<sp:select path="results" id="result">
<sp:options items="${result.organism.abbreviation}"/>
</sp:select>
</sp:form>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<p>Results</p>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<display:table name="results" id="result" pagesize="30" requestURI="/NamedFeature" class="simple" cellspacing="0" cellpadding="4">
	     	<display:column property="organism.abbreviation" title="Organism" class="fonts"/>
			<display:column property="cvTerm.name" title="Type"/>
			<display:column property="uniquename" href="./Search/FeatureByName?name=${result.uniquename}"/>
</display:table>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<format:footer />