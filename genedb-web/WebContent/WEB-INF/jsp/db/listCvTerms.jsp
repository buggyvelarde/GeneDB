<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<format:header name="Generic Feature Page">
<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<%-- 
<table>
	<c:forEach items="${cvTerms}" var="cvTerm">
		<tr>
			<td><a href="./CvTermByCvName?cvTermName=${cvTerm.name}&cvName=${cvTerm.cv.name}">${cvTerm.name}</a></td>
			<td>${cv.definition}</td>
			<td>${cvTerm.cv.name}</td>
			<td><a href="./GenesByCvTermAndCvName?cvTermName=${cvTerm.name}&cvName=${cvTerm.cv.name}">Get Genes with which contains this cvterm</a></td>
		</tr>
	</c:forEach>
</table>
--%>
<display:table name="cvTerms" uid="tmp" pagesize="30" requestURI="/Search/CvTermByCvName" class="simple" cellspacing="0" cellpadding="4" decorator="org.displaytag.sample.Wrapper">
   	<display:column property="link1" title="Name"/>
   	<display:column property="definition" title="Definition"/>
	<display:column property="cv.name" title="Cv"/>
	<display:column property="link2"/>
</display:table>

<format:footer />
