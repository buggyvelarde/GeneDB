<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Name Search">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection id="nameSearch" className="whiteBox">
		<form:form commandName="nameLookup" action="NameSearch" method="get">
			<table>
				<tr>
					<td colspan="3">
			      		<font color="red"><form:errors path="*" /></font>
			    	</td>
			    </tr>
			    <tr>
			    	<td>Organisms:</td>
			      	<td>
			      		<form:select path="organism" multiple="false">
			      			<form:option value="Plasmodium"/>
			      			<form:option value="Pfalciparum"/>
			      			<form:option value="Pberghei_3x"/>
			      			<form:option value="Pchabaudi"/>
			      			<form:option value="Pknowlesi"/>
			      			<form:option value="Pvivax"/>
			      			<form:option value="Pyoelii"/>
			      		</form:select>
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