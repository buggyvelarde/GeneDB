<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>

<format:header title="Generic Feature Page">
	<st:init />
	<link rel="stylesheet"
		href='<misc:url value="/"/>includes/style/alternative.css'
		type="text/css" />
	<link rel="stylesheet" href='<misc:url value="/"/>includes/style/wtsi.css'
		type="text/css" />
	<link rel="stylesheet"
		href='<misc:url value="/"/>includes/style/frontpage1.css' type="text/css" />
	<script type="text/javascript"
		src='<misc:url value="/includes/scripts/extjs/ext-base.js"/>'></script>
	<script type="text/javascript"
		src='<misc:url value="/includes/scripts/extjs/ext-all.js"/>'></script>
	<link rel="stylesheet" type="text/css"
		href='<misc:url value="/includes/style/extjs/ext-all.css"/>' />
	<script type="text/javascript"
		src='<misc:url value="/includes/scripts/extjs/ext-history.js"/>'></script>
</format:header>
<table width="100%">
	<tr>
		<td width="20%">
			<div class="fieldset">
				<div class="legend">Quick Search</div>
				<br>
				<form name="query" action="NameSearch" method="get">
				<table>
					<tr>
						<td>Gene Name:</td>
						<td><input id="query" name="name" type="text" size="12" /></td>
					</tr>
					<tr>
						<td><input type="submit" value="submit" /></td>
						<td><br>
						</td>
					</tr>
				</table>
				</form>
				</div>
				<div class="fieldset">
				<div class="legend">Navigation</div>
				<br>
				<table width="100%" border="0" cellpadding="0" cellspacing="0">
					<tr align="left">
						<td><a href="www.genedb.org" class="mainlevel" id="active_menu">Home</a></td>
					</tr>
					<tr align="left">
						<td><a href="www.genedb.org" class="mainlevel">News</a></td>
					</tr>
					<tr align="left">
						<td><a href="www.genedb.org" class="mainlevel">Links</a></td>
					</tr>
					<tr align="left">
						<td><a href="www.genedb.org" class="mainlevel">Search</a></td>
					</tr>
					<tr align="left">
						<td><a href="www.genedb.org" class="mainlevel">FAQs</a></td>
					</tr>
				</table>
			</div>
		</td>
		<td width="80%">
			<div class="fieldset" align="center" style="width: 98%;">
				<div class="legend">Generic Feature</div>
				<c:forEach items="${feature.featureSynonyms}" var="featSyn">
					<p><b>${featSyn.synonym.type.name}</b> ${featSyn.synonym.name}
					<c:if test="!${featSyn.current}">{Obsolete}</c:if></p>
				</c:forEach>

				<dl>
					<dt><b>Name:</b></dt>
					<dd>${feature.name}</dd>

					<dt><b>Unique name:</b></dt>
					<dd>${feature.uniqueName}</dd>

					<dt><b>Type:</b></dt>
					<dd>${feature.type.name}</dd>

					<dt><b>Analysis Feature:</b></dt>
					<dd>${feature.analysis}</dd>

					<dt><b>Obsolete?:</b></dt>
					<dd>${feature.obsolete}</dd>

					<dt><b>Date created:</b></dt>
					<dd>${feature.timeAccessioned}</dd>

					<dt><b>Date last modified:</b></dt>
					<dd>${feature.timeLastModified}</dd>

					<dt><b>Organism:</b></dt>
					<dd>${feature.organism.genus} ${feature.organism.species}</dd>
				</dl>

				<h3>Feature Properties</h3>

				<table>
					<c:forEach items="${feature.featureProps}" var="featProp">
						<tr>
							<td>[${featProp.rank}]</td>
							<td>&nbsp;&nbsp;&nbsp;&nbsp;${featProp.type.name}</td>
							<td>&nbsp;&nbsp;&nbsp;&nbsp;${featProp.value}</td>
						</tr>
					</c:forEach>
				</table>

				<h3>Feature Relationships</h3>
				<h5>This feature is subject</h5>

				<table>
					<c:forEach items="${feature.featureRelationshipsForSubjectId}"
						var="featRel">
						<tr>
							<td>${featRel.rank}</td>
							<td>${featRel.value}</td>
							<td>this</td>
							<td>is ${featRel.type.name}</td>
							<td><a
								href="./FeatureByName?name=${featRel.objectFeature.uniqueName}">${featRel.objectFeature.uniqueName}</a>
							[${featRel.objectFeature.type.name}]</td>
						</tr>

					</c:forEach>
				</table>

				<h5>This feature is object</h5>

				<table>
					<c:forEach items="${feature.featureRelationshipsForObjectId}"
						var="featRel">
						<tr>
							<td>${featRel.rank}</td>
							<td>${featRel.value}</td>
							<td><a
								href="./FeatureByName?name=${featRel.subjectFeature.uniqueName}">${featRel.subjectFeature.uniqueName}</a>
							[${featRel.subjectFeature.type.name}]</td>
							<td>is ${featRel.type.name}</td>
							<td>this</td>
						</tr>
					</c:forEach>
				</table>

				<h3>Database X-refs</h3>


				<c:if test="${!empty feature.dbXRef}">
					<c:set var="dbxref" value="${feature.dbXRef}" />
					<p><b>Xref:</b> ${dbxref.db.name}:${dbxRef.accession} :
					${dbXRef.description}</p>
					<c:remove var="dbxref" />
				</c:if>
				<p>---</p>
				<c:forEach items="${feature.featureDbXRefs}" var="fdx">
					<p>[${fdx.current}]&nbsp;&nbsp;&nbsp;&nbsp;${fdx.dbXRef.db.name}:${fdx.dbXRef.accession}&nbsp;&nbsp;&nbsp;&nbsp;${fdx.dbxref.description}</p>
				</c:forEach>

				<h3>Feature Locations</h3>
			</div>
		</td>
	</tr>
</table>
<format:footer />