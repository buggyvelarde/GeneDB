<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<format:header title="Location Search" />
<format:page>
<br />

<script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/jquery/jquery.spinner.js"/>"></script>

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
 <c:otherwise>
    <script>var organism = null;</script>
 </c:otherwise>
</c:choose>



<script>

var lengths = {};
var loading = false;
var opts = { height: 11, width: 43, position : 'right', img: '<misc:url value="/includes/image/spinner.gif"/>"' };

function loadTops(organism) {
	$("#spinner").spinner(opts);
	loading = true;
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

       		$("#spinner").spinner('remove');
       		loading = false;
       	
        }
	});

}

function loadOrganisms() {
	$("#spinner").spinner(opts);
	loading = true;
	$.ajax({
        url: getBaseURL() + "service/organisms",
        type: 'GET',
        dataType: 'json',
        success: function(response) {

		   var loadingTops = false;
		   
           for (var i in response.results.organisms.string) {
               var result = response.results.organisms.string[i];
               
               var selected = " ";
               
               if (organism == result) {
            	    selected = " selected ";
            	    loadTops(organism);
            	    loadingTops = true;
               }
               
               $('#organisms').append('<option ' + selected + '>' + result + '</option>');
           }

           if (loadingTops != true) {
        	    $("#spinner").spinner('remove');
        	    loading = false;
           }
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
    
    <P>Select an organism from the retrieve its list of contigs or chromosomes. <span id="spinner" style="padding:5px;" >&nbsp;&nbsp;&nbsp;</span></P>
    
    </format:genePageSection>
    
    <format:test-for-no-results />
</div>


<br><query:results />
</format:page>