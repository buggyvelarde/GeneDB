<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Name Search">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
	<script type="text/javascript">
		function check(form) {
			if(form.query.value == ""){
			//document.writeln("look up is ...");
			form.query.value = "Please enter search text ...";
			}
		}
	</script>
</format:header>

<c:forEach items="${status}" var ="errorMessage"> 
<font color="red">
<c:out value ="${errorMessage}"/><br>
</font>
</c:forEach>
<sp:form commandName="nameLookup" action="NameFeature" method="post" onsubmit="check(this)">
	<table align="center" width="50%">
		<tr>
			<td>Organisms: 
				<sp:select path="organism">
					<sp:option value="ALL" label="ALL"/>
					<sp:options items="${organisms}"/>
				</sp:select>
			</td>
			<td>Look Up : <sp:input path="lookup"/></td>
			<td><input type="submit" value="Submit" /></td>
		</tr>
	</table>
</sp:form>
<format:footer />