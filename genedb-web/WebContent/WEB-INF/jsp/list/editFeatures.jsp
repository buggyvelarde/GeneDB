<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="misc" uri="misc" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Gene Results List">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
	<script type="text/javascript">
		function forward() {
			var myList = document.getElementById("tmp")
			document.location.href="../genedb-web/NameFeature?lookup=SPA*&orglist=" + myList.options[myList.selectedIndex].text
		}
	</script>
</format:header>
	<table align="center" border="0" cellspacing="0" cellpadding="0" width="100%">
	<tr><td colspan="3"><img src="<c:url value="/" />/includes/images/blank.gif" width="100%" height="1" alt="--------"/>
	</tr>
	<!-- 
	<tr>
		<td width="40%" bgcolor="#E2E2FF" align="center">
			<form action="NameFeature">
			Search String :
			<input type="input" name="lookup" />
			<input type="hidden" name="orglist" value="spombe"/>
			<input type="submit" value="Submit"/> 
			</form>
		</td>
	</tr> -->
	<tr><td colspan="3"><img src="<c:url value="/" />/includes/blank.gif" width="100%" height="1" alt="--------"/>
	</tr>
	</table>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<p><a href="<misc:history />">Store these results in my history</a></p>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<form>
<display:table name="results" uid="tmp" pagesize="30" requestURI="/NamedFeature" class="simple" cellspacing="0" cellpadding="4">
   	<display:column title="Include"><input type="checkbox" checked="checked"></display:column>
   	<display:column property="organism.abbreviation" title="Organism"/>
   	<display:column property="cvTerm.name" title="Type"/>
	<display:column property="uniqueName" href="./NamedFeature" paramId="name"/>
</display:table>
</form>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<format:footer />