<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url value="/" var="base"/>

<format:headerRound organism="${organism}" name="${pageName}" title="GeneDB" bodyClass="genePage">

<st:init />
<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>


<p></p>
<format:genePageSection className="greyBox">
    <div id="frontPage">
      <p>This site is to provide access to the latest annotation on the
      <i>P. falciparum</i> project. Periodic snapshots of the data are
      also made available at
      <a href="http://plasmodb.org">PlasmoDB</a></p><br>

      <p>It also provides a preview of the new GeneDB website. This site
      is currently under development, and not running on fault resistant
      hardware, so may not be constantly available.</p><br>

      <p>To locate a gene, enter its ID or product, using wildcards, in
      the text box in the top right, or use the browsable categories in
      the menu just below it.</p><br>

<table width="100%">
<tr>
<td width="50%"><h2><b>Apicomplexa</b></h2>
      <ul>
      <li><i>Plasmodium falciparum</i></li>
        <li><i>Plasmodium berghei</i></li>
        <li><i>Plasmodium chabaudi</i></li>
        <li><i>Plasmodium knowlesi</i></li>
        <li><i>Plasmodium vivax</i></li>
        <li><i>Plasmodium yoelii</i></li>
    </ul>
    </td>
<td width="50%"><h2><b>Bacteria</b></h2>
      <ul>
      <li>Matt to provide list or text</li>
    </ul>
    </td>
</tr>
<tr>
<td width="50%"><h2><b>Kinetoplastida</b></h2>
    <ul>
      <li><i>Leishmania braziliensis</i></li>
      <li><i>Leishmania infantum</i></li>
      <li><i>Leishmania major</i></li>
    </ul>
    </td>
<td width="50%"><h2><b>Helminths</b></h2>
      <ul>
      <li><i>Schistosoma mansoni</i></li>
    </ul></td>
</tr>
<tr>
<td colspan="2">
        <h3><b>Example Genes</b></h3>
        <ul>
        <li><a href="<c:url value="/NamedFeature?name=PF14_0641"/>">PF14_0641</a></li>
        <li><a href="<c:url value="/NamedFeature?name=PF07_0048"/>">???</a></li>
        <li><a href="<c:url value="/NamedFeature?name=PFB0888w"/>">BG27 ???</a> - an alternately spliced gene</li>
        <li><a href="<c:url value="/NamedFeature?name=Smp_000030"/>">Smp_000030</a></li>
      </ul>
      </td>
</table>

    </div>
</format:genePageSection>
<p></p>

<format:footer />