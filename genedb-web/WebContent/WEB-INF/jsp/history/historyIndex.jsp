<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<format:header name="History">
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>


<c:if test="${empty items}">
<p>There are no entries in your history yet (or your session has expired). <a href="/History/FillHistory">Add some for testing</a></p>
</c:if>
<c:if test="${!empty items}">


<form action="<c:url value="/History/Action" />" method="POST">
<input type="hidden" name="version" value="${version}">
<display:table name="items" uid="row" pagesize="30" requestURI="/NamedFeature" class="simple" cellspacing="0" cellpadding="1">
   	<display:column title="Index">${row_rowNum}</display:column>
   	<display:column property="name" title="Name"/>
   	<display:column title="Type" property="historyType" />
   	<display:column>QD</display:column>
   	<display:column property="numberItems" title="No. of results"/>
   	<display:column title="View/Edit"><a href="/History/Edit?item=${row_rowNum}&version=${version}">View/Edit</a></display:column>
   	<display:column title="Tools">Orthologues</display:column>
   	<display:column title="Download"><a href="/DownloadFeatures?historyItem=${row_rowNum}&version=${version}">Download</a></display:column>
   	<display:column title="Delete"><input type="submit" name="__submit_delete_${row_rowNum}" value="Delete" ></display:column>
</display:table>
</form>

</c:if>
<format:footer />