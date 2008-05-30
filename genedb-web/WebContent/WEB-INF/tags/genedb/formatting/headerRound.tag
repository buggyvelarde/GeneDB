<%@ tag display-name="header1"
        body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="title"  required="true" %>
<%@ attribute name="onLoad" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css" />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/jimmac.css" type="text/css" />
	<c:set var="pageTitle" value="${title} - GeneDB"/>
	<c:if test="!empty title">
		<c:set var="pageTitle" value="${title}" />
	</c:if>
	<title>${pageTitle}</title>
    <jsp:doBody />
</head>
<% if (onLoad == null) { %>
<body>
<% } else { %>
<body onLoad="${onLoad}">
<% } %>
<!--  <div class="rounded" style="width: 100%; background-color: rgb(55, 124, 177); text-align: center; font-size: 2em; line-height: 3em; font-size-adjust: none; font-stretch: normal; color: rgb(0, 0, 0); font-family: Georgia; font-variant: small-caps; font-weight: bold;"> -->
	<table width="100%" style="background-color: rgb(55, 124, 177); height: 30px;">
		<tr>
			<td width="30%" align="left" style="background-color: rgb(55, 124, 177); text-align: left; font-size: 2em; line-height: 1em; font-size-adjust: none; font-stretch: normal; color: #e0e0d5; font-family: Georgia; font-variant: small-caps; font-weight: bold;vertical-align: middle">
				GeneDB
			</td>
			<td width="30%" align="center" style="text-align: center; font-size: 2em; line-height: 1em; font-size-adjust: none; font-stretch: normal; color: #e0e0d5; font-family: Georgia; font-variant: small-caps; font-weight: bold;vertical-align: top">
				<c:if test="${!empty name}">
					<font style="font-size:1em;"> ${name}</font>
				</c:if>
			</td>
			<td align="right" width="40%">
				<form name="query" action="<c:url value="/"/>NamedFeature" method="get" style="vertical-align: middle;">
				<input id="query" name="name" type="text" style="width: 150px; border:0; padding: 4px; vertical-align: middle;" align="middle"/>
							<!--<select id="type" name="featureType">
								<option value="all">All Names</option>
								<option value="geneid">Gene ID</option>
								<option value="genename">Gene Name</option>
								<option value="product">Product</option>
							</select>
				--><input type="submit" value="Search" title="Search" align="middle" style="vertical-align: middle; margin:0 0 0 7px;background:transparent url(<c:url value="/"/>includes/images/bgsprite.gif) no-repeat -47px top;border:0;width:66px;height:24px;font-weight:normal;vertical-align:bottom;cursor:pointer;font-size:1em;font-weight:bold;"/>
			</td>
		</tr>
	</table>
		<div id="demo-container">
			<ul id="simple-menu">
				<li><a href="http://www.13styles.com/css-menus/simple-menu/" title="Home">About Us</a></li>
				<li>
					<a href="http://www.13styles.com/css-menus/simple-menu/" title="Home">Browse</a>
					<ul class="w">
 						<li class="w">
 							<a class="w" href="http://pathdbsrv1a.sanger.ac.uk:8180/genedb-web/" title="Keyword Search">Products</a>
 						</li>
  						<li class="w"><a class="w" href="http://pathdbsrv1a.sanger.ac.uk:8180/genedb-web/" title="Blast Search">Curation</a></li>
  						<li class="w"><a class="w" href="http://pathdbsrv1a.sanger.ac.uk:8180/genedb-web/" title="Emowse Search">GO Terms</a></li>
					</ul>
				</li>
				<li><a href="http://www.13styles.com/css-menus/simple-menu/" title="Home">Downloads</a></li>
				<li>
					<a href="http://pathdbsrv1a.sanger.ac.uk:8180/genedb-web/" title="Searches">Searches</a>
					<ul>
 						<li class="w">
 							<a class="w" href="http://pathdbsrv1a.sanger.ac.uk:8180/genedb-web/" title="Keyword Search">Keyword</a>
 						</li>
  						<li class="w"><a class="w" href="http://pathdbsrv1a.sanger.ac.uk:8180/genedb-web/" title="Blast Search">Blast</a></li>
  						<li class="w"><a class="w" href="http://pathdbsrv1a.sanger.ac.uk:8180/genedb-web/" title="Emowse Search">Emowse</a></li>
  						<li class="w"><a class="w" href="http://pathdbsrv1a.sanger.ac.uk:8180/genedb-web/" title="Motif Search">Motif Search</a></li>
					</ul>
				</li>
				<li><a href="http://pathdbsrv1a.sanger.ac.uk:8180/genedb-web/" title="Home" class="current">Home</a></li>
				
			</ul>
		</div>