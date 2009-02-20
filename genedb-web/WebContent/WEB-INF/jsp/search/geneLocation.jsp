<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Gene Location Search">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection id="nameSearch" className="whiteBox">
		<form:form commandName="query" action="Query" method="post">
        <input type="hidden" name="q" value="geneLocation" />
            <table>
                <tr>
                    <td colspan="3">
                        <font color="red"><form:errors path="*" /></font>
                    </td>
                </tr>
                <tr>
                  <td>Parent feature:</td>
                  <td>
                    <form:input id="topLevelFeatureName" path="topLevelFeatureName"/>
                  </td>
                </tr>
                <tr>
                  <td>Min location:</td>
                  <td>
                    <form:input id="minLocation" path="min"/>
                  </td>
                </tr>
                <tr>
                  <td>Max location:</td>
                  <td>
                    <form:input id="maxLocation" path="max"/>
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