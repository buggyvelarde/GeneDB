<%@ tag display-name="predictedpep" 
        body-content="scriptless" dynamic-attributes="fMap" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/genedb/formatting" %>
<c:forEach var="f" begin="0" items="${pMap}">
		<c:set var="polyprop" value="${f.value}" />	
</c:forEach>
<div id="two">
<b class="round">
  <b class="round1"><b></b></b>
  <b class="round2"><b></b></b>
  <b class="round3"></b>
  <b class="round4"></b>
  <b class="round5"></b></b>
  <div class="inner" style="height:160px">
  	<span><b>Predicted Peptide Properties</b></span>
   	<table class="simple">
		<tr>
			<td><b>Isoelectric Point</b></td>
			<td>pH ${polyprop.isoelectricPoint}</td>
		</tr>
		<tr>
			<td><b>Mass</b></td>
			<td>${polyprop.mass} kDa</td>
		</tr>
		<tr>
			<td><b>Charge</b></td>
			<td>${polyprop.charge}</td>
		</tr>
		<tr>
			<td><b>Amino Acids</b></td>
			<td>${polyprop.aminoAcids}</td>
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
 <div id="one">
 <b class="white">
  <b class="white1"><b></b></b>
  <b class="white2"><b></b></b>
  <b class="white3"></b>
  <b class="white4"></b>
  <b class="white5"></b></b>
  <div style="background:#FFFFFF;height:160px;border: 1px solid black;">
  	<span><b>Protein Map</b></span><br></br>
  	<div align="center">	
   		<img src="<c:url value="/includes/images/protein.gif"/>" id="ProteinMap">
   	</div>
  </div>
  <b class="white">
  <b class="white5"></b>
  <b class="white4"></b>
  <b class="white3"></b>
  <b class="white2"><b></b></b>
  <b class="white1"><b></b></b></b>
 </div>