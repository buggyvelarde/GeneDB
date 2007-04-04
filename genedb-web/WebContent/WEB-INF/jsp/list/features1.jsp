<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Gene Results List">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<table width="100%" class="simple">
  <tr align="right">
	<td align="right"><a href="../gviewer.jsp?length=<c:out value="${length}"/>">Click here for GViewer Display</a></td>
  </tr>
</table>

<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<display:table name="features" uid="tmp" pagesize="30" requestURI="/NameFeature" class="simple" cellspacing="0" cellpadding="4">
   	<display:column property="organism.abbreviation" title="Organism"/>
   	<display:column property="cvTerm.name" title="Type"/>
	<display:column property="uniqueName" href="./FeatureByName" paramId="name"/>
</display:table>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<format:footer />