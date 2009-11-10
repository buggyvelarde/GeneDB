<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Homepage" />
<format:page>
<br>

<style>
.readableText
{
	font-size: 1.2em;
	line-height:1.5em;
	text-align:justify;
}
</style>


  
<div id="col-1-1">  

	<div class="readableText">
	
		<h2>${full} Genome Project</h2>
	
		<c:catch var="e">
			<jsp:include page="organisms/${label}.jsp" />
		</c:catch>
		<c:if test="${e!=null}">
			No content has been written for this organism.
		</c:if>
	
	</div>

</div>
  

<div id="col-1-2">
	
	
	<h2>Links</h2>
	<div class="baby-blue-top"></div>
		<div class="baby-blue">
			
	  		<p>Link 1</p>
		</div>
	<div class="baby-blue-bot"></div>
	<br><br><br>
	<P><I>GENEDB is a database that provides a window into ongoing annotation and curation at the Sanger Institute.</I></P>
</div>




</format:page>
