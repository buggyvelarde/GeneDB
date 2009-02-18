<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Protein Length Search">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>

<br>
<div id="geneDetails">
	<format:genePageSection id="nameSearch" className="whiteBox">
		<form:form commandName="query" action="Query" method="post">
        <input type="hidden" name="q" value="proteinLength" />
            <table>
            	<tr>
            		<td width=180>
            			<br><big><b>Protein Length Search</b></big>
            		</td>
            		<td width=180>
            			<b>Minimum Length:</b>
            			<br><form:input id="minProteinLength" path="min"/>
            			<br><font color="red"><form:errors path="min" /></font>
            		</td>
            		<td width=180>
            			<b>Maximum Length:</b>
            			<br><form:input id="maxProteinLength" path="max"/>
            			<br><font color="red"><form:errors path="max" /></font>
            		</td>
            		<td>            			
            			<br><input type="submit" value="Submit" />           
            		</td>
            	</tr>
            	<tr>
            		<td></td>
            		<td colspan=2><font color="red"><form:errors  /></td>
            		<td></td>
            	</tr>
            </table>

		</form:form>
	</format:genePageSection>
</div>

<c:if test="${ not empty results}">
	<br>
	<c:url value="BrowseTerm" var="url">
		<c:param name="category" value="${category}"/>
	</c:url>
	<div id="geneDetails">
		<format:genePageSection className="whiteBox">
			<display:table name="results"  id="row" pagesize="30" requestURI="/Query" class="simple" cellspacing="0" cellpadding="4">
		   		<display:column title="Systematic ids">
		   			<c:url value="${url}" var="final">
						<c:param name="term" value="${row}"/>
			   		</c:url>
					<a href="<c:url value="/NamedFeature"/>?name=${row}"><c:out value="${row}"/></a>
			   	</display:column>
			</display:table>
		</format:genePageSection>
	</div>
</c:if>
<br>
<format:footer />