<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Protein Mass Search">
    <st:init />
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form commandName="query" action="Query" method="GET">
        <input type="hidden" name="q" value="proteinMass" />
            <table border=0>
                <tr>
                    <td>
                        <br><big><b>Protein Mass Search:</b></big>
                    </td>

                    <td>
                        <b>Organism:</b>
                        <br><db:simpleselect />
                     <br><font color="red"><form:errors path="taxons" /></font>
                    </td>
                    <td>
                        <b>Min mass:</b>
                        <br><form:input id="minMass" path="min"/>
                        <br><font color="red"><form:errors path="min" /></font>
                    </td>
                    <td>
                        <b>Max mass:</b>
                        <br><form:input id="maxMass" path="max"/>
                        <br><font color="red"><form:errors path="max" /></font>
                    </td>
                    <td>
                        <br><input type="submit" value="Submit" />
                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td colspan=5><font color="red"><form:errors  /></td>
                    <td></td>
                </tr>
            </table>
        </form:form>
    </format:genePageSection>
</div>




<br><query:results />
<format:footer />