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
  <table style="font:normal 12px verdana;">
    <tr>
      <td>Organisms:</td>
      <td><div id="container"><db:phylogeny/></div></td>
      <td>You can choose either an individual organism or a group of them. (Note this is a temporary select box)</td>
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
	<div id="results-grid" style="visibility: hidden"></div>
	<div id="temp">
	<p>
    This is a very simple example of using XML for load and submit of data with an Ext dynamic form.
</p>
<p>
    Click "Load" to load the <a href="xml-form.xml">dummy XML data</a> from the server using an XmlReader.
</p>
<p>
    After loading the form, you will be able to hit submit. The submit action will make a post to the server,
    and the <a href="xml-errors.xml">dummy XML file</a> on the server with test server-side validation failure messages will be sent back.
    Those messages will be applied to the appropriate fields in the form.

</p>
<p>
    Note: The built-in JSON support does not require any special readers for mapping. However, If you don't like the Form's built-in JSON format, you could also use a JsonReader for reading data into a form.
</p>
	</div>
  </div>
  <div id="props-panel" style="width:200px;height:200px;overflow:hidden;">
  </div>
  <div id="south">
    <format:footer />
  </div>

 </body>
</html>
