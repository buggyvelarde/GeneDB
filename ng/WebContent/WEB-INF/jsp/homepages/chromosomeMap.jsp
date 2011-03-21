<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Chromosome view" />
<format:page>
<script type="text/javascript" src="<misc:url value="/includes/scripts/jquery/Modernizr.js"/>"></script>
<script type="text/javascript" src="<misc:url value="/includes/scripts/genedb/chromosoml.js"/>"></script>


<h2><center>High-Level map of ${full} region ${region}</center></h2>

<style>


#chromosome-container {
    margin-top:30px;
}

.chromosome_feature {
    cursor:pointer;
     filter:alpha(opacity=50);
     -moz-opacity:0.5;
     -khtml-opacity: 0.5;
     opacity: 0.5;
}


.chromosome_feature_text {
    -webkit-transform: rotate(-45deg);
    -moz-transform: rotate(-45deg);
    filter: progid:DXImageTransform.Microsoft.BasicImage(rotation=3);
    position:relative;
    /* border:1px solid black; */
    bottom:5px;
    left:5px;
    width:30px;
    font-size:1.2em;
}


</style>

<!-- <div style="padding:5px" class="buttons">
    <button id="previous">previous</button> 
    <button style="margin-left:750px;" id="next">next</button>
</div> -->
<div id="chromosome-container" style="padding-left:50px;" ></div>

<script>

$(function(){
	$("#chromosome-container").ChromosomeMap({
    	"region" : "${region}", 
    	"overideUseCanvas" : true,
    	'click' : function(event) {
    		console.log(event.target.title);
    		if (event.target.title) {
    			window.location = getBaseURL() + "gene/" + event.target.title;
    		}
       } 
    });
	
	/* $('#next').click(function(event) {
		$("#chromosome-container").ChromosomeMap('next');
	}).button();
	$('#previous').click(function(event) {
        $("#chromosome-container").ChromosomeMap('previous');
    }).button(); */
    
});
</script>




</format:page>