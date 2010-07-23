<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Protein Length Search" />
<format:page>
<br>

<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form commandName="query" action="${baseUrl}Query/proteinLength" method="GET">
            <table>
                <tr>
                    <td width=180>
                        <br><big><b>Protein Length Search:&nbsp;</b></big>
                    </td>
                    <td width=180>
                        <b>Organism:</b>
                        <br><db:simpleselect selection="${taxonNodeName}" />
                        <br><font color="red"><form:errors path="taxons" /></font>
                    </td>
                    <td width=180>
                        <b>Minimum Length:</b>
                        <br><form:input id="minProteinLength" path="min"/>
                        <br><font color="red"><form:errors path="min" /></font>
                    </td>
                    <td width=180>
                        <b>Maximum Length:</b>
                        <br><form:input id="maxProteinLength" path="max"/>
                        <br><font color="red"><form:errors path="max" /></font>
                    </td>
                    <td>
                        <br><input type="submit" value="Submit" />
                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td colspan=3><font color="red"><form:errors  /></td>
                    <td></td>
                </tr>
            </table>

        </form:form>
    </format:genePageSection>
    
    <format:test-for-no-results />
</div>

<br><query:results />

</format:page>
