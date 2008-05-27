<%@ tag display-name="go" 
        body-content="scriptless" dynamic-attributes="fMap" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/genedb/formatting" %>
<c:forEach var="f" begin="0" items="${gMap}">
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
  	<span><b>Gene Ontology</b></span>
   	<table>
   		<tr>
   			<th><b>Biological Process</b></th>
   			<td>
   				<table>
				<format:go-section title="Biological Process"
					cvName="biological_process" feature="${polypeptide}" />
				</table>
			</td>
		</tr>
	</table>
	<table>
		<tr>
   			<th><b>Cellular Component</b></th>
   			<td>
   				<table>
				<format:go-section title="Cellular Component"
					cvName="cellular_component" feature="${polypeptide}" />
				</table>
			</td>
		</tr>
	</table>
	<table>
		<tr>
   			<th><b>Molecular Function</b></th>
   			<td>
   				<table>
				<format:go-section title="Molecular Function"
					cvName="molecular_function" feature="${polypeptide}" />
				</table>
			</td>
		</tr>
	</table>
  </div>
  <b class="round">
  <b class="round5"></b>
  <b class="round4"></b>
  <b class="round3"></b>
  <b class="round2"><b></b></b>
  <b class="round1"><b></b></b></b>
 </div>