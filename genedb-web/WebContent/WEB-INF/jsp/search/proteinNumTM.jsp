<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Number of TM domains Search">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection id="nameSearch" className="whiteBox">
		<form:form commandName="query" action="Query" method="post">
        <input type="hidden" name="q" value="proteinNumTM" />
            <table>
                <tr>
            	  <td>
            		<br><big><b>Protein Number TM Search:</b></big>
            	  </td>
                  <td><b>Organism:</b>
                    <br><db:simpleselect />
                  </td>
                  <td>
                  	<b>Min number of domains:</b>
                  	<br><form:input id="minNumTM" path="min"/>
            		<br><font color="red"><form:errors path="min" /></font>
                  </td>
                  <td>
                  	<b>Max number of domains:</b>
                    <br><form:input id="maxNumTM" path="max"/>
            		<br><font color="red"><form:errors path="max" /></font>
                  </td>
                  <td>
                    <br><input type="submit" value="Submit" />
                  </td>
                </tr>
            	<tr>
            		<td></td>
            		<td colspan=5><font color="red"><form:errors  /></td>
            		<td></td>
            	</tr>
            </table>

		</form:form>
	</format:genePageSection>
</div>

<br><query:results />
<format:footer />