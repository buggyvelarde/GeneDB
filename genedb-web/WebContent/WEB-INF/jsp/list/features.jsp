<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Gene Results List">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
	<script type="text/javascript">
		function forward() {
			var myList = document.getElementById("tmp")
			document.location.href="../genedb-web/" + myList.options[myList.selectedIndex].text
		}
	</script>
</format:header>
<sp:form commandName="rb" name="lst">
	<table align="center" border="0" cellspacing="0" cellpadding="0" width="100%">
	<tr><td colspan="3"><img src="<c:url value="/" />/includes/images/blank.gif" width="100%" height="1" alt="--------"/>
	</tr>
	<tr>
	<td width="40%" bgcolor="#E2E2FF" align="center">
			Search String :
			<form action="<c:url value="/" />/NamedFeature">
			<input type="text" name="lookup"/> 
			<input type="submit" value="Submit"/> 
	</td>
	</form>
	<td width="40%" bgcolor="#E2E2FF" align="center">
			<input type="submit" value="Go To" name="pages">
			<sp:select id="tmp" path="result" items="${rb.results}" onchange="forward()">
			</sp:select>
	</td>
	</tr>
	<tr><td colspan="3"><img src="<c:url value="/" />/includes/blank.gif" width="100%" height="1" alt="--------"/>
	</tr>
	</table>
</sp:form>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<display:table name="results" uid="tmp" pagesize="30" requestURI="/NamedFeature" class="simple" cellspacing="0" cellpadding="4">
   	<display:column property="organism.abbreviation" title="Organism" class="fonts"/>
	<display:column property="cvTerm.name" title="Type"/>
	<display:column property="uniquename" href="./Search/FeatureByName?name=${tmp_rowNum}"/>
</display:table>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<format:footer />