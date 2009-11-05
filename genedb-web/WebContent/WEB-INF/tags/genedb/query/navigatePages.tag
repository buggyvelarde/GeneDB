<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty key and not empty index and not empty resultsLength}">

    &nbsp;Gene ${index} of ${resultsLength} Search Results.&nbsp;
    <c:choose>
        <c:when test="${index != '1'}">
            <a href="ResultsNavigator/${key}?index=1">First</a>
        </c:when>
        <c:otherwise>
            First
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <c:choose>
        <c:when test="${index != '1'}">
            <a href="ResultsNavigator/${key}?index=${index - 1}">Previous</a>
        </c:when>
        <c:otherwise>
            Previous
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <c:choose>
        <c:when test="${index != resultsLength}">
            <a href="ResultsNavigator/${key}?index=${index+1}">Next</a>
        </c:when>
        <c:otherwise>
            Next
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <c:choose>
        <c:when test="${index != resultsLength}">
            <a href="ResultsNavigator/${key}?index=${resultsLength}">Last</a>
        </c:when>
        <c:otherwise>
            Last
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <a href="ResultsNavigator/${key}?index=-256">Back to Search Results</a>
    <br>
</c:if>

