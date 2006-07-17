<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<format:header name="History" />

<c:if test="${!history.filled}">
<p>There are no entries in your history yet (or your session has expired)<p>
</c:if>
<c:if test="${history.filled}">
<p>There are ${history.results} items in your history.</p>
</c:if>
<format:footer />