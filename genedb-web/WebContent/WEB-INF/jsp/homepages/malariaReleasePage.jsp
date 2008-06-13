<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url value="/" var="base"/>

<format:headerRound organism="${organism}" name="Organism: GeneDB" title="Organism: GeneDB" bodyClass="genePage">

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
    
    <p>To locate a gene, enter its ID or product, using wildcards, in 
    the text box in the top right, or use the browsable categories in 
    the menu just below it.</p>
    
    <h3>Example Genes</h3>
    <ul>
    <li>Example 1 - fast, well known gene</li>
    <li>Example 2 - lots of annotation</li>
    <li>Example 3 - alternate splicing</li>
    </ul>
    
    
    <h3>Organism List</h3>
    <ul>
		<li><a href="${base}/Homepage?organism=Pfalciparum"><i>Plasmodium falciparum</i></a></li>
    	<li><a href="${base}/Homepage?organism=Pberghei"><i>Plasmodium berghei</i></a></li>
    	<li><a href="${base}/Homepage?organism=Pchabaudi"><i>Plasmodium chabaudi</i></a></li>
    	<li><a href="${base}/Homepage?organism=Pknowlesi"><i>Plasmodium knowlesi</i></a></li>
    	<li><a href="${base}/Homepage?organism=Pvivax"><i>Plasmodium vivax</i></a></li>
    	<li><a href="${base}/Homepage?organism=Pyoelli"><i>Plasmodium yoelii</i></a></li>
	</ul>
                        
</format:genePageSection>
<p></p>

<format:footer />