<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<format:header title="Feature: ${dto.uniqueName}">
<!--<script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/jquery/jquery-genePage-combined.js"/>"></script>
--><script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/contextMap.js"/>"></script>
<misc:url value="/" var="base"/>
</format:header>
<format:page onLoad="">
<br>

<div id="col-2-1">
<div id="navigatePages">
    <query:navigatePages />
</div>


<c:if test='${taxonNodeName == "Pchabaudi"}'>
<br />
<P>The Wellcome Trust Sanger Institute plans on publishing the completed and annotated sequences (i.e. 8X assembly and updated
annotation) of <i>P.chabaudi</i> AS in a peer reviewed journal as soon as possible. Permission of the principal investigator
should be obtained before publishing analyses of the sequence/open reading frames/genes on a chromosome or genome scale.</P>
<br />
</c:if>
<c:if test='${taxonNodeName == "Pberghei"}'>
<br />
<P>The Wellcome Trust Sanger Institute plans on publishing the completed and annotated sequences (i.e. 8X assembly and updated
annotation) of <i>P.berghei</i> ANKA in a peer reviewed journal as soon as possible. Permission of the principal investigator
should be obtained before publishing analyses of the sequence/open reading frames/genes on a chromosome or genome scale.</P>
<br />
</c:if>


<br>


<h2 style="padding-top:0px;margin-top:0px;">General Information</h2>
<div id="col-4-1">

<div class="main-grey-3-4-top"></div>
<div class="light-grey">
<span class="float-right grey-text"><misc:displayDate time="${dto.lastModified}" message="Last Modified" /></span>
<h2>Summary</h2>
<table cellpadding="0" cellspacing="4" border="0" class="sequence-table">


  <tr>
    <th>Gene Name</th><td>${gene.uniqueName}</td>
    
    <c:forEach items="${ gene.transcripts }" var="dto" >
    
        <th>Transcript Name</th><td>${dto.uniqueName}</td>
        
        <div id="geneDetails">
        <jsp:include page="geneDetails.jsp"/>
        </div>
    
    </c:forEach>
    
    
  </tr>
  
  
<%-- 
    <tr><th>Feature Type</th><td>${gene.typeDescription} ??</td></tr>

<c:if test="${!empty gene.synonymsByTypes}">
  <c:forEach var="type" items="${gene.synonymsByTypes}">
  <tr>
    <th>${type.key}</th>
    <td>
      <misc:listItems collection="${type.value}"/>
    </td>
  </tr>
  </c:forEach>
</c:if>
 --%>

</table>



</div>
</format:page>
