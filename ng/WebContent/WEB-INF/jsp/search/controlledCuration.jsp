<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:header title="Category List" />
<format:page>
<br />
<misc:url value="BrowseTerm" var="url">
	<spring:param name="category" value="${category}"/>
</misc:url>


<br><query:results />

<format:test-for-no-results />

</format:page>
