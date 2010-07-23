<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Product Search" />
<format:page>
<br>

<br>
<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form commandName="query" action="${baseUrl}Query/product" method="GET">
            <table>
                <tr>
                    <td width=180>
                        <br><big><b>Product Search:&nbsp;</b></big>
                    </td>
                    <td width=180>
                        <b>Organism:</b>
                        <br><db:simpleselect selection="${taxonNodeName}" />
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
    
    <format:test-for-no-results />
    
</div>

<br><query:results />

</format:page>
