<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="misc" uri="misc" %>
<format:header title="Homepage" />
<format:page>
<br>

<c:url value="/Homepage/" var="base2" />
<div id="col-1-1">
<h1>Datasets</h1>
<div class="main-light-grey-top"></div>
<div class="main-light-grey">
<table cellpadding="0" cellspacing="0" width="100%" class="dataset-table">
<tr>
<td align="center" valign="top" width="33%">
<h3>Apicomplexan Protozoa</h3>
<img src="<misc:url value="/includes/image/dataset-protozoa.jpg"/>" height="163" width="136" alt="Apicomplexan Protozoa" />
<db:homepageselect top="Apicomplexa" baseUrl="${base2}"/>
</td>
<td align="center" valign="top" width="33%">

<h3>Kinetoplastid Protozoa</h3>
<img src="<misc:url value="/"/>includes/image/dataset-protozoa.jpg" height="163" width="136" alt="Kinetoplastid Protozoa" />
<db:homepageselect top="Kinetoplastida" baseUrl="${base2}"/>
</td>
<td align="center" valign="top" width="33%">
<h3>Parasitic Helminths</h3>
<img src="<misc:url value="/"/>includes/image/dataset-parasitic-helminths.jpg" height="163" width="136" alt="Parasitic Helminths" />
<db:homepageselect top="Helminths" baseUrl="${base2}"/>
</td>

</tr>

<tr>
<td align="center" valign="top" width="33%">
<h3>Bacteria</h3>
<img src="<misc:url value="/"/>includes/image/dataset-bacteria.jpg" height="163" width="136" alt="Bacteria" />
<db:homepageselect top="bacteria" baseUrl="${base2}"/>
</td>
<td align="center" valign="top" width="33%">
<h3>Parasite Vectors</h3>
<img src="<misc:url value="/"/>includes/image/dataset-parasite-vectors.jpg" height="163" width="136" alt="Parasite Vectors" />
<db:homepageselect top="Root" baseUrl="${base2}"/>
</td>
<td align="center" valign="top" width="33%">
<h3>Viruses</h3>
<img src="<misc:url value="/"/>includes/image/dataset-viruses.jpg" height="163" width="136" alt="Viruses" />
<db:homepageselect top="Root" baseUrl="${base2}"/>
</td>
</tr>
</table>
</div>
<div class="main-light-grey-bot"></div>

</div><!-- end main content column -left -->

<div id="col-1-2">
<h2>Sequence searches</h2>
<div class="light-grey-top"></div>
<div class="light-grey">
<form method="post" action="">
<p class="block-para"><u>omniBLAST</u> (Multi-organism BLAST)</p>
<p class="block-para">Single organism BLAST: <db:homepageselect title="Select an organism" top="Root" baseUrl="/cgi-bin/blast/submitblast/" leafOnly="true" /></p>

</form>
</div>
<div class="light-grey-bot"></div>

<h2>Go to our</h2>
<div class="light-grey-top"></div>
<div class="light-grey">
&raquo; <a href="<misc:url value="/Query" />">Query page</a><br />
&raquo; <a href="<misc:url value="/cgi-bin/amigo/go.cgi"/>">AmiGO</a><br />
&raquo; <a href="<misc:url value="/JBrowse"/>">JBrowse</a><br />
&raquo; <a href="http://www.genedb.org/">Classic GeneDB</a><br />
</div>
<div class="light-grey-bot"></div>

<h2>Information</h2>
<div class="baby-blue-top"></div>
<div class="baby-blue">
<a href="<misc:url value="/Page/guide"/>">Guide to GeneDB</a>
<p>What is GeneDB, and what's in it?<br />
Navigating/Searching GeneDB<br />
Contacting Us/Feedback<br />
Privacy Policy<br />
</p>
<br />
<p><a href="<misc:url value="/Page/releases"/>">Data Release Policy</a></p>
</div>
<div class="baby-blue-bot"></div>

<h2>Links</h2>
<div class="baby-blue-top"></div>
<div class="baby-blue">
<p>PSU Sequencing Projects<br />
&raquo; <a href="http://www.sanger.ac.uk/Projects/Microbes/">Prokaryotes</a><br />

&raquo; <a href="http://www.sanger.ac.uk/Projects/Protozoa/">Eukaryotes (Protozoa)</a><br />
&raquo; <a href="http://www.sanger.ac.uk/Projects/Fungi/">Eukaryotes (Fungi)</a><br />
</p>
<br />
<p>
Software<br />
&raquo; <a href="http://www.sanger.ac.uk/Software/ACT/">ACT</a><br />
&raquo; <a href="http://www.sanger.ac.uk/Software/Artemis/">Artemis</a><br />

</p>
</div>
<div class="baby-blue-bot"></div>
</div><!-- end sidebar content columb -right -->

</format:page>
