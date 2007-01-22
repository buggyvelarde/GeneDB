<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Circular Genome ">
	<st:init />
</format:header>

<c:forEach items="${status}" var ="errorMessage"> 
<font color="red">
<c:out value ="${errorMessage}"/><br>
</font>
</c:forEach>

<sp:form action="CircularGenomeForm" method="post">
	<table align="center" width="50%">
		<tr>
			<td>Organism :</td>
			<td><sp:input path="taxon"/></td>
		</tr>
		<tr>
			<td>Restriction Enzyme :</td>
			<td><sp:select items="${digestNames}" path="enzymeName"/></td>
		</tr>
		<tr>
			<td><input type="submit" value="Submit"/></td>
		</tr>
	</table>
</sp:form>
<format:footer />