<%@ tag display-name="header1"
        body-content="scriptless" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="title" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/frontpage.css" type="text/css"/>
	<c:set var="pageTitle" value="${name} - GeneDB"/>
	<c:if test="!empty title">
		<c:set var="pageTitle" value="${title}" />
	</c:if>
	<title>${pageTitle}</title>
    <jsp:doBody />
    <script src="<c:url value="/includes/scripts/script.aculo.us/prototype.js"/>" type="text/javascript"></script>
	<script src="<c:url value="/includes/scripts/script.aculo.us/scriptaculous.js"/>" type="text/javascript"></script>
    <script src='<c:url value="/dwr/interface/nameBrowse.js"/>' type="text/javascript"></script>
    <script src="<c:url value="/includes/scripts/spring-util.js"/>" type="text/javascript"></script>
	<script src='<c:url value="/dwr/engine.js"/>' type="text/javascript"></script>
	<script src='<c:url value="/dwr/util.js"/>' type="text/javascript"></script>
	<script src="<c:url value="/includes/scripts/autocomplete.js"/>" type="text/javascript"></script>
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
</head>    
<body onload="DWRUtil.useLoadingMessage(); createAutoCompleter()">
<div id="masthead">
~~ GeneDB ~~
<h3>The database of Pathogens</h3>
</div>
<div id="nav">
	<div id="goggles">
		<form name="query" action="<c:url value="/"/>NamedFeature" method="get" style="vertical-align: middle;">
			<input id="textInput" name="name" type="text" style="width: 219px; border:0; padding: 4px; vertical-align: middle;" align="middle"/>
			<div id="suggestions"></div>
			<input type="submit" value="Search" title="Search" align="middle" style="vertical-align: middle; margin:0 0 0 7px;background:transparent url(<c:url value="/"/>includes/images/bgsprite.gif) no-repeat -47px top;border:0;width:66px;height:24px;font-weight:normal;vertical-align:bottom;cursor:pointer;font-size:1em;font-weight:bold;"/> 
		</form>
	</div>
	<div id="menu">
		<ul>
			<li ><a href="/about/" title="">About</a></li>
			<li ><a href="//" title="">Advance Search</a></li>
			<li ><a href="//" title="">News</a></li>
			<li class="last"><a href="/contact/" title="">Contact</a></li>
		</ul>
	</div><!--END menu-->
</div><!--END nav-->