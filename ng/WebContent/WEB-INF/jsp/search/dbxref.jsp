<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="misc" uri="misc" %>
<format:header title="Dbxref Search" />
<format:page>
<br>
<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form commandName="query" action="${actionName}" method="GET">
        <input type="hidden" />
            <table>
                <tr>
                    <td colspan="3">
                        <font color="red"><form:errors path="*" /></font>
                    </td>
                </tr>
                <tr>
                   <td>Organism:</td>
                   <td><db:simpleselect selection="${taxonNodeName}" /> <font color="red"><form:errors path="taxons" /></font></td>
               </tr>
                <tr>
                  <td>Dbxref:</td>
                  <td>
                    <form:input id="dbxref" path="dbxref"/> 
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