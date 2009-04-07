<%@ tag display-name="list-string"
        body-content="empty" %>
<%@ attribute name="count" required="true" %>
<%@ attribute name="cv" required="true" %>
<%@ attribute name="cvTermName" required="true" %>
<%@ attribute name="taxons" required="true" %>
<%@ attribute name="suppress" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="others" value="${count - 1}" />
<c:set var="phrase" value="Other" />
<c:if test="${others > 2}">
  <c:set var="phrase" value="Others" />
</c:if>
<c:if test="${others >= 1}">
(<a href="<c:url value="/Query"><c:param name="q" value="controlledCuration" />
                        <c:param name="taxons" value="${taxons}" />
                        <c:param name="cvTermName" value="${cvTermName}" />
                        <c:param name="cv" value="genedb_products" />
                        <c:param name="suppress" value="${suppress}" /></c:url>">${others} ${phrase}</a>)
</c:if>
