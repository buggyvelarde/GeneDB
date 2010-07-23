<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Homepage" />
<format:page>
<br>




  
<div id="col-1-1">  
    <div class="readableText">
        <h1>${full}</h1>
        <div id="readableContent">${content}</div>
        
        <!-- new annotations -->
        <h1 id="readableActivityTitle" ></h1>
        <div style="font-size:small;" id="readableActivity"></div>
    </div>
</div>

<script>

$(function(){
	var d = new Date();
	d.setDate(d.getDate() - 28);
    var since = d.getFullYear() + "-" + d.getMonth() + "-" + d.getDate();
    var baseHREF = getBaseURL() + "gene/";
     $.ajax({
         url: getBaseURL() + "service/changes",
         type: 'GET',
         dataType: 'json',
         data: {
             'since' : since,
             'taxon' : "${node.label}",
             'type' : 'annotation'
         },
         success: function(response) {
             if (response.results.hasOwnProperty("feature")) {
            	 var s = "<table cellpadding=10 cellspacing=10><tr><th>gene</th><th>details</th><th>date</th></tr>";
                 if (jQuery.isArray(response.results.feature)) {
                     for (var i in response.results.feature) {
                    	 console.log(response.results.feature[i]);
                    	 var a = "<tr><td><a href='" + baseHREF + response.results.feature[i]["@geneuniquename"] + "' >" + response.results.feature[i]["@geneuniquename"] + "</a></td><td>" + response.results.feature[i]["@changedate"] + "</td><td>" + response.results.feature[i]["@changedetail"] +  "</td></tr>" ;
                    	 s += a;
                     }
                     $('#readableActivityTitle').append("Recent annotation activity (28 days)");
                 }
                 s += "</table>";
                 console.log(s);
                 $('#readableActivity').append(s); 
             }
             
         }
     });
});
        
</script>
  

<div id="col-1-2">
	
	
	 <h2>About GeneDB</h2>  
    <div class="light-grey-top"></div>
    <div class="light-grey">
        <P><I>GeneDB is a database that provides a window into ongoing annotation and curation at the Sanger Institute.</I></P>
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
