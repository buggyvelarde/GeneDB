<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="GO term Search">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
	<script type="text/javascript">
		function redirect() {
			var lookup = document.forms(0).lookup.value()
			document.location.href="/lookup=" + lookup
		}
	</script>
</format:header>

<c:forEach items="${status}" var ="errorMessage"> 
<font color="red">
<c:out value ="${errorMessage}"/><br>
</font>
</c:forEach>

<sp:form commandName="goLookup" action="GoFeature" method="post">
	<table align="center" width="50%">
		<tr>
			<td>Look Up :</td>
			<td><sp:input path="lookup"/></td>
		</tr>
		<tr>
			<td><input type="submit" value="Submit"/> <input type="reset"/></td>
		</tr>
	</table>
</sp:form>
<format:footer />