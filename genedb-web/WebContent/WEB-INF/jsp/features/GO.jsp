<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>
<format:header name="Gene Ontology Search">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<sp:form >
	<table align="center" border="0" cellspacing="0" cellpadding="0" width="100%">
	<tr><td colspan="3"><img src="<c:url value="/" />/includes/images/blank.gif" width="100%" height="1" alt="--------"/>
	</tr>
	<tr>
	<td width="40%" bgcolor="#E2E2FF" align="center">
			Search String :
			<form action="<c:url value="/" />/GoFeature">
			<input type="text" name="lookup"/> 
			<input type="submit" value="Submit"/> 
			</form>
	</td>
	</tr>
	<tr><td colspan="3"><img src="<c:url value="/" />/includes/blank.gif" width="100%" height="1" alt="--------"/>
	</tr>
	<tr align="left">
  		<td align="left">GO accession number: <c:out value="${goNumber}"></c:out>
  	</tr>
  	<tr>
  		 <td align="left">GO Term Name: <c:out value="${termName}"></c:out>
  	</tr>
	</table>
</sp:form>
<table width="100%" class="simple">
  <tr align="right">
	<td align="right"><a href="../gviewer.jsp?length=<c:out value="${length}"/>">Click here for GViewer Display</a></td>
  </tr>
</table>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<display:table name="results" uid="count" pagesize="30" requestURI="/GoFeature" class="simple" cellspacing="0" cellpadding="4">
	<display:column property="organism.abbreviation" title="Organism" class="fonts"/>
	<display:column property="cvTerm.name" title="Type"/>
	<display:column property="uniqueName" href="./Search/FeatureByName" paramId="name"/>
</display:table>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<format:footer />