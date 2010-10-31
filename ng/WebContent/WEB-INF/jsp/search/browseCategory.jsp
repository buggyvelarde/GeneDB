<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="db" uri="db" %>
<format:header title="Browse By Category" />
<format:page>

<div id="geneDetails">
  <format:genePageSection id="browseCategory" className="whiteBox">
    <form:form action="BrowseCategory" commandName="${actionName}" method="get">
            <table>
                <tr>
                    <td width=180>
                        <br><big><b>Controlled Curation:&nbsp;</b></big>
                    </td>
                    <td width=180>
                        <b>Organism:</b>
                        <br><db:simpleselect selection="${taxonNodeName}"/>
                        <br><font color="red"><form:errors path="taxons" /></font>
                    </td>
                    <td width=180>
                        <b>Browse Category:</b>
                        <br><form:select path="category" items="${categories}" />
                        <br><font color="red"><form:errors path="category" /></font>
                    </td>
                    <td>
                        <br><input type="submit" value="Submit" />
                    </td>
                </tr>
                <c:if test="${noResultFound}">
	                <tr>
	                    <td></td>
	                    <td colspan=2><font color="red">No Result Found.</td>
	                    <td></td>
	                </tr>
                </c:if>
            </table>

        </form:form>
    </format:genePageSection>
</div>

</format:page>
