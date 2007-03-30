<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Lucene Search">
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
<sp:form name="lucene" commandName="luceneSearch" action="LuceneSearch" method="post" onsubmit="check(this)">
	<table align="center" width="50%">
		<tr>
			<td>Fields: 
				<sp:select path="field">
					<sp:option value="ALL" label="ALL"/>
					<sp:options items="${fields}"/>
				</sp:select>
			</td>
			<td>Search String: <sp:input path="query" /></td>
			<td><input type="submit" value="Submit" /></td>
		</tr>
	</table>
</sp:form>
<format:footer />