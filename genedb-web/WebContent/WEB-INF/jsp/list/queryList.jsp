<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:header title="Query List" />
<format:page>
<br>




<div id="col-2-1">

<format:genePageSection>

<h2>GeneDB query list</h2>

<P>The following queries are available from the GeneDB website.</P>



<table cellpadding="0" cellspacing="4" border="0" class="sequence-table">
<c:forEach items="${queries}" var="query">
<tr><th><a href="<c:url value="/Query/" />${query.key}?taxons=">${query.key}</a></th><td>${query.value.queryDescription}</td></tr>
</c:forEach>
</table>
</format:genePageSection>
</div>
</format:page>
