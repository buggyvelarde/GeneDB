<%@ tag display-name="page" body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="organism"%>
<%@ attribute name="onLoad" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>

<% if (onLoad == null) { %>
<body>
<% } else { %>
<body onLoad="${onLoad}">
<% } %>

<div id="container">
<div id="header">
<a href="/Homepage"><img src="<c:url value="/"/>includes/image/GeneDB-logo.png" border="0" height="52" width="290" alt="GeneDB" class="float-left" /></a>

<div class="float-right" >
<div class="baby-blue-top"></div>
<div id="search" class="baby-blue">
<form action="" method="post">
<table cellpadding="0" cellspacing="0" width="100%" class="search-table">
<tr>
<td><input type="text" name="search" class="search-box" /></td><td align="right"><input type="image" src="<c:url value="/"/>includes/image/button-search.gif" /></td>
</tr>
<tr>
<td colspan="2">
<select>
<option>All organisms</option>
</select>
</td>
</tr>
</table>
</form>
</div>
<div class="baby-blue-bot"></div>
</div><!-- end float right of search box -->
<br class="clear" />
</div><!-- end header block -->

<br class="clear" />

<div id="navigation">
<ul id="nav">
<li><a href="/Homepage">Home</a></li>
<li class="has-sub"><a href="">About us</a>
<ul class="sub-menu">
<li><a href="">One</a></li>
<li><a href="">A really long title just for testing</a></li>
<li><a href="">Three</a></li>
</ul>
<!-- end sub menu -->
</li>
<li class="has-sub"><a href="">Searches</a>
<ul class="sub-menu">
<li><a href="<c:url value="/Query/geneType&taxons=${taxonNodeName}&newSearch=true"/>" >By Gene Type</a></li>
<li><a href="<c:url value="/Query/geneLocation&taxons=${taxonNodeName}&newSearch=true"/>" >By Location</a></li>
<li><a href="<c:url value="/Query/proteinLength&taxons=${taxonNodeName}&newSearch=true"/>" >By Protein Length</a></li>
<li><a href="<c:url value="/Query/proteinMass&taxons=${taxonNodeName}&newSearch=true"/>">By Molecular Mass</a></li>
<li><a href="<c:url value="/Query/proteinNumTM&taxons=${taxonNodeName}&newSearch=true"/>">By No. TM domains</a></li>
<li><a href="<c:url value="/"/>Query/proteinTargetingSeq&taxons=${taxonNodeName}&newSearch=true">By Targeting Seqs.</a></li>
<li><a href="<c:url value="/"/>Query/simpleName&taxons=${taxonNodeName}&newSearch=true">Gene names</a></li>
<li><a href="<c:url value="/"/>Query/product&taxons=${taxonNodeName}&newSearch=true">Product</a></li>
<li><a href="<c:url value="/"/>Query/curation&taxons=${taxonNodeName}&newSearch=true">Curated annotations [comments & notes]</a></li>
<li><a href="<c:url value="/"/>Query/go&taxons=${taxonNodeName}&newSearch=true">GO term/id</a></li>
<li><a href="<c:url value="/"/>Query/ec&taxons=${taxonNodeName}&newSearch=true">EC number</a></li>
<li><a href="<c:url value="/"/>Query/pfam&taxons=${taxonNodeName}&newSearch=true">Pfam ID or keyword</a></li>
</ul>
<!-- end sub menu -->
</li>
<li class="has-sub"><a href="">Browse</a>
<ul class="sub-menu">
<li><a href="<c:url value="/"/>category/genedb_products?taxons=${taxonNodeName}">Products</a></li>
<li><a href="<c:url value="/"/>category/ControlledCuration?taxons=${taxonNodeName}">Controlled Curation</a></li>
<li><a href="<c:url value="/"/>category/biological_process?taxons=${taxonNodeName}">Biological Process</a></li>
<li><a href="<c:url value="/"/>category/cellular_component?taxons=${taxonNodeName}">Cellular Component</a></li>
<li><a href="<c:url value="/"/>category/molecular_function?taxons=${taxonNodeName}">Molecular Function</a></li>
</ul>
</li>
<li class="has-sub">Pages
<ul class="sub-menu">
<li><a href="index.html">Home</a></li>
<li><a href="search.html">Search results</a></li>
<li><a href="entrypoint.html">Database entry point</a></li>
<li><a href="infantum.html">L. infantum JPCM5</a></li>
</ul><!-- end sub menu -->
</li>
</ul>
</div><!-- end navigation block -->


<jsp:doBody />

<br class="clear" />

<div id="footer">
<div class="footer-top"></div>
<div class="light-grey">
<table cellpadding="0" cellspacing="0" width="100%">
<tr>
<td valign="top" align="left">
<p>&copy; 2009 and hosted by the <a href="http://www.sanger.ac.uk/">Sanger Institute</a></p>
</td>

<td valign="top" align="right">
<p><a href="">Send us your comments on GeneDB</a> | Version 4.88</p>
</td>
</tr>
</table>
</div>
<div class="footer-bot"></div>
</div><!-- end footer -->
</div><!-- end container -->

<script type="text/javascript">
_userv=0;
urchinTracker();
</script>
</body>
</html>

