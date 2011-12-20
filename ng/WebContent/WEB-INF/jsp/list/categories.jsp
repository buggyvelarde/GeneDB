<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="misc" uri="misc" %>
<format:header title="Category List" />
<format:page>
<br>

<div id="geneDetails">
  <format:genePageSection id="browseCategory" className="whiteBox">
    <form:form action="category" commandName="${actionName}" method="get">
            <table>
                <tr>
                    <td width=180>
                        <br><big><b>Controlled Curation:&nbsp;</b></big>
                    </td>
                    <td width=180>
                        <b>Organism!${taxonNodeName}:</b>
                        <br><db:simpleselect selection="${taxonNodeName}"/>
                        <br><font color="red"><form:errors path="taxons" /></font>
                    </td>
                    <td width=180>
                        <b>Browse Category:</b>
                        <br>
                            <select name="category" >
                                <c:forEach var="cat" items="${categories}">
                                    <option <c:if test="${ category == cat }"> selected </c:if> >  ${cat}</option>
                                </c:forEach>
                            </select>
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

<misc:url value="/Search" var="url">
	<spring:param name="organism" value="${organism}" />
	<spring:param name="category" value="${category}" />
</misc:url>
  <div id="col-2-1">
	<display:table name="results" id="row" pagesize="30"
		requestURI="/category/${category}" cellspacing="0"
		cellpadding="4" class="search-data-table">
		<display:column title="Category - ${category}">
			<misc:url value="${url}" var="final">
				<spring:param name="term" value="${row.name}" />
			</misc:url>
			<a href="<misc:url value="/Query/controlledCuration"><spring:param name="cvTermName" value="${row.name}" /><spring:param name="taxons" value="${taxons}" /><spring:param name="cv" value="${category}"/></misc:url>">
				<c:out value="${row.name}" /></a>
		</display:column>
		<display:column property="count" title="Count" />
	</display:table>
  </div>

</format:page>
