<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Name/Product Search">
    <st:init />
    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">

        <st:flashMessage />

        <form:form commandName="query" action="Query" method="GET">
        <input type="hidden" name="q" value="allNameProduct" />
            <table>
                <tr>
                  <td>Organism:</td>
                  <td>
                    <db:simpleselect selection="${taxonNodeName}" />
                    <font color="red"><form:errors path="taxons" /></font>
                  </td>
                </tr>
                <tr>
                  <td>Name/Product:</td>
                  <td>
                    <form:input id="nameProductInput" path="search"/>
                    <font color="red">&nbsp;<form:errors path="search" /></font>
                  </td>
                </tr>
                <tr>
                  <td>Pseudogene:</td>
                  <td>
                    <form:checkbox id="nameProduct" path="pseudogenes"/>
                  </td>
                </tr>
                <tr>
                  <td>All names:</td>
                  <td>
                    <form:checkbox id="nameProduct" path="allNames"/>
                  </td>
                </tr>
                <tr>
                  <td>Products:</td>
                  <td>
                    <form:checkbox id="nameProduct" path="product"/>
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

<script>
    onload = function(){
        input = document.getElementById('nameProductInput');
        if (input!= null && input.value== ''){
            input.focus();
        }
    }
</script>