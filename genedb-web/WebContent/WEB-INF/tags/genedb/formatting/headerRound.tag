<%@ tag display-name="header1"
        body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="bodyClass" required="false" %>
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
<body class="${bodyClass}">
<% } else { %>
<body class="${bodyClass}" onLoad="${onLoad}">
<% } %>
<div id="header">
	<form name="query" action="<c:url value="/"/>NamedFeature" method="get" style="vertical-align: middle;">
		<table width="100%">
			<tr>
				<td align="left">GeneDB</td>
				<td align="center">
					<c:if test="${!empty name}">
						<font style="font-size:1em;"> ${name}</font>
					</c:if>
				</td>
				<td align="right">
					<input id="query" name="name" type="text" align="middle"/>
					<input id="submit" type="submit" value="Search" title="Search" align="middle" />
				</td>
			</tr>
		</table>
	</form>
	<div id="navigation">
		<ul id="simple-menu">
			<li>
				<a href="http://pathdbsrv1a.sanger.ac.uk:8180/genedb-web/" title="Searches">Searches</a>
				<ul>
					<li class="w">
						<a class="w" href="<c:url value="/NamedFeature"/>" title="Name Search">By Name/Product</a>
					</li>
					<li class="w">
						<a class="w" href="<c:url value="/"/>" title="Motif Search">Motif Search</a>
					</li>
				</ul>
			</li>
			<li>
				<a href="" title="Home">Browse</a>
				<ul class="w">
					<li class="w">
						<a class="w" href="<c:url value="/"/>BrowseCategory" title="Browse Products">Products</a>
					</li>
 					<li class="w">
 						<a class="w" href="<c:url value="/"/>BrowseCategory" title="Browse Curation">Curation</a>
 					</li>
 					<li class="w">
 						<a class="w" href="<c:url value="/"/>BrowseCategory" title="Browse GO Terms">GO Terms</a>
 					</li>
				</ul>
			</li>
		</ul>
	</div>
</div>