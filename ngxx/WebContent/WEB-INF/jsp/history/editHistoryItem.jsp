<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/genedb/formatting" %>
<format:header title="Edit History Item">
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/historyEdit2.js"/>"></script>
</format:header>
<format:page>

<br>

<div id="col-2-1">

<form action="<misc:url value="/History/${history}"/>" method="POST">
<input type="hidden" name="historyVersion" value="${historyVersion}" >
<display:table name="items"  id="row" pagesize="30" requestURI="/Results" class="search-data-table" sort="external" cellspacing="0" cellpadding="4" partialList="true" size="${fn:length(items)}">
    <display:column title="Delete"><input type="checkbox" name="item${row_rowNum}"></display:column>
    <display:column title="Systematic ids" style="width: 100px;">
        <%--<a href="<misc:url value="/ResultsNavigator"/>?index=${row_rowNum+firstResultIndex-1}&resultsLength=${fn:length(results)-1}&key=${key}">${row}</a>--%>
        ${row}
    </display:column>
</display:table>

<p>&nbsp;</p>
<a href="javascript:checkAll()" >Check all</a>
<a href="javascript:uncheckAll()" >Uncheck all</a>
<a href="javascript:toggle()" >Toggle selection</a>
<p>&nbsp;</p>
<p>Return <a href="<misc:url value="/History" />">to History page</a> or
<input type="submit" value="Delete checked entries">
</p>

</form>

</div>

</format:page>
