<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="primaryLoc" value="${gene.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.sourceFeature}" />
<c:url value="/" var="base"/>

<format:headerRound name="${gene.organism.htmlShortName}" organism="${gene.organism.commonName}" title="Gene Sequences ${gene.displayName}" bodyClass="genePage">

<st:init />
<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
<%-- Here we put those styles that contain URLs --%>
<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/jquery/jquery-genePage-combined.js"/>"></script>
</format:headerRound>

    <c:if test="${!empty protein}">
    <format:genePageSection id="proteinSequence">
        <div class="heading">Protein</div>
        <br><a href="<c:url value="/SequenceDistributor" />?destination=OMNIBLAST&name=${uniqueName}&type=PROTEIN">Send to GeneDB omniBLAST</a>
        <a href="<c:url value="/SequenceDistributor" />?destination=BLAST&name=${uniqueName}&type=PROTEIN">Send to GeneDB BLAST</a>
        <a href="<c:url value="/SequenceDistributor" />?destination=NCBI_BLAST&name=${uniqueName}&type=PROTEIN">Send to BLAST at NCBI</a>
        <br><misc:format-sequence sequence="${protein}"/>
    </format:genePageSection>
    </c:if>

    <c:if test="${!empty gene_sequence}">
    <format:genePageSection id="splicedSequence">
        <div class="heading">Gene Sequence</div>
        <br><a href="<c:url value="/SequenceDistributor" />?destination=OMNIBLAST&name=${uniqueName}&type=GENE_SEQUENCE">Send to GeneDB omniBLAST</a>
        <a href="<c:url value="/SequenceDistributor" />?destination=BLAST&name=${uniqueName}&type=GENE_SEQUENCE">Send to GeneDB BLAST</a>
        <a href="<c:url value="/SequenceDistributor" />?destination=NCBI_BLAST&name=${uniqueName}&type=GENE_SEQUENCE">Send to BLAST at NCBI</a>
        <br><misc:format-sequence sequence="${gene_sequence}"/>
    </format:genePageSection>
    </c:if>

    <c:if test="${!empty transcript && fn:length(transcript) != fn:length(gene_sequence)}">
    <format:genePageSection id="splicedSequence">
        <div class="heading">Full transcript</div>
        <br><a href="<c:url value="/SequenceDistributor" />?destination=OMNIBLAST&name=${uniqueName}&type=TRANSCRIPT">Send to GeneDB omniBLAST</a>
        <a href="<c:url value="/SequenceDistributor" />?destination=BLAST&name=${uniqueName}&type=TRANSCRIPT">Send to GeneDB BLAST</a>
        <a href="<c:url value="/SequenceDistributor" />?destination=NCBI_BLAST&name=${uniqueName}&type=TRANSCRIPT">Send to BLAST at NCBI</a>
        <br><misc:format-sequence sequence="${transcript}"/>
    </format:genePageSection>
    </c:if>

    <c:if test="${!empty cds}">
    <format:genePageSection id="splicedSequence">
        <div class="heading">CDS</div>
        <br><a href="<c:url value="/SequenceDistributor" />?destination=OMNIBLAST&name=${uniqueName}&type=CDS">Send to GeneDB omniBLAST</a>
        <a href="<c:url value="/SequenceDistributor" />?destination=BLAST&name=${uniqueName}&type=CDS">Send to GeneDB BLAST</a>
        <a href="<c:url value="/SequenceDistributor" />?destination=NCBI_BLAST&name=${uniqueName}&type=CDS">Send to BLAST at NCBI</a>
        <br><misc:format-sequence sequence="${cds}"/>
    </format:genePageSection>
    </c:if>


<format:footer />