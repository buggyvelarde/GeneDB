<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Homepage for ${fulltext}" />

<script type="text/javascript" src="<misc:url value="/includes/scripts/jquery/Modernizr.js"/>"></script>
<script type="text/javascript" src="<misc:url value="/includes/scripts/genedb/chromosoml.js"/>"></script>
<script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/jquery/jquery.spinner.js"/>"></script>

<format:page>
<br>

<style>

.region, .region_type {
    width:100%;
    display:block;
}


</style>

<script>

$(function(){

    var defaultDateOffset = 28;
    
    var d = new Date();
    d.setDate(d.getDate() - defaultDateOffset);
    var since = d.getFullYear() + "-" + ( d.getMonth() + 1 ) + "-" + d.getDate();
    var baseHREF = getBaseURL() + "gene/";
    
    
    function getAllStatistics(sincedate) {

        var thissince = sincedate.getFullYear() + "-" + ( sincedate.getMonth() + 1 ) + "-" + sincedate.getDate();
        
        $.ajax({
             url: getBaseURL() + "service/changes",
             type: 'GET',
             dataType: 'json',
             data: {
                 'since' : thissince,
                 'taxon' : "${node.label}"
             },
             success: function(response) {
                 var s = "<table cellpadding=10 cellspacing=10><tr><th>gene</th><th>type</th><th>details</th><th>date</th></tr>";
                 if (response.results.hasOwnProperty("feature")) {
                     
                     if (jQuery.isArray(response.results.feature)) {
                         for (var i in response.results.feature) {
                             //console.log(response.results.feature[i]);
                             var a = "<tr><td><a style='text-decoration:underline;' href='" + baseHREF + response.results.feature[i]["@geneuniquename"] + "' >" + response.results.feature[i]["@geneuniquename"] + "</a></td><td>" + response.results.feature[i]["@type"] + "</td><td>" + response.results.feature[i]["@changedetail"] + "</td><td>" + response.results.feature[i]["@changedate"] +  "</td></tr>" ;
                             s += a;
                         }
                         
                     }
                    
                 }
                 s += "</table>";
                 $('#readableActivity').html(s);
                  
                 $("#activities").dialog({ width: 700, height: 530 , title :  "Recent annotation activity (since " + thissince + " , count " + response.results["@count"] + ")" });
                 $("#datepicker").datepicker({
                        maxDate: '+0D', 
                        dateFormat: 'yy-mm-dd',  
                        selectedDate: sincedate,
                        onSelect: function(dateText, inst) {
                            console.log(dateText);
                            var newDate = new Date(inst.selectedYear, inst.selectedMonth, inst.selectedDay);
                            console.log(newDate);
                            //console.log(inst);
                            getAllStatistics(newDate);
                        } 
                 });
                 $("#datepicker").val(thissince);
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
                 //s+= "<h2>Annotation Statistics</h2>"  
                 //s+= "<div class=\"light-grey-top\"></div>"
                 //s+= "<div class=\"light-grey\" >"
                 s+="Since " + since + "<br>";
                 
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
                 //s += "</div>";
                 //s += "<div class=\"light-grey-bot\"></div>";
                 
                 $('#annotation_statistics').html(s);
                 $('#showstats').click(function () {
                     getAllStatistics(d);
                 });
             }
             
         }
     });

    
    $('a#about').click (function(event) {
    	$("#readableContent").dialog({ width: 700, height: 530 , title :  "About ${full} on GeneDB" });
    });
    
});
        
</script>
  

<script>





$(function(){
	
	
	var spinMeister = new SpinnerManager("#spinner", { 
        height : 10.5,
        width : 50,
        position : 'right',
        img : '<misc:url value="/includes/image/spinner.gif"/>'
    });
	
	//spinnerManager.addCall();
	
	
	$("#regions").ChromosomePicker(
		{
			"organism" : "com:${node.label}",
			"on_select" : function(region) {
				window.location = getBaseURL()
					+ "Homepage/${node.label}?region=" + region;
			},
			"spinner" : spinMeister
		});
 
	}); 
</script>






  





<div id="activities" style="display:none;" class="ui-state-default ui-corner-all">
<div style="text-align:right">Change date: <input type="text" id="datepicker" ></div>
<!-- new annotations -->
<div style="font-size:small;" id="readableActivity"></div>
</div>

<center><h1>The ${full} homepage on GeneDB</h1></center>

<div id="col-1-2">
    
    <h2>Annotation statistics</h2>
    
    <div class="light-grey-top"></div>
    <div class="light-grey">
        <div id='annotation_statistics'>
            None available.
        </div> 
    </div>
    <div class="light-grey-bot"></div> 
    
    <h2>Scaffolds <span id="spinner" >&nbsp;&nbsp;&nbsp;</span> </h2>

    <div class="baby-blue-top"></div>
        <div class="baby-blue">
            
            
                        
            <div id ="regions" ></div>
            
        </div>
    <div class="baby-blue-bot"></div>
    
    <h2>Information</h2>
    
    <div class="light-grey-top"></div>
    <div class="light-grey">
        
        <p class="block-para">About <br />
        &raquo; <a id="about">${full} on GeneDB </a> <br />
        
        </p> 
    </div>
    <div class="light-grey-bot"></div> 

</div>




<div id="col-2-2">
    
    
    <h2>Utilities</h2>  
    <div class="baby-blue-top"></div>
        <div class="baby-blue">
        <p class="block-para">Blast <br />
        &raquo; <a href="${baseUrl}blast/submitblast/GeneDB_${node.label}">Blast ${full} </a> <br />
        &raquo; <a href="<misc:url value="/blast/submitblast/GeneDB_proteins/omni" />">Multi-organism (proteins)</a><br />
        &raquo; <a href="<misc:url value="/blast/submitblast/GeneDB_transcripts/omni" />">Multi-organism (transcripts and contigs/chromosomes)</a><br />
        </p>
        <br />
        <p class="block-para">Tools <br />
        &raquo; <a href="http://www.genedb.org/web-artemis/" > Web artemis</a> <br />
        &raquo; <a href="${baseUrl}jbrowse/${node.label}/?tracks=Complex%20Gene%20Models/">Jbrowse</a> <br />
        &raquo; <a href="<misc:url value="/cgi-bin/amigo/go.cgi"/>">AmiGO</a><br />
        </p>
        <br /> 
        <P class="block-para">Searches <br />
            <c:forEach items="${queries}" var="query">
            
            &raquo; <a title="${query.queryDescription}" href="<misc:url value="/Query/${query.realName}" />?taxonNodeName=${node.label}">${query.queryName}</a> <br />
            
            </c:forEach>
            </P>
    </div>
    <div class="baby-blue-bot"></div>
    

</div>

<div id="col-3-2">
    
    
    
    <h2>Links</h2>
    <div class="light-grey-top"></div>
    <div class="light-grey">
        <div id="readableContentLinks" >${links}</div> 
    </div>
    <div class="light-grey-bot"></div>
    

</div>

<div id="readableContent" style="display:none;" class="ui-state-default ui-corner-all">
${content}
</div>


</format:page>
