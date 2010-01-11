<%@ tag display-name="section"
        body-content="scriptless" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="id" %>
<%@ attribute name="collapsed" type="java.lang.Boolean" %>
<%@ attribute name="collapsible" type="java.lang.Boolean" %>
<%@ attribute name="hideIfEmpty" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<c:set var="contextPath"><misc:c:url value="/"/></c:set>
<c:set var="imgLoc">${contextPath}includes/images/tri.gif</c:set>

<c:if test="${!collapsed}">
	<c:set var="imgLoc">${contextPath}includes/images/tridown.gif"</c:set>
</c:if>

<p class="section"><img id="sect_${id}_image" src="${imgLoc}" onclick="toggleSection('${id}', '${contextPath}')"/>&nbsp;${name}</p>
<div class="section-body" id="sect_${id}_content" <c:if test="${collapsed}">style="display:none"</c:if>>
<jsp:doBody />
</div>