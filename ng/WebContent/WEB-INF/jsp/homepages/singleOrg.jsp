<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="${full} Homepage" />
<format:page>
<br>




  
<div id="col-1-1">  
    <div class="readableText">
        <h1>${full}</h1>
        <div id="readableContent">${content}</div>
        
        
    </div>
</div>

<!-- #class="ui-state-default ui-corner-all" style="position:absolute; left:100px;top:200px;display:none; height:500px;width:800px;  background:#fff; overflow-y: scroll ;"
 -->
<div id="activities" >
<!-- new annotations -->
<h1 id="readableActivityTitle" ></h1>
<div style="font-size:small;" id="readableActivity"></div>
</div>

<script>

$(function(){
	var d = new Date();
	d.setDate(d.getDate() - 328);
    var since = d.getFullYear() + "-" + d.getMonth() + "-" + d.getDate();
    var baseHREF = getBaseURL() + "gene/";

    function getAllStatistics() {
	    $.ajax({
	         url: getBaseURL() + "service/changes",
	         type: 'GET',
	         dataType: 'json',
	         data: {
	             'since' : since,
	             'taxon' : "${node.label}"
	         },
	         success: function(response) {
	             if (response.results.hasOwnProperty("feature")) {
	            	 var s = "<table cellpadding=10 cellspacing=10><tr><th>gene</th><th>type</th><th>details</th><th>date</th></tr>";
	                 if (jQuery.isArray(response.results.feature)) {
	                     for (var i in response.results.feature) {
	                    	 //console.log(response.results.feature[i]);
	                    	 var a = "<tr><td><a href='" + baseHREF + response.results.feature[i]["@geneuniquename"] + "' >" + response.results.feature[i]["@geneuniquename"] + "</a></td><td>" + response.results.feature[i]["@type"] + "</td><td>" + response.results.feature[i]["@changedetail"] + "</td><td>" + response.results.feature[i]["@changedate"] +  "</td></tr>" ;
	                    	 s += a;
	                     }
	                     
	                 }
	                 s += "</table>";
	                 
	                 $('#readableActivity').html(s); 
	             }
	             $("#activities").dialog({ width: 700, height: 530 , title :  "Recent annotation activity (28 days)" });
	             
	         }
	     });
    }

    
    $.ajax({
         url: getBaseURL() + "service/changesummary",
         type: 'GET',
         dataType: 'json',
         data: {
             'since' : since,
             'taxon' : "${node.label}"
         },
         success: function(response) {
             if (response.results.hasOwnProperty("summary")) {
            	 
                 var s = "";
                 s+= "<h2>Annotation Statistics</h2>"  
                 s+= "<div class=\"light-grey-top\"></div>"
                 s+= "<div class=\"light-grey\" >"
                 
                 if (response.results.count == 0) {
                	 return;
                 }
                 
                 if (jQuery.isArray(response.results.summary.statistics)) {
                     for (var i in response.results.summary.statistics) {
                         console.log(response.results.summary.statistics[i]);
                         var a = "&raquo; " + response.results.summary.statistics[i]["Annotation type"] + ": " + response.results.summary.statistics[i]["Count"] + "<br/>" ; 
                         s += a;
                     }
                 }
                 s += "<a id='showstats' style='cursor:pointer;' >More details...</a> <br/>";
                 s += "</div>";
                 s += "<div class=\"light-grey-bot\"></div>";
                 
                 $('#col-1-2').append(s);
                 $('#showstats').click(getAllStatistics);
             }
             
         }
     });

    
});
        
</script>
  

<div id="col-1-2">
	
	
	<h2>Utilities</h2>  
    <div class="light-grey-top"></div>
    <div class="light-grey">
        &raquo; <a href="${baseUrl}blast/submitblast/${node.label}">Blast</a> <br />
        &raquo; <a href="${baseUrl}jbrowse/${node.label}/?tracks=Complex%20Gene%20Models/">Jbrowse</a> <br /> 
    </div>
    <div class="light-grey-bot"></div>
    
    
    <h2>Links</h2>

    <div class="baby-blue-top"></div>
        <div class="baby-blue">
            <div id="readableContentLinks" >${links}</div>
        </div>
    <div class="baby-blue-bot"></div>
    

</div>




</format:page>
