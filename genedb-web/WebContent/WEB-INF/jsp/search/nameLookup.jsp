<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Name Search">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection id="nameSearch" className="whiteBox">
		<form:form commandName="nameLookup" action="NamedFeature" method="get">
			<table>
				<tr>
					<td colspan="3">
			      		<font color="red"><form:errors path="*" /></font>
			    	</td>
			    </tr>
			    <tr>
			    	<td>Organisms:</td>
			      	<td>
			      		<select name="organism">
			      			<option value="Plasmodium">Plasmodium</option>
			      			<option value="Pfalciparum">Pfalciparum</option>
			      			<option value="Pberghei">Pberghei</option>
			      			<option value="Pchabaudi">Pchabaudi</option>
			      			<option value="Pknowlesi">Pknowlesi</option>
			      			<option value="Pvivax">Pvivax</option>
			      			<option value="Pyoelii">Pyoelii</option>
			      		</select>
			      	</td>
			    </tr>
			    <tr>
				  <td>Look Up:</td>
				  <td>
				  	<form:input id="textInput" path="name"/>
				  </td>
			    </tr>
			    <tr>
			      <td>&nbsp;</td>
				  <td colspan="2">
				  	<input type="submit" value="Submit" />
				  </td>
				  <td>&nbsp;</td>
			    </tr>
			</table>
		</form:form>
	</format:genePageSection>
</div>
<format:footer />