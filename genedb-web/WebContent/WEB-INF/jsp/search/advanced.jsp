<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Advanced Search">
    <st:init />
    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form commandName="query" action="Query" method="GET">
        <input type="hidden" name="q" value="advanced" />
            <table>
                <tr>
                    <td colspan="3">
                        <font color="red"><form:errors path="*" /></font>
                    </td>
                </tr>
                <tr>
                  <td>Search In:</td>
                  <td>
                    <form:select path="category">
                        <c:forEach items="${typeMap}" var="mapEntry">
                        <form:option value="${mapEntry.key}">${mapEntry.value}</form:option>
                        </c:forEach>
                    </form:select>
                  </td>
                </tr>
                <tr>
                  <td>Search Term:</td>
                  <td>
                    <form:input id="search" path="search"/>
                  </td>
                </tr>
                <tr>
                  <td>&nbsp;</td>
                  <td colspan="2">
                    <input type="submit" value="Submit" />
                  </td>
                  <td>&nbsp;</td>
                </tr>
            </table>

        </form:form>
    </format:genePageSection>
</div>
<format:footer />