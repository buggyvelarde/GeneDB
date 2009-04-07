<%@ tag display-name="list-string"
        body-content="empty" %>
<%@ attribute name="count" required="true" %>
<%@ attribute name="cv" required="true" %>
<%@ attribute name="cvTermName" required="true" %>
<%@ attribute name="taxons" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="phrase" value="Other" />
<c:if test="${count > 2}">
  <c:set var="phrase" value="Others" />
</c:if>
<c:if test="${count >= 2}">
(<a href="<c:url value="/Query"><c:param name="q" value="controlledCuration" />
                        <c:param name="taxons" value="${taxons}" />
                        <c:param name="cvTermName" value="${cvTermName}" />
                        <c:param name="cv" value="genedb_products" /></c:url>">${count} ${phrase}</a>)
</c:if>
