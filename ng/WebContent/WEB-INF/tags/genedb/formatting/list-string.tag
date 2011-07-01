<%@ tag display-name="list-string"
        body-content="empty" %>
<%@ attribute name="list" required="true" %>
<%@ attribute name="separator" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<c:set var="sep" value=", " />
<c:if test="${!empty separator}">
  <c:set var="sep" value="${separator}" />
</c:if>


<c:forEach items="${list}" var="element" varStatus="vs">
<c:if test="${!vs.first}">${sep}</c:if>${element}
</c:forEach>
