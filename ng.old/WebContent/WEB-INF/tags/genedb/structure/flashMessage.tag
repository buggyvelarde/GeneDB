<%@ tag display-name="flashMessage" body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${!empty sessionScope._FLASH_MSG}">
<div>
<p><font color="red">${sessionScope._FLASH_MSG}</font></p>
<c:remove scope="session" var="_FLASH_MSG" />
</div>
</c:if>