<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="misc" uri="misc" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:if test="${not empty key and not empty index and not empty resultsLength}">

    &nbsp;Gene ${index} of ${resultsLength} Search Results.&nbsp;
    <c:choose>
        <c:when test="${index != '1'}">
            <a href="<misc:url value="/ResultsNavigator/${key}"><spring:param name="index" value="1" /></misc:url>">First</a>
        </c:when>
        <c:otherwise>
            First
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <c:choose>
        <c:when test="${index != '1'}">
            <a href="<misc:url value="/ResultsNavigator/${key}"><spring:param name="index" value="${index - 1}" /></misc:url>">Previous</a>
        </c:when>
        <c:otherwise>
            Previous
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <c:choose>
        <c:when test="${index != resultsLength}">
            <a href="<misc:url value="/ResultsNavigator/${key}"><spring:param name="index" value="${index+1}" /></misc:url>">Next</a>
        </c:when>
        <c:otherwise>
            Next
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <c:choose>
        <c:when test="${index != resultsLength}">
            <a href="<misc:url value="/ResultsNavigator/${key}"><spring:param name="index" value="${resultsLength}" /></misc:url>">Last</a>
        </c:when>
        <c:otherwise>
            Last
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <a href="<misc:url value="/ResultsNavigator/${key}"><spring:param name="index" value="-256" /></misc:url>">Back to Search Results</a>
    <br>
</c:if>

