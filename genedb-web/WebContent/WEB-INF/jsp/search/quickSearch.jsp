<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<format:header title="Quick Search" />
<format:page>
<br>

<table width=100% border=0>
    <tr>
       <td>

            <div id="geneDetails">
                <format:genePageSection id="nameSearch" className="whiteBox">
                <st:flashMessage />
                <table border=0 width="100%">
                    <tr>
                        <td width="30%">
                          <form:form commandName="query" action="${baseUrl}QuickSearchQuery" method="GET">
                            <table width="100%">
                                <tr>
                                    <td>Organism:</td>
                                    <td><db:simpleselect /> <font color="red"><form:errors
                                        path="taxons" /></font></td>
                                </tr>
                                <tr>
                                    <td>Name/Product:</td>
                                    <td><form:input id="nameProductInput" path="searchText" />
                                    <font color="red">&nbsp;<form:errors path="searchText" /></font>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Pseudogene:</td>
                                    <td><form:checkbox id="nameProduct" path="pseudogenes" /></td>
                                </tr>
                                <tr>
                                    <td>All names:</td>
                                    <td><form:checkbox id="nameProduct" path="allNames" /></td>
                                </tr>
                                <tr>
                                    <td>Products:</td>
                                    <td><form:checkbox id="nameProduct" path="product" /></td>
                                </tr>
                                <tr>
                                    <td>&nbsp;</td>
                                    <td colspan="2"><input type="submit" value="Submit" /></td>
                                    <td>&nbsp;</td>
                                </tr>
                            </table>

                        </form:form>
                        </td>

                        <td valign="center" align="left" width="100%">

                        <table border=0 width="100%">
                            <tr>

                               <c:if test="${empty resultsSize && !fn:contains(query.searchText, '*')}">
                                    <td>
                                        Select <a href="<misc:url value="/QuickSearchQuery"/>?taxons=${taxonNodeName}&searchText=*${query.searchText}*&allNames=${query.allNames}&pseudogenes=${query.pseudogenes}&product=${query.product}">*${query.searchText}*</a> to repeat search with wildcards.
                                    </td>
                               </c:if>

                              <c:if test="${fn:length(taxonGroup)>1}">
                                      <td align=right>
                                        <div id="taxonFrameId" class="taxonIFrame">
                                          <db:quicksearchtaxons
                                            currentTaxonNodeName="${taxonNodeName}"
                                            hasResult="${not empty resultsSize}"
                                            taxonGroup="${taxonGroup}"
                                            allNames="${query.allNames}"
                                            product="${query.product}"
                                            pseudogenes="${query.pseudogenes}"
                                            searchText="${query.searchText}" />
                                        </div>


                                        </td>
                              </c:if>
                                   <td align="left">
                                    <db:quicksearchmessage taxonGroup="${taxonGroup}" currentTaxonName="${taxonNodeName}" />
                                 </td>

                            </tr>

                        </table>
                        </td>
                    </tr>
                </table>
                </format:genePageSection>
            </div>
        </td>
    </tr>
    <tr>
        <td >
            <query:results />
        </td>
    </tr>
</table>


</format:page>

