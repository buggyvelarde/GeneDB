<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Product Search">
    <st:init />
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>

<br>
<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form commandName="query" action="Query" method="GET">
        <input type="hidden" name="q" value="product" />
            <table>
                <tr>
                    <td width=180>
                        <br><big><b>Product Search:&nbsp;</b></big>
                    </td>
                    <td width=180>
                        <b>Organism:</b>
                        <br><db:simpleselect />
                        <br><font color="red"><form:errors path="taxons" /></font>
                    </td>
                    <td width=180>
                        <b>Product:</b>
                        <br><form:input id="search" path="search"/>
                        <br><font color="red"><form:errors path="search" /></font>
                    </td>
                     <td>
                         <b>Pseudogene:</b>
                          <br><form:checkbox id="nameProduct" path="pseudogenes" />
                      </td>
                      <td>&nbsp;&nbsp;&nbsp;</td>
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
</div>

<br><query:results />

<format:footer />
