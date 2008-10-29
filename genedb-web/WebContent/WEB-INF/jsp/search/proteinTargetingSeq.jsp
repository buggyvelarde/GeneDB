<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Number of TM domains Search">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection id="nameSearch" className="whiteBox">
		<form:form commandName="query" action="Query" method="post">
        <input type="hidden" name="q" value="proteinTargett" />
            <table>
                <tr>
                    <td colspan="3">
                        <font color="red"><form:errors path="*" /></font>
                    </td>
                </tr>
                <tr><td colspan="2">Protein Targeting Sequence present:</td></tr>
                <tr><td>Signal Peptide</td>
                    <td><form:checkbox id="sigP" path="sigP"/></td>
                </tr>
                <tr><td colspan="2">GPI anchor</td>
                    <td><form:checkbox id="gpi" path="gpi"/></td>
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