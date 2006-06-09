<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<script src="/genedb-web/includes/scripts/script.aculo.us/prototype.js" type="text/javascript"></script>
<script src="/genedb-web/includes/scripts/script.aculo.us/scriptaculous.js" type="text/javascript"></script>
<script src="/genedb-web/includes/scripts/autocomplete.js" type="text/javascript"></script>
<script src="/genedb-web/includes/scripts/spring-util.js" type="text/javascript"></script>
<script src='/genedb-web/dwr/interface/PfamLookup.js' type="text/javascript"></script>
<script src='/genedb-web/dwr/engine.js' type="text/javascript"></script>
<script src='/genedb-web/dwr/util.js' type="text/javascript"></script>

<script type="text/javascript" language="javascript">
// <!CDATA[
  // the callback for the auto completer
  function populateAutocomplete(autocompleter, token) {
      PfamLookup.getPossibleMatches(token, function(suggestions) {
          autocompleter.setChoices(suggestions);
      });
  }

  // should be in the "onload" of the body
  function createAutoCompleter() {
      new Autocompleter.DWR("pfamInput", "pfamSuggestions", populateAutocomplete, {});
  }
// ]]>
</script>



<body onload="DWRUtil.useLoadingMessage(); createAutoCompleter()">
<format:header>Welcome to the GeneDB website<br />Version 4.0</format:header>

<h3>Pfam parameter test</h3>

<p><b>Where should body be handled?</b></p>
<p>Imagine you've just chosen a "Select genes with a specific pfam domain"</p>

<form>

<input id="pfamInput" type="text" /><div style="background-color: #2C5F93;" id="pfamSuggestions"></div>

</form>

<format:footer />
