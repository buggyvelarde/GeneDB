<%@ tag display-name="list-string"
        body-content="empty" %>
<%@ attribute name="list" required="true" %>
<%@ attribute name="seperator" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="sep" value=", " />
<c:if test="${!empty seperator}">
  <c:set var="sep" value="${seperator}" />
</c:if>


<c:forEach items="${list}" var="element" varStatus="vs">
<c:if test="${!vs.first}">${sep}</c:if>${element.class}
</c:forEach>
