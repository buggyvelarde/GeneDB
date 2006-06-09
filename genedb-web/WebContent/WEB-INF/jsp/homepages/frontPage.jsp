<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header name="Welcome to the GeneDB website Next Generation"></format:header>

<p>&nbsp;<br>The GeneDB project is a core part of the Sanger Institute Pathogen
Sequencing Unit's (PSU) activities. Its primary goals are:
<ul>
 <li>to provide reliable storage of, and access to the latest sequence data and
annotation/curation for the whole range of organisms sequenced by the PSU.</li>
 <li>to develop the website and other tools to aid the community in accessing
and obtaining the maximum value from this data.</li>
</ul>

<hr />

<p>  
This is a quick set of example links
</p>
<table width="100%">
<tr><td>
<ul>
<li><a href="<c:url value="/Organism"/>">Organism List</a> (Partial)</li>
<li><a href="http://localhost:8080/genedb-web/Genome/Trypanasoma_brucei_brucei/current">Common URL</a> (Partial)</li>
<li><a href="<c:url value="/examples/JSMenuTest.jsp"/>">Query drop-down</a> (OK)</li>
<li><a href="./Admin/LockExaminer">Lock examiner</a> (OK)</li>
<li><a href="./Search/FeatureByName?name=Tb927.2.4760">New tbrucei example</a></li>
<li><a href="./Search/FeatureByName?name=Tb927.2.4760">New pombe example</a></li>
<li><a href="./Search/DummyGeneFeature">New off-line example</a></li>
</ul></td><td><ul>
<li><a href="<c:url value="/Search/BooleanQuery?taxId=12345"/>">Boolean query page</a> (Partial)</li>
<li><a href="<c:url value="/examples/pfamTest.jsp"/>">Pfam auto-complete</a> (Partial)</li>
<li><a href="<c:url value="/dwr/test/PfamLookup"/>">DWR Pfam page</a> (OK)</li>
<li><a href="./Search/FindCvByName"/>CV Browser (check)</a></li>
<li><a href="./Search/FeatureByName?name=PF08_0098">New malaria example</a></li>
<li><a href="./Search/FeatureByName?name=Tb927.2.4760">New pombe alt-splicing example</a></li>
</ul></td></tr></table>

<format:footer />
