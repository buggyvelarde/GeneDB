<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<format:headerRound title="Circular Genome ">
	<st:init />
</format:headerRound>
<br>
<form:form action="CircularGenomeForm" method="post" enctype="multipart/form-data">
	<table align="center" width="50%">
		<tr>
			<td><FONT color="red"><form:errors path="*"/></FONT></td>
		</tr>
		<tr>
			<td>Organism :</td>
			<td><form:select items="${organisms}" path="taxon"/></td>
		</tr>
		<tr>
			<td>File upload :</td>
			<td><input id="file" name="file" type="file"/></td>
		</tr>
		<!--   <tr>
			<td>Misc Feature File upload :</td>
			<td><input id="additionalFile" type="file" name="additionalFile"/></td>
		</tr>-->
		<tr>
			<td>Restriction Enzyme :</td>
			<td><form:select items="${digestNames}" path="enzymeName"/></td>
		</tr>
		<tr>
			<td><input type="submit" value="Submit"/></td>
		</tr>
	</table>
</form:form>
<format:footer />