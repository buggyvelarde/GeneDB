<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<script type="text/javascript" src="<c:url value="/includes/yui/build/yahoo/yahoo-min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/includes/yui/build/dom/dom-min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/includes/yui/build/event/event-min.js"/>"></script>

<!-- OPTIONAL: Animation (required only if enabling animation) -->
<script type="text/javascript" src="<c:url value="/includes/yui/build/animation/animation-min.js"/>"></script>

<script src="<c:url value="/includes/scripts/spring-util.js"/>" type="text/javascript"></script>
<script src='<c:url value="/dwr/interface/goProcessBrowse.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/engine.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/util.js"/>' type="text/javascript"></script>

<script type="text/javascript" language="javascript">
// <!CDATA[
  // the callback for the auto completer
  function populateAutocomplete(autocompleter, token) {
      goProcessBrowse.getPossibleMatches(token, function(suggestions) {
          autocompleter.setChoices(suggestions);
      });
  }

// ]]>
</script>



<body onload="DWRUtil.useLoadingMessage()">
<format:header name="DWR/YUI autocomplete test" />

<p>Imagine you've just chosen a "Select genes with a specific GO process"</p>

<form>

<input id="textInput" type="text" size="50"/><div id="suggestions"></div>
<script type="text/javascript">
// A JavaScript Function DataSource
var myFunction = function() {
 var myArray2 = ["d", "e", "f"];
 return myArray2.reverse();
}
var myDataSource = new YAHOO.widget.DS_JSFunction(myFunction);
var myAutoComp = new YAHOO.widget.AutoComplete("textInput","suggestions", myDataSource);
</script>

</form>

<format:footer />
