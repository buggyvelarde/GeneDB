<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Protein Length Search">
    <st:init />
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<br>



<table width=100% border=0>
    <tr>
        <td colspan=2>
            
<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
            
            
            <!-- Select <a href="${pageContext.request.contextPath}/Query?q=allNameProduct&taxons=${param.taxons}&search=${param.searchText}&pseudogenes=true&_pseudogenes=on&allNames=true&_allNames=on&product=true&_product=on">Refine</a> to modify search options.-->
            
                   <st:flashMessage />

        <form:form commandName="query" action="QuickSearchQuery" method="GET">
        <input type="hidden" name="q" value="quickSearchQuery" />
            <table>
                <tr>
                  <td>Organism:</td>
                  <td>
                    <db:simpleselect />
                    <font color="red"><form:errors path="taxons" /></font>
                  </td>
                </tr>
                <tr>
                  <td>Name/Product:</td>
                  <td>
                    <form:input id="nameProductInput" path="searchText"/>
                    <font color="red">&nbsp;<form:errors path="searchText" /></font>
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
            
            
            
            
                 
        </td>        
    </tr>
    <tr>
        <td width=20% align=right>    
            <div id="geneTaxonsPanel">
                <format:genePageSection  className="whiteBox">
                    <table border=0>
                    <c:forEach var="map" items="${taxonGroup}">
                        <tr>
                            <td><a href="${pageContext.request.contextPath}/QuickSearchQuery?taxons=${map.key}&searchText=${query.searchText}">${map.key}(${map.value})</a></td>
                        </tr>
                    </c:forEach>
                    </table>
                </format:genePageSection>
            </div>
        </td>

        <td width=80%>
            <query:results />
        </td>    
    </tr>
</table>


<format:footer />

