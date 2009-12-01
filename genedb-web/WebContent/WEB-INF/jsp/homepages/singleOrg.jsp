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
	margin-left:1em;
}
.readableText a
{
    color:#4381a7;
}
ul {
    padding-left:1em;
}
</style>


  
<div id="col-1-1">  

	<div class="readableText">
	
		<h1>${full} Genome Project</h1>
	
		<c:catch var="e">
			<jsp:include page="organisms/${label}.jsp" />
		</c:catch>
		<c:if test="${e!=null}">
		  The ${full} genome has been loaded into GeneDB. At this moment, we have not yet 
		  entered the details of this sequencing project. If you are one of the contributors
		  to this project and you would like to help with this page, 
		  then please get <a href="mailto:webmaster@genedb.org">email us</a> with some copy, which
		  we can then edit to make more consistent with the rest of the site.  
		</c:if>
	
	</div>

</div>
  

<div id="col-1-2">
	
	
	<h2>About GeneDB</h2>
	<div class="baby-blue-top"></div>
		<div class="baby-blue">
			<P><I>GENEDB is a database that provides a window into ongoing annotation and curation at the Sanger Institute.</I></P>
		</div>
	<div class="baby-blue-bot"></div>
	
</div>




</format:page>
