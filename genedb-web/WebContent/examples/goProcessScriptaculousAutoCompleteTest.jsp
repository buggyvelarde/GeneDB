<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<script src="<c:url value="/includes/scripts/script.aculo.us/prototype.js"/>" type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/script.aculo.us/scriptaculous.js"/>" type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/autocomplete.js"/>" type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/spring-util.js"/>" type="text/javascript"></script>
<script src='<c:url value="/dwr/interface/goPBrowse.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/engine.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/util.js"/>' type="text/javascript"></script>

<script type="text/javascript" language="javascript">
// <!CDATA[
  // the callback for the auto completer
  function populateAutocomplete(autocompleter, token) {
      goPBrowse.getPossibleMatches(token, function(suggestions) {
          autocompleter.setChoices(suggestions);
      });
  }

  // should be in the "onload" of the body
  function createAutoCompleter() {
      new Autocompleter.DWR("textInput", "suggestions", populateAutocomplete, {});
  }
// ]]>
</script>



<body onload="DWRUtil.useLoadingMessage(); createAutoCompleter()">
<format:header name="DWR/Scriptaculous autocomplete test" />

<style type="text/css">
    div.auto_complete {
      position:absolute;
      width:250px;
      background-color:white;
      border:1px solid #888;
      margin:0px;
      padding:0px;
    }
    li.selected { background-color: #ffb; }
  </style>

<p>Imagine you've just chosen a "Select genes with a specific GO process"</p>

<form>

<input id="textInput" type="text" size="50"/><div style="background-color: #2C5F93;" id="suggestions"></div>

</form>

<format:footer />
