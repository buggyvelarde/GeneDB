<%@ tag display-name="geninfo" 
        body-content="scriptless" dynamic-attributes="fMap" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>
<c:forEach var="f" begin="0" items="${fMap}">
	<c:set var="temp" value="${f.value}"/>
	<c:if test="${temp.cvTerm.name == 'gene'}">
		<c:set var="feature" value="${temp}" />	
	</c:if>
	<c:if test="${temp.cvTerm.name == 'polypeptide'}">
		<c:set var="polypeptide" value="${temp}" />	
	</c:if>
</c:forEach>
<div id="one">
	<b class="round">
  <b class="round1"><b></b></b>
  <b class="round2"><b></b></b>
  <b class="round3"></b>
  <b class="round4"></b>
  <b class="round5"></b></b>

  <div class="inner" style="height:160px;">
   <span><b>General Information</b></span><br><br>
   <table cellpadding="3px;" width="100%">
     <tr>
       <th>Gene Name</th>
       <td>${feature.displayName}</td>
       <db:synonym name="synonym" var="name"
				collection="${feature.featureSynonyms}">
					<th>Synonym</th>
					<td><db:list-string collection="${name}" /></td>
			</db:synonym>
     </tr>
     <tr>
       <th>Systematic Name</th>
       <td>${feature.uniqueName}</td>
       <db:synonym name="obsolete_name" var="name"
				collection="${feature.featureSynonyms}">
					<th>Previous IDs</th>
					<td><db:list-string collection="${name}" /></td>
		</db:synonym>
     </tr>
     <tr>
       <th>Protein names</th>
				<td><c:forEach items="${polypeptide.featureCvTerms}"
					var="featCvTerm">
					<c:if test="${featCvTerm.cvTerm.cv.name == 'genedb_products'}">
						<span>
						${featCvTerm.cvTerm.name}
						</span><br>
					</c:if>
				</c:forEach></td>
     </tr>
     <tr>
       <th>Location</th>
		<td><c:forEach items="${feature.featureLocsForFeatureId}"
			var="featLoc">
			<c:set var="start" value="${featLoc.fmin}" />
			<c:set var="end" value="${featLoc.fmax}" />
			<c:set var="chromosome"
				value="${featLoc.featureBySrcFeatureId.uniqueName}" />
			<span>${start}..${end}</span>
		</c:forEach></td>
		<th>Chromosome</th>
		<td><span>${chromosome}</span></td>		
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
<div id="two">
<b class="round">
  <b class="round1"><b></b></b>
  <b class="round2"><b></b></b>
  <b class="round3"></b>
  <b class="round4"></b>
  <b class="round5"></b></b>

  <div class="inner" style="height:160px;" align="center">
		<span>Send</span>
		<br>
		<form name="" action=""><select name="type">
			<option value="dna">Nucleotide</option>
			<option value="protein">Protein</option>
		</select> to <select name="analysis">
			<option value="blast">Blast</option>
			<option value="omni">omniBlast</option>
		</select></form>
		<br><br>
		<span>Download Region</span>
		<br>
		<form name="" action="">as <select name="type">
			<option value="fasta">FASTA</option>
			<option value="embl">EMBL</option>
		</select>
		</form><br>
		<a href="">GBrowse</a>&nbsp;&nbsp;&nbsp;<a href="">Synview</a>
  </div>
  <b class="round">
  <b class="round5"></b>
  <b class="round4"></b>
  <b class="round3"></b>
  <b class="round2"><b></b></b>
  <b class="round1"><b></b></b></b>
</div>