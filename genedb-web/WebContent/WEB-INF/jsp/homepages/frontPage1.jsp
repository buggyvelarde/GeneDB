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
<format:headerRound title="Welcome to the GeneDB website Next Generation">
<st:init />
<script src='<c:url value="/dwr/interface/nameBrowse.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/engine.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/util.js"/>' type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/autocomplete.js"/>" type="text/javascript"></script>
<style>
</style>
</format:headerRound>
<table width="100%" style="clear: both;">
<tr>
	<td width="20%">
		<format:searchOptions/>
		<format:news/>
	</td>
	<td width="80%" align="center">
		<div class="fieldset" align="center">
		<div class="legend">Organisms</div>
		<div class="content">
		<table width="100%" align="center">
		<tbody>
		<tr>
		<td width="50%" align="center">
		Bacteria:
		<img class="img-nb" src="<c:url value="/includes/images/nm.gif"/>" alt="Bacteria" title="Bacteria" style="height: 55px; width: 55px; float: right;">
		<p></p>
		</td>
		<td width="50%" align="center">
		<a href="./OrganismChooser?organism=Protozoa">Protozoa:</a>
		<a href="./OrganismChooser?organism=Protozoa"><img class="img-nb" src="<c:url value="/includes/images/bg.jpg"/>" alt="Protozoa" title="Protozoa" style="height: 55px; width: 55px; float: left;"></a>
		<p><a href="#"> Kinetoplastid</a> ; <a href="#"> Apicomplexa</a></p>
		</td>
		</tr>
		<tr>
		<td width="50%" align="center">
		Fungi:
		<img class="img-nb" src="<c:url value="/includes/images/fg.jpg"/>" alt="Fungi" title="Fungi" style="height: 55px; width: 55px; float: right;">
		<p></p>
		</td>
		<td width="50%" align="center">
		Helminths:
		<img class="img-nb" src="<c:url value="/includes/images/he.jpg"/>" alt="Helminths" title="Helminths" style="height: 55px; width: 55px; float: left;">
		<p></p>
		</td>
		</tr>
		<tr>
		<td width="50%" align="center">
		Plasmids:
		<img class="img-nb" src="<c:url value="/includes/images/pl.jpg"/>" alt="Plasmids" title="Plasmids" style="height: 55px; width: 55px; float: right;">
		<p></p>
		</td>
		<td width="50%" align="center"><!--run from /bin-offline/Website/lists/-->
		Vectors:
		<img class="img-nb" src="<c:url value="/includes/images/ve.gif"/>" alt="Vectors" title="Vectors" style="height: 55px; width: 55px; float: left;">
		<p></p>
		</td>
		</tr>
		<tr>
		<td width="50%" align="center">
		Bacteriophage:
		<img class="img-nb" src="<c:url value="/includes/images/ba.jpg"/>" alt="Bacteriophage" title="Bacteriophage" style="height: 55px; width: 55px; float: right;">
		<p></p>
		</td>
		<td width="50%" align="center">Virus:
		<img class="img-nb" src="<c:url value="/includes/images/vi.jpg"/>" alt="Virus" title="Virus" style="height: 55px; width: 55px; float: left;">
		<p></p>
		</td>
		</tr>
		</tbody>
		</table>
		</div>
		</div>
	</td>
	</tr>
	</table>
    <br>
<format:footer />