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
    /* filter: progid:DXImageTransform.Microsoft.BasicImage(rotation=3); */
    filter: progid:DXImageTransform.Microsoft.Matrix(sizingMethod='auto expand', M11=0.7071067811865476, M12=-0.7071067811865475, M21=0.7071067811865475, M22=0.7071067811865476); /* IE6,IE7 */
    -ms-filter: "progid:DXImageTransform.Microsoft.Matrix(SizingMethod='auto expand', M11=0.7071067811865476, M12=-0.7071067811865475, M21=0.7071067811865475, M22=0.7071067811865476)"; /* IE8 */
    position:relative;
    /* border:1px solid black; */
    bottom:5px;
    left:5px;
    width:30px;
    font-size:1.2em;
}


</style>

<div id="chromosome-container" style="padding-left:50px;" ></div>

<script>

$(function(){
	$("#chromosome-container").ChromosomeMap({
    	"region" : "${region}", 
    	"overideUseCanvas" : true,
    	'click' : function(event) {
    		if (event.target.title) {
    			window.location = getBaseURL() + "gene/" + event.target.title;
    		}
       } 
    });
    
});
</script>




</format:page>