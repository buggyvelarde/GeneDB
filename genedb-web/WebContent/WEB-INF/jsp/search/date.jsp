<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Date Search">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection id="nameSearch" className="whiteBox">
		<form:form commandName="query" action="Query" method="GET">
        <input type="hidden" name="q" value="date" />
            <table>
                <tr>
                    <td colspan="3">
                        <font color="red"><form:errors path="*" /></font>
                    </td>
                </tr>
                <tr>
                  <td>Date:</td>
                  <td>
                    <form:input id="date" path="date"/>
                  </td>
                </tr>
                <tr>
                  <td>Created:</td>
                  <td>
                    <form:checkbox id="created" path="created"/>
                  </td>
                </tr>
                <tr>
                  <td>After:</td>
                  <td>
                    <form:checkbox id="after" path="after"/>
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