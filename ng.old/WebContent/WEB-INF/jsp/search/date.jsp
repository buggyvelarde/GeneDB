<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="misc" uri="misc" %>
<format:header title="Date Search" />
<format:page>
<br>
<div id="geneDetails">
	<format:genePageSection id="nameSearch" className="whiteBox">
		<form:form commandName="query" action="${baseUrl}Query/date" method="GET">
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
                    <form:input id="date" path="date"/> (YYYY/MM/DD)
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

	<format:test-for-no-results />
</div>

<br><query:results />
</format:page>