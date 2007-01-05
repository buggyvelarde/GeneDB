<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<script src="<c:url value="/includes/scripts/autocomplete.js"/>" type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/spring-util.js"/>" type="text/javascript"></script>
<script src='<c:url value="/dwr/interface/goProcessBrowse.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/engine.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/util.js"/>' type="text/javascript"></script>

<link rel="stylesheet" type="text/css" href="<c:url value="/includes/scripts/browser/browser_stylesheet.css"/>" />
<script type="text/javascript" src="<c:url value="/includes/scripts/script.aculo.us/prototype.js"/>" />    
<script type="text/javascript" src="<c:url value="/includes/scripts/browser/Other.js"/>" />
<script type="text/javascript" src="<c:url value="/includes/scripts/browser/View.js"/>" />
<script type="text/javascript" src="<c:url value="/includes/scripts/browser/TracksAndZooms.js"/>" />
<script type="text/javascript" src="<c:url value="/includes/scripts/browser/ComponentInterface.js"/>" />
<script type="text/javascript" src="<c:url value="/includes/scripts/browser/ViewerComponent.js"/>" />
<script type="text/javascript" src="<c:url value="/includes/scripts/browser/NavigationComponent.js"/>" />
<script type="text/javascript" src="<c:url value="/includes/scripts/browser/TrackControlComponent.js"/>" />
<script type="text/javascript" src="<c:url value="/includes/scripts/browser/DebugComponent.js"/>" />
<script type="text/javascript" src="<c:url value="/includes/scripts/browser/MenuComponent.js"/>" />
<script type="text/javascript" src="<c:url value="/includes/scripts/browser/Load.js"/>" />


<script type="text/javascript" language="javascript">
// <!CDATA[
  // the callback for the auto completer
  function populateAutocomplete(autocompleter, token) {
      goProcessBrowse.getPossibleMatches(token, function(suggestions) {
          autocompleter.setChoices(suggestions);
      });
  }

  // should be in the "onload" of the body
  function createAutoCompleter() {
      new Autocompleter.DWR("textInput", "suggestions", populateAutocomplete, {});
  }
// ]]>
</script>


<body onload="loadBrowser()" id="browserBody">
<!-- <body onload="DWRUtil.useLoadingMessage(); createAutoCompleter()">-->
<format:header name="New GUI Test" />
    
    <div>
      <text class="header2" id="landmarkname" />
      <p />
    </div>
    
    <div id="NavigationComponentPlaceholder">
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
	  <td width="40%">
	    <b>Region or Nucleotide:</b>
	    <br />
	  <input id="searchInputBox" type="text" size="25"></input>
	  <button id="searchButton">Search</button>
	  </td>
	  <td>
	    <b>Scroll and Zoom:</b><br />
	    <button id="goToStartButton">Start</button>
	    <button id="scrollFarLeftButton">&lt;&lt;</button>
	    <button id="scrollNearLeftButton">&lt;</button>
	    <button id="zoomOutButton">-</button>
	    <select id="zoomLevelsMenu"></select>
	    <button id="zoomInButton">+</button>
	    <button id="scrollNearRightButton">&gt;</button>
	    <button id="scrollFarRightButton">&gt;&gt;</button>
	    <button id="goToEndButton">End</button>
	  </td>
	</tr>
      </table>
    </div>
    
    <!-- for some reason, <div /> doesn't work... but it ought to, right? -->
    <div id="ViewerComponentPlaceholder"></div>
    
    <div id="TrackControlComponentPlaceholder">
      <button id="goToClassic">View this in the original GBrowse</button>
      <p><b id="trackControls">Toggle Tracks On/Off:<br /></b></p>
   
      <p><b>Track Label Controls:</b>
	<br />
	<button id="trackLabelToggle">Toggle track labels on/off</button>
	<button id="raiseTransp">Make labels MORE transparent</button>
	<button id="lowerTransp">Make labels LESS transparent</button>
      </p>

      <p><b>Dimension Controls:</b>
	<br />
	<button id="maxHeightButton">Set view height to exactly fit all tracks</button>
	<button id="setHeightButton">Set view height to:</button>
	<textarea id="viewHeight" cols="5" rows="1"></textarea>
	pixels
      </p>
    </div>
    
    <div>
      <b>Message Console</b><br />
      <textarea id="debugMessage" cols="120" rows="10" readonly="readonly" wrap="virtual"></textarea>
    </div>      
    
    <!-- FOR DEBUGGING ONLY - *REMOVE* before public release !!! -->
    <div id="DebugComponentPlaceholder">
      <b>Debugging Tools/Options</b><br />
      
      Nudge left/right by one pixel:
      <button onclick="nudgeLeft(); return false;">Left</button>
      <button onclick="nudgeRight(); return false;">Right</button>
      
      Stupid testing box:
      <textarea id="stupidTestBox" cols="30" rows="1"></textarea>
      <button onclick="stupidTestBoxHandler(); return false;">Gwan Test?</button>
    </div>
    
<format:footer />
