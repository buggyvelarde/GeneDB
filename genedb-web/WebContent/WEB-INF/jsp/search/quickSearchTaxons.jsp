<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>


<html>
<head>
    <script type="text/javascript">
    
    </script>   
    
    <st:init />
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</head>
<body>
    	   <table border=0>
		  <c:forEach var="map" items="${taxonGroup}">
			 <tr>
			 	<td>
			 	   <a  href="${pageContext.request.contextPath}/QuickSearchQuery?q=quickSearchQuery&taxons=${map.key}&searchText=${param.searchText}&allNames=${param.allNames}&pseudogenes=${param.pseudogenes}&product=${param.product}"
			 	       target="_parent">
					   <small>${map.key}(${map.value})</small>
				    </a>
				</td>
			 </tr>
		  </c:forEach>
	   </table>
</body>
</html>