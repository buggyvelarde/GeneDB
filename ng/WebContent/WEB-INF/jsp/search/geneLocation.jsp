<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<format:header title="Location Search" >
    <script type="text/javascript" src="<misc:url value="/includes/scripts/web-artemis" />/js/chromosoml/chromosoml.js"></script>
</format:header>
<format:page>
<br />

<script type="text/javascript"
    src="<misc:url value="/includes/scripts/genedb/chromosoml.js"/>"></script>
<script type="text/javascript"
    src="<misc:url value="/includes/scripts/genedb/geneLocation.js"/>"></script>



<c:choose>
 <c:when test="${topLevelFeatureName != null}">
    <script>var topLevelFeatureName = "${topLevelFeatureName}";</script>
 </c:when>
 <c:otherwise>
    <script>var topLevelFeatureName = null;</script>
 </c:otherwise>
</c:choose>

<c:choose>
 <c:when test="${organism != null}">
    <script>var organism = "${organism}";</script>
 </c:when>
 <c:when test="${taxonNodeName != null}">
    <script>var organism = "${taxonNodeName}";</script>
 </c:when>
 <c:otherwise>
    <script>var organism = null;</script>
 </c:otherwise>
</c:choose>



<script>

$(function(){
	$('.spinner').CallStatusSpinner({
        height : 11.5,
        width : 50,
        img : '<misc:url value="/includes/image/spinner.gif"/>'
    });
	
	$('select#geneLocationTaxons').GeneLocations("${topLevelFeatureName}", "#topLevelFeatureName", ".spinner", "#minLocation", "#maxLocation", '#slider-text', '#toggle-text');
	
});

</script>




<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form id="geneLocationQueryForm" commandName="query" action="${actionName}" method="GET">
        
        
        
        
            <table border=0 width="100%">
                <tr>
                    <td width="180px">
                        <br><big><b>Gene Location Search:</b></big>
                        <br><span class="spinner" >&nbsp;&nbsp;&nbsp;</span>
                    </td>
                    <td >
                     <b>Organism:</b>
                        <br><db:simpleselect selection="${taxonNodeName}" unlinkgroups="true" id="geneLocationTaxons" />
                        <br><font color="red"><form:errors path="taxons" /></font>
                    </td>
                </tr>
                <tr>
                    <td>
                        &nbsp;
                    </td>
                    <td>    
                        <b>Region:</b><br>
                        <select id="topLevelFeatureName" name="topLevelFeatureName"></select>
                    </td>
                </tr>
                <tr>
                    <td>
                        &nbsp;
                    </td>
                    <td>
                        <div style="float:right">
                        <input type="checkbox" checked="checked" id="toggle-text" /><label title="Click to show inputs" for="toggle-text"></label>
                        </div>
                        <b>Boundaries:  </b> <label id="boundaries"></label>
                        <br>
                        
                        <div id="slider-range"></div>
                        
                        <div id='slider-text'>
                        <div style="float:left">
                        <form:input id="minLocation" path="min"/>
                        <font color="red"><form:errors path="min" /></font>
                        </div>
                        <div style="float:right">
                        <form:input id="maxLocation" path="max"/>
                        <font color="red"><form:errors path="max" /></font>
                        </div>
                        </div>
                        <div style="clear:both;height:1px;" ></div>
                        
                    </td>
                </tr>
                <tr>
                    <td>
                        &nbsp;
                    </td>
                    <td>
                         <b>Pseudogene:</b>
                          <br><form:checkbox id="pseudogenes" path="pseudogenes" />
                      </td>
                      <td>&nbsp;&nbsp;&nbsp;</td>
                </tr>
                <tr>
                    <td></td>
                    <td colspan=7> <input type="submit" value="Submit" /></td>
                    <td></td>
                </tr>
                <tr>
                    <td></td>
                    <td colspan=7> <font color="red"><form:errors  /></font></td>
                    <td></td>
                </tr>
            </table>
        </form:form>
    
    <P>Select an organism from the retrieve its list of contigs or chromosomes. </P>
    
    </format:genePageSection>
    
    <format:test-for-no-results />
</div>


<br><query:results />
</format:page>