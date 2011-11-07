<%@ tag display-name="results" body-content="scriptless"%>
<%@ attribute name="name"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/genedb/formatting"%>
<%@ taglib prefix="db" uri="db"%>
<%@ taglib prefix="misc" uri="misc"%>




<c:if test="${not empty resultsSize}">
    <div id="col-2-1">
        <c:if test="${isMaxResultsReached}">
            <font color="red">Please note that these search results are
                limited by a maximum of ${resultsSize}.</font>
            <br />
        </c:if>

        <!--  sort="external" -->
        <display:table 
                name="results" 
                id="row" 
                pagesize="${bounds.length}"
                size="${resultsSize}" 
                 
                partialList="true"
                class="search-data-table" 
                requestURI="/Query/${queryName}"
                cellspacing="0" 
                cellpadding="4">
            <display:column title="Systematic ids" style="width: 100px;">
                <a
                    href="<misc:url value="/gene/${row.displayId}"/>">${row.displayId}</a>
            </display:column>
            <display:column title="Organism" style="width: 150px;">
                <i><db:taxonname label="${row.taxonDisplayName}"
                        taxonNameType="HTML_SHORT" /> </i>
            </display:column>
            <display:column title="Product">
                ${row.product}
            </display:column>
            
            <c:if test="${motifs != null}">
                <display:column title="Match" >
                    <div style="text-align:center;">
                        <c:out value='${motifs[row.displayId].start}'/>-<c:out value='${motifs[row.displayId].end}'/>
                    </div>
                </display:column>
                <display:column title="Motif">
                    <div style="text-align:center;font-family:monospace;word-wrap: break-word;width:430px;"> 
                        <c:out value='${motifs[row.displayId].pre}'/><span style="color:#AB2219;"><c:out value='${motifs[row.displayId].match}'/></span><c:out value='${motifs[row.displayId].post}'/>
                   </div> 
                </display:column>
            </c:if> 
            
        </display:table>
    </div>
</c:if>










<%-- requestURI="/Query/${key}" 
              partialList="true"
              size="${resultsSize}"
              requestURI="/Query/${queryName}"
              sort="external" 
 --%>              

            <%--
    <display:column title="Contig">
      <i>${row.topLevelFeatureName}</i>
    </display:column>
    <display:column title="left">
      <i>${row.left}</i>
    </display:column>
    --%>




<%-- 
<h2></h2>

<table class="search-data-table" cellspacing="0" cellpadding="4">
<thead>
<tr>
<th>Systematic ids</th>
<th>Organism</th>
<th>Product</th></tr></thead>
<c:set var="cls" value="even" />
<c:forEach items="${results}" var="result">
    
    <c:choose>
        <c:when test="${cls == 'odd' }">
            <c:set var="cls" value="even" />
        </c:when>
        <c:otherwise>
            <c:set var="cls" value="odd" />
        </c:otherwise>
    </c:choose>
    
    
    <c:if test="${odd == true}">
        
    </c:if>
    
    <tr class="${cls}">
        <td style="width: 100px;" ><a href="<misc:url value="/gene/${result.displayId}"/>">${result.displayId}</a></td>
        <td style="width: 150px;">${result.taxonDisplayName}</td>
        <td>${result.product}</td>
    
    </tr>
</c:forEach>



</table>

<p>${resultsSize}</p>
<p>${queryName}</p> 

<p>${bounds.page}</p>
<p>${bounds.length}</p>

--%>

