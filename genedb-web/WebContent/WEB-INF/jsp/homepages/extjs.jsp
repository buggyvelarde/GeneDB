<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
<head>
  <title>GeneDB</title>
	<link rel="stylesheet" type="text/css" href="<c:url value="/"/>includes/style/extjs/ext-all.css" />
 	<script type="text/javascript" src="<c:url value="/"/>includes/scripts/extjs/ext-base.js"></script>
    <script type="text/javascript" src="<c:url value="/"/>includes/scripts/extjs/ext-all.js"></script>
    <script type="text/javascript" src="<c:url value="/"/>includes/scripts/phylogeny.js"></script>
    <script type="text/javascript" src="<c:url value="/"/>includes/scripts/frontpage.js"></script>
	<style type="text/css">
	html, body {
        font:normal 12px verdana;
        margin:0;
        padding:0;
        border:0 none;
        overflow:hidden;
        height:100%;
    }
	p {
	    margin:5px;
	}
    .settings {
        background-image:url(../shared/icons/fam/folder_wrench.png);
    }
    .nav {
        background-image:url(../shared/icons/fam/folder_go.png);
    }
    </style>
</head>
<body onload="doSomething()">
  <div id="west">
    <p>Hi. I'm the west panel.</p>
  </div>
  <div id="north">
    <format:header name="Welcome to Genedb 3.0"/>
  </div>
  <div id="center2" >
  	<div id="gene-page" style="visibility: hidden; z-index: 1;" >
	</div>
	<div id="query-form">
  <table style="font:normal 12px verdana;">
    <tr>
      <td>Organisms:</td>
      <td><div id="container"><db:phylogeny/></div></td>
    </tr>
    <tr>
      <td></td>
      <td><input id="selected" readonly="readonly"/></td>
    </tr>
    <tr>
	  <td>Look Up:</td>
	  <td><input id="query" type="text"/><div style="background-color: #2C5F93;" id="suggestions"></div></td>
	  <td>The name to lookup. It can include wildcards (*) to match any series of characters</td>
    </tr>
    <tr>
	  <td>Feature Type:</td>
	  <td>Gene</td>
	  <td>Restrict the type of features searched for</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
	  <td colspan="2"><input type="submit" value="Submit" onclick="SubmitClicked()"/></td>
	  <td>&nbsp;</td>
    </tr>
  </table>
  </div>
	<div id="results-grid" style="visibility: hidden"></div>
  </div>
  <div id="south">
    <format:footer />
  </div>

 </body>
</html>
