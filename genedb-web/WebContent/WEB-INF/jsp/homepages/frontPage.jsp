<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Homepage" />
<format:page>
<br>

<script type="text/javascript"><!--//--><![CDATA[//><!--
startList = function() {
    if (document.all&&document.getElementById) {
        navRoot = document.getElementById("nav");
        for (i=0; i<navRoot.childNodes.length; i++) {
            node = navRoot.childNodes[i];
            if (node.nodeName=="LI") {
                node.onmouseover=function() {
                    this.className+=" over";
                }
                node.onmouseout=function() {
                    this.className=this.className.replace(" over", "");
                }
            }
        }
    }
}
window.onload=startList;

//--><!]]></script>
</head>


<div id="col-1-1">
<h1>Datasets</h1>
<div class="main-light-grey-top"></div>
<div class="main-light-grey">
<table cellpadding="0" cellspacing="0" width="100%" class="dataset-table">
<tr>
<td align="center" valign="top" width="33%">
<h3>Apicomplexan Protozoa</h3>
<img src="/includes/image/dataset-protozoa.jpg" height="163" width="136" alt="Apicomplexan Protozoa" />
<select>
<option>Choose...</option>
</select>
</td>
<td align="center" valign="top" width="33%">

<h3>Kinetoplastid Protozoa</h3>
<img src="/includes/image/dataset-protozoa.jpg" height="163" width="136" alt="Kinetoplastid Protozoa" />
<select>
<option>Choose...</option>
</select>
</td>
<td align="center" valign="top" width="33%">
<h3>Parasitic Helminths</h3>
<img src="/includes/image/dataset-parasitic-helminths.jpg" height="163" width="136" alt="Parasitic Helminths" />
<select>
<option>Choose...</option>
</select>
</td>

</tr>

<tr>
<td align="center" valign="top" width="33%">
<h3>Bacteria</h3>
<img src="/includes/image/dataset-bacteria.jpg" height="163" width="136" alt="Bacteria" />
<select>
<option>Choose...</option>
</select>
</td>
<td align="center" valign="top" width="33%">
<h3>Parasite Vectors</h3>
<img src="/includes/image/dataset-parasite-vectors.jpg" height="163" width="136" alt="Parasite Vectors" />
<select>

<option>Choose...</option>
</select>
</td>
<td align="center" valign="top" width="33%">
<h3>Viruses</h3>
<img src="/includes/image/dataset-viruses.jpg" height="163" width="136" alt="Viruses" />
<select>
<option>Choose...</option>
</select>
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
<p class="block-para"><input type="button" value=" Go to " /> &nbsp; single organism BLAST:</p>
<p class="block-para"><select><option>Choose...</option></select></p>

</form>
</div>
<div class="light-grey-bot"></div>

<h2>Go to our</h2>
<div class="light-grey-top"></div>
<div class="light-grey">
&raquo; <a href="">Main search page</a><br />
&raquo; <a href="">Complex querying page</a><br />
&raquo; <a href="">AmiGO</a><br />

&raquo; <a href="">List Download</a>
</div>
<div class="light-grey-bot"></div>

<h2>Information</h2>
<div class="baby-blue-top"></div>
<div class="baby-blue">
<a href="">Guide to GeneDB</a>
<p>What is GeneDB, and what's in it?<br />
Navigating/Searching GeneDB<br />
Contacting Us/Feedback<br />

Privacy Policy<br />
</p>
<br />
<p><a href="">Data Release Policy</a></p>
</div>
<div class="baby-blue-bot"></div>

<h2>Links</h2>
<div class="baby-blue-top"></div>
<div class="baby-blue">
<p>PSU Sequencing Projects<br />
&raquo; <a href="">Prokaryotes</a><br />

&raquo; <a href="">Eukaryotes (Protozoa)</a><br />
&raquo; <a href="">Eukaryotes (Fungi)</a><br />
</p>
<br />
<p>
Software<br />
&raquo; <a href="">ACT</a><br />
&raquo; <a href="">Artemis</a><br />

</p>
</div>
<div class="baby-blue-bot"></div>
</div><!-- end sidebar content columb -right -->

</format:page>
