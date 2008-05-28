<%@ tag display-name="controlledcuration" 
        body-content="scriptless" dynamic-attributes="fMap" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/genedb/formatting" %>

<c:forEach var="f" begin="0" items="${cMap}">
		<c:set var="polypeptide" value="${f.value}" />	
</c:forEach>
<div class="outer">
<b class="round">
  <b class="round1"><b></b></b>
  <b class="round2"><b></b></b>
  <b class="round3"></b>
  <b class="round4"></b>
  <b class="round5"></b></b>
  <div class="inner">
  	<span><b>Controlled Curation</b></span>
   	<table>
		<format:go-section title="Controlled Curation"
				cvName="CC_genedb_controlledcuration" feature="${polypeptide}" />
	</table>
  </div>
  <b class="round">
  <b class="round5"></b>
  <b class="round4"></b>
  <b class="round3"></b>
  <b class="round2"><b></b></b>
  <b class="round1"><b></b></b></b>
 </div>