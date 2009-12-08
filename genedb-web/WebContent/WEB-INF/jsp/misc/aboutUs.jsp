<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:header title="About Us : GeneDB" />
<format:page>
<br>

<div id="col-2-1">

<br />

<style>
.readableText
{
	font-size: 1.2em;
	line-height:1.5em;
	text-align:justify;
	margin-left:1em;
}
.readableText a
{
    color:#4381a7;
}
ul {
    padding-left:1em;
}
</style>

<div id="readableText">
<h2>About GeneDB</h2>
<p>The GeneDB project is a core part of the Sanger Institute Pathogen Informatics activities.
 Its primary goals are:</p>
<ul>
<li>to provide reliable access to the latest sequence data and annotation/curation for the whole range of
organisms sequenced by the Pathogen group.</li>
<li>to develop the website and other tools to aid the community in accessing and obtaining
the maximum value from this data.</li>
</ul>
<p>GeneDB currently provides access to 37 genomes, from various stages of the sequencing curation
pipeline, from early access to partial genomes with automatic annotation through to complete
genomes with extensive manual curation.</p>

</div>
</format:page>
