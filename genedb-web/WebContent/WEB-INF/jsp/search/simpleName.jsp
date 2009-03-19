<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Name/Product Search">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection id="nameSearch" className="whiteBox">
		<form:form commandName="query" action="Query" method="post">
        <input type="hidden" name="q" value="simpleName" />
            <table>            
            	<tr>
            		<td width=180>
            			<br><big><b>Organism Search:&nbsp;</b></big>
            		</td>
            		<td width=180>
            			<b>Organism:</b>
            			<br><db:simpleselect />
            			<br><font color="red"><form:errors path="taxons" /></font>
            		</td>
            		<td width=180>
            			<b>Name:</b>
            			<br><form:input id="simpleName" path="search"/>
            			<br><font color="red"><form:errors path="search" /></font>
            		</td>
            		<td>            			
            			<br><input type="submit" value="Submit" />           
            		</td>
            	</tr>
            	<tr>
            		<td></td>
            		<td colspan=3><font color="red"><form:errors  /></td>
            		<td></td>
            	</tr>
            </table>

		</form:form>
	</format:genePageSection>
	
</div>

<%--<br><query:results />--%>
<format:footer />