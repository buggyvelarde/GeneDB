<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty key and not empty index and not empty resultsLength}">

    &nbsp;Gene ${index+1} of ${resultsLength+1} Search Results.&nbsp;
    <c:choose>
        <c:when test="${param.index != '0'}">
            <a href="ResultsNavigator?index=0&key=${key}">First</a>
        </c:when>
        <c:otherwise>
            First
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <c:if test="${index != '0'}">
        <a href="ResultsNavigator?index=${index - 1}&key=${key}">Previous</a>
         &nbsp;|&nbsp;
    </c:if>

    <c:if test="${index != resultsLength}">
        <a href="ResultsNavigator?index=${index + 1}&key=${key}">Next</a>
         &nbsp;|&nbsp;
    </c:if>

    <c:choose>
        <c:when test="${index != resultsLength}">
            <a href="ResultsNavigator?index=${resultsLength-1}&key=${key}">Last</a>
        </c:when>
        <c:otherwise>
            Last
        </c:otherwise>
    </c:choose>
    &nbsp;|&nbsp;

    <a href="ResultsNavigator?index=-256&key=${key}">Back to Search Results</a>
    <br>
</c:if>

