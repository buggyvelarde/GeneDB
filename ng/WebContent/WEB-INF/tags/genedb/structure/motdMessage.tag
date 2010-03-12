<%@ tag display-name="motdMessage" body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${!empty sessionScope._MOTD_MSG}">
<div>
<p><font color="red">${sessionScope._MOTD_MSG}</font></p>
</div>
</c:if>