<%@ tag display-name="box"
        body-content="scriptless" %>
<%@ attribute name="nest" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="query" tagdir="/WEB-INF/tags/genedb/query" %>

<c:set var="colour" value="#CC0000" />
<c:if test="${nest}"><c:set var="colour" value="#C0C0C0" /></c:if>

<!-- <div class='informationMacroPadding' align="center"> -->
<table cellpadding='5' width='100%' cellspacing='0' class='infoMacro' border='0'>
<tr>
<td bgcolor="${colour}">&nbsp;</td>
<td bgcolor="${colour}">
<jsp:doBody />
</td><td bgcolor="${colour}">&nbsp;</td>
</tr></table>
