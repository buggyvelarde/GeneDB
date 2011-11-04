<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<format:header title="Gene Sequences ${gene.displayName}">
<c:set var="primaryLoc" value="${gene.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.sourceFeature}" />
</format:header>
<format:page>
<div id="col-2-1">
    <c:if test="${!empty protein}">
    <format:genePageSection id="proteinSequence">
        <h2>Protein <c:if test="${pseudogenic == true}">(<span style="color:#C00008">Warning: conceptual protein that was constructed as pseudogene</span>)</c:if></h2>
        <br><a href="<misc:url value="/SequenceDistributor/${uniqueName}/PROTEIN/OMNIBLAST" />">Send to GeneDB omniBLAST</a>
        <a href="<misc:url value="/SequenceDistributor/${uniqueName}/PROTEIN/BLAST" />">Send to GeneDB BLAST</a>
        <a href="<misc:url value="/SequenceDistributor/${uniqueName}/PROTEIN/NCBI_BLAST" />">Send to BLAST at NCBI</a>
        <br><misc:format-sequence sequence="${protein}"/>
    </format:genePageSection>
    </c:if>

    <c:if test="${!empty gene_sequence}">
    <format:genePageSection id="splicedSequence">
        <h2>Gene Sequence</h2>
        <br><%-- <a href="<misc:url value="/SequenceDistributor/${uniqueName}/GENE_SEQUENCE/OMNIBLAST" />">Send to GeneDB omniBLAST</a> --%>
        <a href="<misc:url value="/SequenceDistributor/${uniqueName}/GENE_SEQUENCE/BLAST" />">Send to GeneDB BLAST</a>
        <a href="<misc:url value="/SequenceDistributor/${uniqueName}/GENE_SEQUENCE/NCBI_BLAST" />">Send to BLAST at NCBI</a>
        <br><misc:format-sequence sequence="${gene_sequence}"/>
    </format:genePageSection>
    </c:if>

    <c:if test="${!empty transcript && fn:length(transcript) != fn:length(gene_sequence)}">
    <format:genePageSection id="splicedSequence">
        <h2>Full transcript</h2>
        <br><a href="<misc:url value="/SequenceDistributor/${uniqueName}/TRANSCRIPT/OMNIBLAST" />">Send to GeneDB omniBLAST</a>
        <a href="<misc:url value="/SequenceDistributor/${uniqueName}/TRANSCRIPT/BLAST" />">Send to GeneDB BLAST</a>
        <a href="<misc:url value="/SequenceDistributor/${uniqueName}/TRANSCRIPT/NCBI_BLAST" />">Send to BLAST at NCBI</a>
        <br><misc:format-sequence sequence="${transcript}"/>
    </format:genePageSection>
    </c:if>

    <c:if test="${!empty cds}">
    <format:genePageSection id="splicedSequence">
        <h2>CDS</h2>
        <br><%-- <a href="<misc:url value="/SequenceDistributor/${uniqueName}/CDS/OMNIBLAST" />">Send to GeneDB omniBLAST</a> --%>
        <a href="<misc:url value="/SequenceDistributor/${uniqueName}/CDS/BLAST" />">Send to GeneDB BLAST</a>
        <a href="<misc:url value="/SequenceDistributor/${uniqueName}/CDS/NCBI_BLAST" />">Send to BLAST at NCBI</a>
        <br><misc:format-sequence sequence="${cds}"/>
    </format:genePageSection>
    </c:if>
</div>
</format:page>