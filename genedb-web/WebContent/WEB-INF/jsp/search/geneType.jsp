<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Gene Type Search">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection id="nameSearch" className="whiteBox">
		<form:form commandName="query" action="Query" method="post">
        <input type="hidden" name="q" value="geneType" />
            <table>
                <tr>
                    <td colspan="3">
                        <font color="red"><form:errors path="*" /></font>
                    </td>
                </tr>
                <tr>
                <td>
                     <b>Organism:</b>
            	     <br><db:simpleselect />
            	     <br><font color="red"><form:errors path="taxons" /></font>
                  <td>
                </tr>
                <tr>
                  <td><b>Gene Type:</b>
                    <form:select path="type">
                        <c:forEach items="${typeMap}" var="mapEntry">
                        <form:option value="${mapEntry.key}">${mapEntry.value}</form:option>
                        </c:forEach>
                    </form:select>
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


<br><query:results />
<format:footer />