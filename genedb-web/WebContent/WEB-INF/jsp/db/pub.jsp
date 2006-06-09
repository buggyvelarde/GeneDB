<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header name="Publication Page" />
        	

<st:section name="Publication" id="gene_location" collapsed="false" collapsible="true" hideIfEmpty="true">

        	<p><b>Title:</b> ${pub.title}</p>
        	
			<p><b>Pages:</b> ${pub.pages}</p>
        	
        	<p><b>Year:</b> ${pub.pyear}</p>
        	        	
        	<p><b>Publication Place:</b> ${pub.pubplace}</p>
        	
        	<p><b>Issue:</b> ${pub.issue}</p>
        	
        	<p><b>Publisher:</b> ${pub.publisher}</p>
        	
        	<p><b>Volume:</b> ${pub.volume}</p>
        	        	
        	<p><b>Volume Title:</b> ${pub.volumetitle}</p>
        	
			<p><b>Series name:</b> ${pub.seriesName}</p>
			        	
        	<p><b>Mini-reference:</b> ${pub.miniref}</p>

           <c:forEach items=${pub.pubauthors} var="pa">
     		  <p>[${pa.rank}]&nbsp;&nbsp;${pa.givennames}&nbsp;${pa.surname}&nbsp;${pa.suffix}&nbsp;&nbsp;&nbsp;<c:if test="${pa.editor == true}">(Editor)</c:if></p>
		   </c:forEach>

           <c:forEach items=${pub.pubprops} var="pp">
     		  <p>[${pp.rank}]&nbsp;&nbsp;${pa.cvterm.name}=${pp.value}</p>
		   </c:forEach>

</st:section>

<format:footer />