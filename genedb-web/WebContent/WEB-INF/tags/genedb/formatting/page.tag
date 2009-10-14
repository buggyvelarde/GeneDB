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
<a href="<c:url value="/"/>Homepage"><img src="<c:url value="/"/>includes/image/GeneDB-logo.png" border="0" height="52" width="290" alt="GeneDB" class="float-left-and-offset" /></a>

<div class="float-right" >
<div class="baby-blue-top"></div>
<div id="search" class="baby-blue">
<form action="<c:url value="/"/>QuickSearchQuery" method="get">
<table cellpadding="0" cellspacing="0" width="100%" class="search-table">
<tr>
<td>
<input type="hidden" name="pseudogenes" value="true" />
<input type="hidden" name="product" value="true" />
<input type="hidden" name="allNames" value="true" />
<input type="text" name="searchText" class="search-box" /></td><td align="right"><input type="image" src="<c:url value="/"/>includes/image/button-search.gif" /></td>
</tr>
<tr>
<td colspan="2">
<db:simpleselect/>
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
<li class="has-sub"><a href="/Homepage">Home</a>
<ul class="sub-menu">
<li><a href="">Weebles</a></li>
<li><a href="">wobble</a></li>
<li><a href="">but</a></li>
</ul>
</li>
<li><a href="">About us</a></li>
<li class="has-sub"><a href="">Searches</a>
<ul class="sub-menu">
<li><a href="<c:url value="/Query/geneType?taxons=${taxonNodeName}"/>" >By Gene Type</a></li>
<li><a href="<c:url value="/Query/geneLocation?taxons=${taxonNodeName}"/>" >By Location</a></li>
<li><a href="<c:url value="/Query/proteinLength?taxons=${taxonNodeName}"/>" >By Protein Length</a></li>
<li><a href="<c:url value="/Query/proteinMass?taxons=${taxonNodeName}"/>">By Molecular Mass</a></li>
<li><a href="<c:url value="/Query/proteinNumTM?taxons=${taxonNodeName}"/>">By No. TM domains</a></li>
<li><a href="<c:url value="/"/>Query/proteinTargetingSeq?taxons=${taxonNodeName}">By Targeting Seqs.</a></li>
<li><a href="<c:url value="/"/>Query/simpleName?taxons=${taxonNodeName}">Gene names</a></li>
<li><a href="<c:url value="/"/>Query/product?taxons=${taxonNodeName}">Product</a></li>
<!-- <li><a href="<c:url value="/"/>Query/curation?taxons=${taxonNodeName}">Curated annotations [comments & notes]</a></li> -->
<li><a href="<c:url value="/"/>Query/go?taxons=${taxonNodeName}">GO term/id</a></li>
<li><a href="<c:url value="/"/>Query/ec?taxons=${taxonNodeName}">EC number</a></li>
<li><a href="<c:url value="/"/>Query/pfam?taxons=${taxonNodeName}">Pfam ID or keyword</a></li>
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
<li><a href="<c:url value="/History"/>">History</a></li>
<li class="has-sub"><a href="">Temp. Testing</a>
<ul class="sub-menu">
<li><a href="<c:url value="/feature/Tb927.1.710"/>">Tb927.1.710</a></li>
<li><a href="<c:url value="/feature/Tb927.1.700"/>">Tb927.1.700</a></li>
<li><a href="<c:url value="/feature/Tb927.1.710"/>">Three</a></li>
</ul>
<!-- end sub menu -->
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
<p>Comments/Questions:
<a href="<c:url value="/Feedback" />">Curator/Data</a> or
<a href="<c:url value="/Feedback" />">website</a> | Version 4.98</p>
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

