<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:header title="Information : Data Release Policy" />
<format:page>
<br>

<style>

#homepage-text p, #homepage-text ul {
    padding-bottom:1em;
}

#homepage-text li {
    margin-left:2em;
    padding-left:2em;
}

</style>

<h1>Data Release Policy</h1>

<div id="col-2-1">
<format:genePageSection>

<jsp:include page="wiki/Data_Release_Policy.jsp" />

</format:genePageSection>

</div>
</format:page>
