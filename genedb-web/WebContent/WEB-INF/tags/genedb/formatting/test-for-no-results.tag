 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
 <c:if test="${fn:length(results)== 0}"><h2>No results.</h2></c:if>