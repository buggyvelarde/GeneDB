<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Homepage" />
<format:page>
<br>




  
<div id="col-1-1">  
    <div class="readableText">
        <h1>${full}</h1>
        <div id="readableContent">${content}</div>
    </div>
</div>
  

<div id="col-1-2">
	
	
	 <h2>About GeneDB</h2>  
    <div class="light-grey-top"></div>
    <div class="light-grey">
        <P><I>GeneDB is a database that provides a window into ongoing annotation and curation at the Sanger Institute.</I></P>
    </div>
    <div class="light-grey-bot"></div>
    
    
    <h2>Links</h2>

    <div class="baby-blue-top"></div>
        <div class="baby-blue">
            <div id="readableContentLinks" >${links}</div>
        </div>
    <div class="baby-blue-bot"></div>





</div>




</format:page>
