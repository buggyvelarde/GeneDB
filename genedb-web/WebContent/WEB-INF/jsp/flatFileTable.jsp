<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="misc" uri="misc" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Flat File Gene Results List">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
	<table align="center" border="0" cellspacing="0" cellpadding="0" width="100%">
	<tr><td colspan="3"><img src="<c:url value="/" />/includes/images/blank.gif" width="100%" height="1" alt="--------"/>
	<tr><td colspan="3"><img src="<c:url value="/" />/includes/blank.gif" width="100%" height="1" alt="--------"/>
	</tr>
	</table>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<display:table name="features" uid="tmp" pagesize="30" requestURI="/FlatFileReport" class="simple" cellspacing="0" cellpadding="4">
   	<display:column property="id" title="Id" sortable="true" />
   	<display:column property="product" title="Product" />
   	<display:column property="strand" title="Strand"/>
	<display:column property="min" title="Start" sortable="true" />
	<display:column property="max" title="End" />
	<display:column property="size" title="Size" sortable="true" />
</display:table>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<format:footer />