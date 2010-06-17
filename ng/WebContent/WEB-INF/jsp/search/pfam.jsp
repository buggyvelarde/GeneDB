<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Pfam Search" />
<format:page>
<br>

<br>
<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form commandName="query" action="${baseUrl}Query/pfam" method="GET">

            <table>
                <tr>
                    <td width=180>
                        <br><big><b>Pfam:&nbsp;</b></big>
                    </td>
                    <td width=180>
                        <b>Organism:</b>
                        <br><db:simpleselect selection="${taxonNodeName}" />
                        <br><font color="red"><form:errors path="taxons" /></font>
                    </td>
                    <td width=180>
                        <b>Pfam Id:</b>
                        <br><form:input id="pfam" path="search"/>
                        <br><font color="red"><form:errors path="search" /></font>
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
