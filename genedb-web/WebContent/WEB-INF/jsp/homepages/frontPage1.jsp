<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<script type="text/javascript" language="javascript">
	var ilength;
			
	function populateAutocomplete(autocompleter, token) {
      nameBrowse.getPossibleMatches(token, function(suggestions) {
          autocompleter.setChoices(suggestions);
      });
  }

  // should be in the "onload" of the body
  function createAutoCompleter() {
      new Autocompleter.DWR("textInput", "suggestions", populateAutocomplete, {});
  }
</script>
<body onload="DWRUtil.useLoadingMessage(); createAutoCompleter()">
<format:header1 name="Welcome to the GeneDB website Next Generation">
<st:init />
<link rel="stylesheet" href="<c:url value="/"/>includes/style/wtsi.css" type="text/css"/>
<link rel="stylesheet" href="<c:url value="/"/>includes/style/jimmac.css" type="text/css"/>
<link rel="stylesheet" href="<c:url value="/"/>includes/style/frontpage1.css" type="text/css"/>
<script src='<c:url value="/dwr/interface/nameBrowse.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/engine.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/util.js"/>' type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/autocomplete.js"/>" type="text/javascript"></script>
</format:header1>
<table width="100%">
<tr>
	<td width="20%">
		<div class="fieldset">
		<div class="legend">Quick Search</div>
		<div class="content">
			<form name="query" action="NamedFeature" method="get">
			<table>
				<tr>
					<td>Gene Name: </td>
					<td><input id="query" id="textInput" name="name" type="text" size="12"/><div style="background-color: #2C5F93;" id="suggestions"></div></td>
				</tr>
				<tr>
					<td><input type="submit" value="submit"/></td>
					<td><br></td>
				</tr>
			</table>
			</form>
		</div>
		</div>
		<div class="fieldset">
		<div class="legend">Navigation</div>
			<br>
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" id="active_menu">Home</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel">News</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >Links</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >Search</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >FAQs</a></td></tr>
			</table>
		</div>
		<div class="fieldset">
		<div class="legend">Search</div>
			<br>
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
				<tr align="left"><td><a href="./Curation" class="mainlevel" id="active_menu">Proteins with Curation</a></td></tr>
				<tr align="left"><td><a href="./SearchAll" class="mainlevel">Genes with specific GO</a></td></tr>
				<tr align="left"><td><a href="./SearchAll" class="mainlevel" >Proteins with Product</a></td></tr>
			</table>
		</div>
	</td>
	<td width="60%" align="center">
		<div class="fieldset" align="center">
		<div class="legend">Information</div>
		<div class="content">
		<div class="fieldset">
		<div class="legend">Pathogen Sequencing - Projects</div>
		<div class="content">
		<table width="100%" align="center">
		<tbody>
		<tr>
		<td width="50%">
		Bacteria:
		<img class="img-nb" src="<c:url value="/includes/images/nm.gif"/>" alt="Bacteria" title="Bacteria" style="height: 55px; width: 55px; float: right;">
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		<td width="50%">
		<a href="./OrganismChooser?organism=Protozoa">Protozoa</a>:
		<a href="./OrganismChooser?organism=Protozoa"><img class="img-nb" src="<c:url value="/includes/images/bg.jpg"/>" alt="Protozoa" title="Protozoa" style="height: 55px; width: 55px; float: right;"></a>
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		</tr>
		<tr>
		<td width="50%">
		Fungi:
		<img class="img-nb" src="<c:url value="/includes/images/fg.jpg"/>" alt="Fungi" title="Fungi" style="height: 55px; width: 55px; float: right;">
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		<td width="50%">
		Helminths:
		<img class="img-nb" src="<c:url value="/includes/images/he.jpg"/>" alt="Helminths" title="Helminths" style="height: 55px; width: 55px; float: right;">
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		</tr>
		<tr>
		<td width="50%">
		Plasmids:
		<img class="img-nb" src="<c:url value="/includes/images/pl.jpg"/>" alt="Plasmids" title="Plasmids" style="height: 55px; width: 55px; float: right;">
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		<td width="50%"><!--run from /bin-offline/Website/lists/-->
		Vectors:
		<img class="img-nb" src="<c:url value="/includes/images/ve.gif"/>" alt="Vectors" title="Vectors" style="height: 55px; width: 55px; float: right;">
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		</tr>
		<tr>
		<td width="50%">
		Bacteriophage:
		<img class="img-nb" src="<c:url value="/includes/images/ba.jpg"/>" alt="Bacteriophage" title="Bacteriophage" style="height: 55px; width: 55px; float: right;">
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		<td width="50%">Virus:
		<img class="img-nb" src="<c:url value="/includes/images/vi.jpg"/>" alt="Virus" title="Virus" style="height: 55px; width: 55px; float: right;">
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		</tr>
		</tbody>
		</table>
		</div>
		</div>
		<table width="100%" align="center" >
			<tr>
				<td>The GeneDB project is a core part of the Sanger Institute Pathogen
		Sequencing Unit's (PSU) activities. Its primary goals are:<ul><li>to provide reliable storage of, and access to the latest sequence data and
		annotation/curation for the whole range of organisms sequenced by the PSU.</li>
		 <li>to develop the website and other tools to aid the community in accessing
		and obtaining the maximum value from this data.</li>
		</ul></td>
			</tr>
		</table>
		</div>
		</div>
	</td>
	<!--   <td width="20%">
		<div class="fieldset">
		<div class="legend">News</div>
		<div class="content">
		<table>
			<tr>
				<td>
					<div align="center"> 
						<font color="#660000" size="+1"> <a href="www.google.com"> News<br> News News<br> News News<br> </a></font><br><br>
						<font color="#660000" size="+1"> <a href="www.sanger.ac.uk"> News1<br> News1 News1<br> News1 News1<br> </a></font> 
					</div>
				</td>
			</tr>
		</table>
		</div>
		</div>
	</td> -->
	</tr>
	</table>
<format:footer />