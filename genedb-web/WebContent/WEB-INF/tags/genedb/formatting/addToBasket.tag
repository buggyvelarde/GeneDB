<%@ tag display-name="moveToBasket"
        body-content="empty" %>
<%@ attribute name="uniqueName" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
<c:when test="${inBasket eq 'false'}">
<a href="<c:url value="/" />NamedFeature?name=${dto.uniqueName}&addToBasket=true&geneDetails=true"><img src="<c:url value="/" />includes/images/addToBasket.gif"></a>
</c:when>
<c:otherwise>
<img src="<c:url value="/includes/images/alreadyInBasket.gif" />">
</c:otherwise>
</c:choose>
