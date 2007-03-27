<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<format:header name="Category List">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<display:table name="results" uid="tmp" pagesize="30" requestURI="/BrowseCategory" class="simple" cellspacing="0" cellpadding="4">
   	<display:column property="name" title="Category" href="./BrowseTerm?organism=bacteria&category=RILEY" paramId="term"/>
   	<display:column property="count" title="Count" />
</display:table>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<format:footer />