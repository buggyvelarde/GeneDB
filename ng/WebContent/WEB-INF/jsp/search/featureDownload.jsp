<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Feature Download">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection id="featureDownload" className="whiteBox">
		<form:form commandName="featureDownload" action="FeatureDownload" method="get">
			<table>
				<tr>
					<td colspan="3">
			      		<font color="red"><form:errors path="*" /></font>
			    	</td>
			    </tr>
			    <tr>
			    	<td>Feature Name:</td>
			      	<td>
			      		<form:input id="textInput" path="featureName"/>
			      	</td>
			    </tr>
			    <tr>
					<td>Feature Type:</td>
					<td>
						<form:input id="textInput" path="featureType"/>
					</td>
			    </tr>
			    	<td>Download Type:</td>
					<td><form:select path="downloadType" items="${downloadTypes}" /></td>
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