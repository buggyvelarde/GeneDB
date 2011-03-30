<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<format:header title="Location Search" />
<format:page>
<br />

<script type="text/javascript"
    src="<misc:url value="/includes/scripts/genedb/chromosoml.js"/>"></script>

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


$('.spinner').CallStatusSpinner({
    height : 11.5,
    width : 50,
    img : '<misc:url value="/includes/image/spinner.gif"/>'
});

var lengths = {};
var loading = false;

function startedLoading() {
    if (! loading) {
    	$('.spinner').CallStatusSpinner("addCall");
    	loading = true;
    }
}

function stoppedLoading() {
    if (loading) {
    	$('.spinner').CallStatusSpinner("removeCall");
        loading = false;
    }
}

function loadTops(organism) {
	startedLoading();
	
	$.ajax({
        url: getBaseURL() + "service/top",
        type: 'GET',
        dataType: 'json',
        data: {
            'commonName' : organism
        },
        success: function(response) {
        	$('#topLevelFeatureName').html('');

        	lengths = {};

        	if (jQuery.isArray(response.results.feature)) {
            
	       		for (var i in response.results.feature) {
	
	       			var result = response.results.feature[i];
	       			var name = result["@name"];
	       			var length = result["@length"];
	       			lengths[name] = length;
	       		    
	       			var selected = '';
	
	       		    if (topLevelFeatureName == name) {
	       		    	selected = ' selected ';
	       		    }
	       			
	       			$('#topLevelFeatureName').append('<option length="'+length+'" ' + selected + ' value='+ name + ' >' + name + ' ('+length+' residues) </option>');
	       		}
	       	    
        	} else {
            	
        		var result = response.results.feature;
                var name = result["@name"];
                var length = result["@length"];
                lengths[name] = length;
                
                var selected = '';

                if (topLevelFeatureName == name) {
                    selected = ' selected ';
                }
                
                $('#topLevelFeatureName').append('<option length="'+length+'" ' + selected + ' value='+ name + ' >' + name + ' ('+length+' residues) </option>');
        	}

        	stoppedLoading();
            
       	
        }
	});

}

function loadOrganisms() {
	startedLoading();
	
	$.ajax({
        url: getBaseURL() + "service/organisms",
        type: 'GET',
        dataType: 'json',
        success: function(response) {
           for (var i in response.results.organisms.string) {
               var result = response.results.organisms.string[i];
               
               var selected = " ";
               
               if (organism == result) {
            	    selected = " selected ";
            	    loadTops(organism);
               }
               
               $('#organisms').append('<option ' + selected + '>' + result + '</option>');
           }

           stoppedLoading();
           
        }
    });
}



$(function(){

	$("#topLevelFeatureName").click(function (e) {
		if (loading) {
		    alert("Currently loading");
		}
		var val = $(e.currentTarget).val();
		var length = lengths[val];
		$("#maxLocation").val(length);
	});
    
    $("#organisms").change(function (e) {
    	if (loading) {
            alert("Currently loading");
        }
        var val = $(e.currentTarget).val();
        organism = val;
        loadTops(organism, true);
    }); 

    loadOrganisms();
    
});



</script>


<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form id="geneLocationQueryForm" commandName="query" action="${actionName}" method="GET">
            <table border=0>
                <tr>
                    <td>
                        <br><big><b>Gene Location Search:</b></big>
                        
                    </td>
                    <td>
                     <b>Organism:</b>
                        <select id="organisms" name="organisms">
                            
                        </select>
                    </td>
                    
                    <td>
                        <b>Parent feature:&nbsp;</b>
                        <select id="topLevelFeatureName" name="topLevelFeatureName"></select>
                    </td>
                    <td>
                        <b>Start:</b>
                        <br><form:input id="minLocation" path="min"/>
                        <br><font color="red"><form:errors path="min" /></font>
                    </td>
                    <td>
                        <b>End:</b>
                        <br><form:input id="maxLocation" path="max"/>
                        <br><font color="red"><form:errors path="max" /></font>
                    </td>

                     <td>
                         <b>Pseudogene:</b>
                          <br><form:checkbox id="pseudogenes" path="pseudogenes" />
                      </td>
                      <td>&nbsp;&nbsp;&nbsp;</td>
                    <td>
                        <br><input type="submit" value="Submit" />
                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td colspan=7> <font color="red"><form:errors  /></font></td>
                    <td></td>
                </tr>
            </table>
        </form:form>
    
    <P>Select an organism from the retrieve its list of contigs or chromosomes. <span class="spinner" >&nbsp;&nbsp;&nbsp;</span></P>
    
    </format:genePageSection>
    
    <format:test-for-no-results />
</div>


<br><query:results />
</format:page>