<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>

<c:url value="/" var="base"/>
<format:headerRound name="Gene: ${feature.displayName}" title="Gene Page ${feature.displayName}"
		onLoad="initContextMap('${base}', '${feature.uniqueName}')">
	<base href="<c:url value="/"/>">
	<st:init />
	<script type="text/javascript"
		src="<c:url value="/includes/scripts/extjs/ext-base.js"/>"></script>
	<script type="text/javascript"
		src="<c:url value="/includes/scripts/extjs/ext-all.js"/>"></script>
	<link rel="stylesheet" type="text/css"
		href="<c:url value="/includes/style/extjs/ext-all.css"/>" />
	<%-- The next two are used by the scrollable context map --%>
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/contextMap.css"/>" />
	<script type="text/javascript" src="<c:url value="/includes/scripts/genedb/contextMap.js"/>"></script>
</format:headerRound>
<table width="100%">
	<tr align="center">
		<td align="center">
			<div id="contextMapImage">
				<img src="<c:url value="/includes/images/default/grid/loading.gif"/>" id="contextMapLoadingImage">
			</div>
		</td>
	</tr>
	<tr>
		<td width="80%">
		<div class="fieldset" align="center" style="width: 98%;">
		<div class="legend">Gene Details</div>
		<br>
		<p>See <a
			href="http://www.genedb.org/genedb/Search?organism=All:*&name=${feature.uniqueName}">corresponding
		gene</a> in production GeneDB</p>
		<div style="float: left;">
		<div style="background-color: #9bd1fa;"><span>General
		Information</span></div>
		<table class="info" id="geninfo" cellspacing="0">
			<db:synonym name="primary_name" var="name"
				collection="${feature.featureSynonyms}">
				<tr>
					<td><b>Name</b></td>
					<td><db:list-string collection="${name}" /></td>
				</tr>
			</db:synonym>
			<db:synonym name="synonym" var="name"
				collection="${feature.featureSynonyms}">
				<tr>
					<td><b>Synonym</b></td>
					<td><db:list-string collection="${name}" /></td>
				</tr>
			</db:synonym>
			<db:synonym name="obsolete_name" var="name"
				collection="${feature.featureSynonyms}">
				<tr>
					<td><b>Obselete Name</b></td>
					<td><db:list-string collection="${name}" /></td>
				</tr>
			</db:synonym>
			<db:synonym name="reserved_name" var="name"
				collection="${feature.featureSynonyms}">
				<tr>
					<td><b>Reserved Name</b></td>
					<td><db:list-string collection="${name}" /></td>
				</tr>
			</db:synonym>
			<tr>
				<td><b>Type</b></td>
				<td><span><uc>${feature.cvTerm.name}</uc></span></td>
			</tr>
			<tr>
				<td><b>Product</b></td>
				<td><c:forEach items="${polypeptide.featureCvTerms}"
					var="featCvTerm">
					<c:if test="${featCvTerm.cvTerm.cv.name == 'genedb_products'}">
						<span>
						<dd>${featCvTerm.cvTerm.name}</dd>
						</span>
					</c:if>
				</c:forEach></td>
			</tr>
		</table>
		</div>
		<div style="float: right;">
		<div style="background-color: #9bd1fa;"><span>Genomic
		Locations</span></div>
		<table class="info" id="loc" cellspacing="0">
			<tr>
				<td><b>Sequence Location</b></td>
				<td><c:forEach items="${feature.featureLocsForFeatureId}"
					var="featLoc">
					<c:set var="start" value="${featLoc.fmin}" />
					<c:set var="end" value="${featLoc.fmax}" />
					<c:set var="chromosome"
						value="${featLoc.featureBySrcFeatureId.uniqueName}" />
					<span>${start}..${end}</span>
				</c:forEach></td>
			</tr>
			<tr>
				<td><b>Chromosome</b></td>
				<td><span>${chromosome}</span></td>
			</tr>
		</table>
		</div>
		<br>
		<table class="info" id="curation" cellspacing="0" style="clear: both;">
			<tr>
				<th class="bold" style="text-align: left;">Curation</th>
				<td></td>
			</tr>
			<tr>
				<th>Controlled Curation</th>
				<td><c:set var="url">
					<c:url value="/" />
				</c:set> <db:curation polypeptide="${polypeptide}" url="${url}"></db:curation>
				</td>
			</tr>
			<tr>
				<th>Comments</th>
				<td><db:comment polypeptide="${polypeptide}"></db:comment></td>
			</tr>
		</table>
		<table class="info" id="GO" cellspacing="0">
			<tr>
				<th class="bold" style="text-align: left;">GO annotations</th>
				<td>
				<table>
					<tr>
						<th class="c">Term</th>
						<th class="c">Qualifier</th>
						<th class="c">evidence</th>
						<th class="c">DbxRef</th>
						<th class="c">Other Genes</th>
					</tr>
				</table>
				</td>
			</tr>
			<tr>
				<th>Biological Process</th>
				<td>
				<table>
					<format:go-section title="Biological Process"
						cvName="biological_process" feature="${polypeptide}" />
				</table>
				</td>
			</tr>
			<tr>
				<th>Cellular Component</th>
				<td>
				<table>
					<format:go-section title="Cellular Component"
						cvName="cellular_component" feature="${polypeptide}" />
				</table>
				</td>
			</tr>
			<tr>
				<th>Molecular Function</th>
				<td>
				<table>
					<format:go-section title="Molecular Function"
						cvName="molecular_function" feature="${polypeptide}" />
				</table>
				</td>
			</tr>
		</table>
		<table class="info" id="peptide" cellspacing="0" style="clear: both;">
			<tr>
				<th class="bold" style="text-align: left;">Peptide Properties</th>
				<td></td>
			</tr>
			<tr>
				<th>Predicted Properties</th>
				<td></td>
			</tr>
			<tr>
				<th>Physical Properties</th>
				<td>
				<table class="simple">
					<tr>
						<td><b>Isoelectric Point</b></td>
						<td>pH ${polyprop.isoelectricPoint}</td>
						<td><b>Mass</b></td>
						<td>${polyprop.mass} kDa</td>
					</tr>
					<tr>
						<td><b>Charge</b></td>
						<td>${polyprop.charge}</td>
						<td><b>Amino Acids</b></td>
						<td>${polyprop.aminoAcids}</td>
					</tr>
				</table>
				</td>
			</tr>
		</table>
		<table class="info" id="phenotype" cellspacing="0">
			<tr>
				<th class="bold" style="text-align: left;">Phenotype Data</th>
			</tr>
		</table>
		<table class="info" id="exp" cellspacing="0">
			<tr>
				<th class="bold" style="text-align: left;">Expression Data</th>
			</tr>
		</table>
		<table class="info" id="pathway" cellspacing="0">
			<tr>
				<th class="bold" style="text-align: left;">Interactions /
				Pathways</th>
			</tr>
		</table>
		<table class="info" id="ortho" cellspacing="0">
			<tr>
				<th class="bold" style="text-align: left;">Orthologues</th>
				<td><db:ortholog polypeptide="${polypeptide}"></db:ortholog></td>
			</tr>
		</table>
		<table class="info" id="dbxref" cellspacing="0">
			<tr>
				<th class="bold" style="text-align: left;">Database
				cross-references</th>
				<td><c:if test="${!empty feature.dbXRef}">
					<c:set var="dbxref" value="${feature.dbXRef}" />
					<p><b>Xref:</b> ${dbXRef.db.name}:${dbXRef.accession} :
					${dbXRef.description}</p>
					<c:remove var="dbxref" />
				</c:if> <c:forEach items="${polypeptide.featureDbXRefs}" var="fdx">
					<br />
					<a href="${fdx.dbXRef.db.urlPrefix}${fdx.dbXRef.accession}">${fdx.dbXRef.db.name}:${fdx.dbXRef.accession}</a>&nbsp;&nbsp;&nbsp;&nbsp;${fdx.dbXRef.description}
		  			</c:forEach></td>
			</tr>
		</table>
		<table class="info" id="stock" cellspacing="0">
			<tr>
				<th class="bold" style="text-align: left;">Stocks / Reagents</th>
			</tr>
		</table>
		<a href="ArtemisLaunch?organism=${feature.organism.commonName}&chromosome=${chromosome}&start=${start}&end=${end}">Show region in Artemis</a></div>
		</td>
		<td width="20%">
		<div class="fieldset" align="center" style="width: 98%;">
		<div class="legend">Analysis Tools</div>
		<br>
		<div style="background-color: #9bd1fa;"><span>Send</span></div>
		<br>
		<form name="" action=""><select name="type">
			<option value="dna">Nucleotide</option>
			<option value="protein">Protein</option>
		</select> to <select name="analysis">
			<option value="blast">Blast</option>
			<option value="omni">omniBlast</option>
		</select></form>
		<br>
		<br>
		<div style="background-color: #9bd1fa;"><span>Download
		Region</span></div>
		<br>
		<form name="" action="">as <select name="type">
			<option value="fasta">FASTA</option>
			<option value="embl">EMBL</option>
		</select>
		</div>
		</td>
	</tr>
</table>
<format:footer />