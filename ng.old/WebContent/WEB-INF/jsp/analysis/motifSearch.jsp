<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="db" uri="db" %>
<format:header title="GeneDB Motif Search">
	<st:init />
</format:header>
<format:page>
<html>
  <head>
     <title>GeneDB Motif Search</title>
     <meta name="description" content="Identify protein sequences within
     GeneDB which match a particular pattern">

<sp:form action="MotifSearch" method="post">
	<table align="center" width="50%">
		<tr>
			<td>Organism: </td>
			<td><sp:input path="taxon"/></td>
		</tr>
		<tr>
			<td>Pattern: </td>
			<td><sp:input path="pattern" size="30" /></td>
		</tr>
		<tr>
			<td>Type: </td>
			<td><sp:radiobutton path="type" />Nucleotide
			<sp:radiobutton path="type" />Protein</td>
		</tr>
		<tr>
			<td><input type="submit" value="Submit"/></td>
			<td><input type="reset" value="Reset"/></td>
		</tr>
	</table>

	<st:section name="Advanced" id="motif_advanced" collapsed="false" collapsible="true">

		<table align="center" width="50%">
			<tr>
				<td><sp:checkbox path="extJrev" /></td>
				<td>J: </td>
				<td><sp:input path="extJ"/></td>
			</tr>
			<tr>
				<td><sp:checkbox path="extXrev" /></td>
				<td>X: </td>
				<td><sp:input path="extX"/></td>
			</tr>
			<tr>
				<td><sp:checkbox path="extensionCreverse" /></td>
				<td>C: </td>
				<td><sp:input path="extensionC"/></td>
			</tr>
		</table>

	</st:section>



</sp:form>


<table>
<tr>
<td>

Organism:
</td>
<td>
</tr>

</table>

<table width="100%">
	<tr valign="center">
	  <td align="center">
	    <INPUT TYPE="submit" VALUE="Start Motif Search">
	    &nbsp;&nbsp;&nbsp;&nbsp;<INPUT TYPE="reset">
	    <BR><BR>
	  </td>
	</tr>
</table>
<!-- </FORM> -->

</format:page>