<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Protein Targetting Sequences" />
<format:page>
<br>

<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form commandName="query" action="${baseUrl}Query/proteinTargetingSeq" method="GET">
            <table>
                <tr>
                    <td colspan="3">
                        <font color="red"><form:errors path="*" /></font>
                    </td>
                </tr>
                                <tr>
                <td>
                     <b>Organism:</b>
                     <br><db:simpleselect selection="${taxonNodeName}" />
                     <br><font color="red"><form:errors path="taxons" /></font>
                  <td>
                </tr>
                <tr><td colspan="2">Search proteins for the following targeting sequences (select the type(s) that must be present):</td></tr>
                <tr><td>Signal Peptide</td>
                    <td><form:checkbox id="sigP" path="sigP"/></td>
                </tr>
                <tr><td>GPI anchor</td>
                    <td><form:checkbox id="gpi" path="gpi"/></td>
                </tr>
                <tr><td>Apicoplast</td>
                    <td><form:checkbox id="apicoplast" path="apicoplast"/></td>
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
