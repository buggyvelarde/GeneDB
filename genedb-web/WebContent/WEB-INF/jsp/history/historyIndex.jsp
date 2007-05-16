<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<format:header name="History">
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>


<c:if test="${empty items}">
<p>There are no entries in your history yet (or your session has expired)</p>
</c:if>
<c:if test="${!empty items}">

<display:table name="items" uid="mainTable" pagesize="30" requestURI="/NamedFeature" class="simple" cellspacing="0" cellpadding="1">
   	<display:column title="Index">${mainTable_rowNum}</display:column>
   	<display:column property="name" title="Name"/>
   	<display:column title="Type">Query</display:column>
   	<display:column property="numberItems" title="No. of results"/>
   	<display:column title="View/Edit">View/Edit</display:column>
   	<display:column title="Download">Download</display:column>
   	<display:column title="Delete">Delete</display:column>
</display:table>


</c:if>
<format:footer />