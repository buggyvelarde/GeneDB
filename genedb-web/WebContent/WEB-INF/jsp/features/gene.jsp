<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Feature: ${dto.uniqueName}" />
<format:page>
<br>

<div id="col-2-1">
<!-- <h1>ID here and imagemap</h1> -->
<img src="images/sequence-data.jpg" height="101" width="916" alt="Sequence map" />
<br /><br />

<div id="geneDetails">
    <jsp:include page="geneDetails.jsp"/>
</div>


</format:page>
