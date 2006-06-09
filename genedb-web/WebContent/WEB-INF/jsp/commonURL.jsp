<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header>${pageTitle}</format:header>

<ul>
<c:forEach items="${list}" var="item">
<li><a href="${prefix}${item}">${item}</a></li>
</c:forEach>
</ul>

<p />

<format:footer />
