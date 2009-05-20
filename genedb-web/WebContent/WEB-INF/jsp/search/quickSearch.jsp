<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<format:headerRound title="Protein Length Search">
    <st:init />
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<br>



<table width=100% border=0>
    <tr>
	   <td>
			            
			<div id="geneDetails">
			    <format:genePageSection id="nameSearch" className="whiteBox">
			    <!-- Select <a href="${pageContext.request.contextPath}/Query?q=allNameProduct&taxons=${param.taxons}&search=${param.searchText}&pseudogenes=true&_pseudogenes=on&allNames=true&_allNames=on&product=true&_product=on">Refine</a> to modify search options.-->
			    <st:flashMessage />
				<table border=0 width="100%">
					<tr>
						<td width="50%">
						  <form:form commandName="query" action="QuickSearchQuery"
							method="GET">
							<input type="hidden" name="q" value="quickSearchQuery" />
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
						
						<td valign="center" align="left" width="50%">
						<br>
						<table border=0>
    						<tr>
	           					<td>
				            		<c:choose>
                                        <c:when test="${empty resultsSize && fn:length(taxonGroup)==0 }">
                                            No Exact match found for <b>${query.searchText}</b> in <b>${taxonNodeName}</b> or in other organisms.
                                        </c:when>
						                <c:when test="${empty resultsSize && fn:length(taxonGroup)>0}">
						                      No Exact match found for <b>${query.searchText}</b> in <b>${taxonNodeName}</b>, <br>however the following ${fn:length(taxonGroup)} organisms below have matches.
						                </c:when>
                                        <c:when test="${not empty resultsSize && fn:length(taxonGroup)==1}">
                                            All ${resultsSize} matches for <b>${query.searchText}</b>, found in organism <b>${taxonNodeName}</b>.
                                        </c:when>
                                        <c:when test="${not empty resultsSize && fn:length(taxonGroup)>1}">
                                            ${resultsSize} matches found in ${fn:length(taxonGroup)} organisms.
                                        </c:when>
						             </c:choose>
						         </td>
						     </tr>
						
						      <c:if test="${fn:length(taxonGroup)>1}">
						          <tr>
						              <td>
						                      <iframe
                                                src="${pageContext.request.contextPath}/QuickSearchQuery?q=none&searchText=${query.searchText}&pseudogenes=${query.pseudogenes}&allNames=${query.allNames}&product=${query.product}"
                                                frameborder="1"
                                                width="70%" height="90" align="left">
                                            </iframe>
                                        </td>
                                    </tr>
                              </c:if>
                        
                              <c:if test="${empty resultsSize && !fn:contains(query.searchText, '*')}">
                                  <tr>
                                      <td>
                                        Select <a href="${pageContext.request.contextPath}/QuickSearchQuery?q=quickSearchQuery&taxons=${taxonNodeName}&searchText=*${query.searchText}*&allNames=${query.allNames}&pseudogenes=${query.pseudogenes}&product=${query.product}">*${query.searchText}*</a> to repeat search with wildcards.
                                      </td>
                                  </tr>
                              </c:if>
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


<format:footer />

