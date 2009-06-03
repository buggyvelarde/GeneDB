<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:headerRound title="Category List" bodyClass="genePage">
  <st:init />
  <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>



<div id="geneDetails">
  <format:genePageSection id="browseCategory" className="whiteBox">
    <form:form action="BrowseCategory" commandName="browseCategory" method="post">
            <table>
                <tr>
                    <td width=180>
                        <br><big><b>Controlled Curation:&nbsp;</b></big>
                    </td>
                    <td width=180>
                        <b>Organism:</b>
                        <br><db:simpleselect />
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
                <tr>
                    <td></td>
                    <td colspan=2><font color="red"><form:errors  /></td>
                    <td></td>
                </tr>
            </table>

        </form:form>
    </format:genePageSection>
</div>

<c:if test="${not empty results}">
    <c:url value="/Search" var="url">
        <c:param name="organism" value="${organism}"/>
        <c:param name="category" value="${category}"/>
    </c:url>
    <div id="geneResultsPanel">
        <format:genePageSection className="whiteBox">
            <display:table name="results"  id="row" pagesize="30" requestURI="/BrowseCategory" class="simple" cellspacing="0" cellpadding="4">
            <display:column title="Category - ${category}">
                <c:url value="${url}" var="final">
                    <c:param name="term" value="${row.name}"/>
                </c:url>
                <a href="<c:url value="/Query"><c:param name="q" value="controlledCuration" /><c:param name="cvTermName" value="${row.name}" /><c:param name="taxons" value="${taxons}" /><c:param name="cv" value="${category}"/></c:url>"><c:out value="${row.name}"/></a>
            </display:column>
            <display:column property="count" title="Count"/>
            </display:table>
        </format:genePageSection>
    </div>
</c:if>
<format:footer />