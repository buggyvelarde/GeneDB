<%@ tag display-name="moveToBasket"
        body-content="empty" %>
<%@ attribute name="uniqueName" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

${inBasket}
<c:choose>
<c:when test="${inBasket eq Boolean.FALSE}">
<img id="basketbutton" src="<c:url value="/" />includes/images/addToBasket.gif" onclick="addToBasket(${dto.uniqueName})">
</c:when>
<c:otherwise>
<img src="<c:url value="/includes/images/alreadyInBasket.gif" />">
</c:otherwise>
</c:choose>
