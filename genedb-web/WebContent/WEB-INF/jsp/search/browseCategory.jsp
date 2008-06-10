<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>

<format:headerRound title="Browse By Category" bodyClass="genePage">
	<st:init />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection id="browseCategory" className="whiteBox">
		<form:form action="BrowseCategory" commandName="browseCategory" method="get">
			<table>
				<tr>
					<td><form:errors path="*" /></td>
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
					<td>Browse category:</td>
					<td><form:select path="category" items="${categories}" /></td>
					<td></td>
			    </tr>
			    <tr>
			    	<td>&nbsp;</td>
				  	<td colspan="2"><input type="submit" value="Submit" /></td>
				  	<td>&nbsp;</td>
			    </tr>
			</table>
		</form:form>
	</format:genePageSection>
</div>
<format:footer />