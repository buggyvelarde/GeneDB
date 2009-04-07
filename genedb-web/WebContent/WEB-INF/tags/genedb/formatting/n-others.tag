<%@ tag display-name="list-string"
        body-content="empty" %>
<%@ attribute name="count" required="true" %>
<%@ attribute name="link" required="true" %>
<%@ attribute name="name" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="phrase" value="Other" />
<c:if test="${count > 2}">
  <c:set var="phrase" value="Others" />
</c:if>
<c:if test="${count >= 2}">
(<a href="${link}${name}">${count} ${phrase}</a>)
</c:if>
