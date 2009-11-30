<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="misc" uri="misc" %>
<format:header title="Name Product Search" />
<format:page>
<br>
<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form commandName="query" action="Query" method="GET">
        <input type="hidden" name="q" value="nameProduct" />
            <table>

                <tr>
                  <td>Name/Product:</td>
                  <td>
                    <form:input id="nameProduct" path="search"/>
                    <font color="red">&nbsp;<form:errors path="search" /></font>
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
</format:page>