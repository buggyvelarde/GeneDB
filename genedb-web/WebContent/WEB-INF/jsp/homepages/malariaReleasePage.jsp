<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url value="/" var="base"/>

<format:headerRound name="Organism: GeneDB" title="Organism: GeneDB" bodyClass="genePage">

<st:init />
<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>



<p></p>
<format:genePageSection className="greyBox">
    <h1>Welcome to GeneDB</h1>
    <p>This site is to provide access to the latest annotation on the 
    <i>P. falciparum</i> project. Periodic snapshots of the data are 
    also made available at 
    <a href="http://www.plasmodb.org">PlasmoDB</a></p>
    
    <p>It also provides a preview of the new GeneDB website. This site 
    is currently under development, and not running on fault resistant 
    hardware, so may not be constantly available.
    
    
    <h3>Example Genes</h3>
    <ul>
    <li>Example 1 - fast, well known gene</li>
    <li>Example 2 - lots of annotation</li>
    <li>Example 3 - alternate splicing</li>
    </ul>
    
    
    <h3>Organism List</h3>
    <ul>
    <li><a href="${base}/Homepage?organism=pfalciparum"><i>Plasmodium falciparum</i></a></li>
    <li><a href="${base}/Homepage?organism=pfalciparum"><i>Plasmodium falciparum</i></a></li>
    <li><a href="${base}/Homepage?organism=pfalciparum"><i>Plasmodium falciparum</i></a></li>
    <li><a href="${base}/Homepage?organism=pfalciparum"><i>Plasmodium falciparum</i></a></li>
    <li><a href="${base}/Homepage?organism=pfalciparum"><i>Plasmodium falciparum</i></a></li>
                    
    
    <p>
</format:genePageSection>
<p></p>

<format:footer />