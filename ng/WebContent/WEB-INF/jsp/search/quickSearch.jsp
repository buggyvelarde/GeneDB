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
                          <form:form commandName="query" action="${actionName}" method="GET">
                            <table width="100%">
                                <tr>
                                    <td>Organism:</td>
                                    <td><db:simpleselect selection="${taxonNodeName}" /> <font color="red"><form:errors
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
                                        <c:if test="${(fn:length(query.searchText) > 0)}" >
                                        Select <a href="<misc:url value="/QuickSearchQuery"/>?taxons=${taxonNodeName}&searchText=*${query.searchText}*&allNames=${query.allNames}&pseudogenes=${query.pseudogenes}&product=${query.product}">${query.searchText}*</a> to repeat search with a wildcard.
                                        </c:if>
                                         
                                        Please note that searches with a wildcard (*) prefix will have this removed for performance reasons. You can still
                                        use wildcards in the middle or at the end of your search.
                                        
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
                                            searchText="${query.searchText}"
                                            baseUrl="${baseUrl}"
                                             />
                                        </div>


                                        </td>
                              </c:if>
                                <td align="left">
                                    <db:quicksearchmessage taxonGroup="${taxonGroup}" currentTaxonName="${taxonNodeName}" />
                                 </td>
                               
                            </tr>
                       </table>
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
<c:if test="${! empty suggestions}">
    <table id="row" cellpadding="4" class="ui-widget " cellspacing="0">
        <tr>
            <td style="width: 150px;" >Did you mean one of the following terms?</td>
             <td> 
               <c:forEach items="${suggestions}" var="suggestion">
                  <div class="did-you-mean-result ui-state-default ui-corner-all" style="float:left; padding:5px;margin:5px;"  >
                     <a style="width: 150px;" href="<misc:url value="/Query/quickSearch"/>?taxons=${taxonNodeName}&searchText=${suggestion}&allNames=${query.allNames}&pseudogenes=${query.pseudogenes}&product=${query.product}">${suggestion}</a>
                  </div>
               </c:forEach>
             </td>
        </tr>
    </table>
</c:if>

</format:page>

