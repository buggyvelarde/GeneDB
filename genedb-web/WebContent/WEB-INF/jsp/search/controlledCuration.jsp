<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:header title="Category List" />
<format:page>
<br />
<c:url value="BrowseTerm" var="url">
	<c:param name="category" value="${category}"/>
</c:url>


<br><query:results />

<format:footer />
</format:page>
