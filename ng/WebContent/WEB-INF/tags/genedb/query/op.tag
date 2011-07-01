<%@ tag display-name="box"
        body-content="empty" %>
<%@ attribute name="op" required="true" %>
<%@ attribute name="nest" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="query" tagdir="/WEB-INF/tags/genedb/query" %>

<c:set var="colour" value="#CC0000" />
<c:if test="${nest}"><c:set var="colour" value="#C0C0C0" /></c:if>

</td><td bgcolor="${colour}">&nbsp;</td></tr>
<tr><td colspan="2" bgcolor="${colour}"><b>${op}</b></td>
<td bgcolor="${colour}">
<query:expansionIcons />
</td>
</tr>
<tr>
<td bgcolor="${colour}">&nbsp;</td><td bgcolor="${colour}">
