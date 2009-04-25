<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty key and not empty index and not empty resultsLength}">

    &nbsp;Gene ${index} of ${resultsLength} Search Results.&nbsp;
    <c:choose>
        <c:when test="${index != '1'}">
            <a href="ResultsNavigator?index=0&key=${key}">First</a>
        </c:when>
        <c:otherwise>
            First
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <c:choose>
        <c:when test="${index != '1'}">
            <a href="ResultsNavigator?index=${index - 1}&key=${key}">Previous</a>
        </c:when>
        <c:otherwise>
            Previous
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <c:choose>
        <c:when test="${index != resultsLength}">
            <a href="ResultsNavigator?index=${index+1}&key=${key}">Next</a>
        </c:when>
        <c:otherwise>
            Next
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <c:choose>
        <c:when test="${index != resultsLength}">
            <a href="ResultsNavigator?index=${resultsLength}&key=${key}">Last</a>
        </c:when>
        <c:otherwise>
            Last
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <a href="ResultsNavigator?index=-256&key=${key}">Back to Search Results</a>
    <br>
</c:if>

