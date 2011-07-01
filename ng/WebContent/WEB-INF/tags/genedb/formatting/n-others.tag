<%@ tag display-name="list-string"
        body-content="empty" %>
<%@ attribute name="count" required="true" %>
<%@ attribute name="cv" required="true" %>
<%@ attribute name="cvTermName" required="true" %>
<%@ attribute name="taxons" required="true" %>
<%@ attribute name="suppress" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="misc" uri="misc" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="others" value="${count - 1}" />
<c:set var="phrase" value="Other" />
<c:if test="${others > 1}">
  <c:set var="phrase" value="Others" />
</c:if>
<c:if test="${others >= 1}">
(<a href="<misc:url value="/Query/controlledCuration"><spring:param name="taxons" value="${taxons}" /><spring:param name="cvTermName" value="${cvTermName}" /><spring:param name="cv" value="${cv}" /><spring:param name="suppress" value="${suppress}" /></misc:url>">${others} ${phrase}</a>)
</c:if>
