<%@ tag display-name="flashMessage" body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%-- FLASH_MSG is defined as WebConstants.FLASH_MSG --%>
<c:if test="${!empty sessionScope['FLASH_MSG']}">
<div class="flash">
<p>${sessionScope['FLASH_MSG']}</p>
</div>
<c:remove scope="session" var="FLASH_MSG" />
</c:if>