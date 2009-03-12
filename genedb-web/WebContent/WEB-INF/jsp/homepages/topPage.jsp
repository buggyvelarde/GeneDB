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
    <p>This resource provides access to genome-scale datasets for a range of pathogenic
    organisms and is a beta version of the new GeneDB website. Please be aware that
    this site is stable but under active development. Hence only a subset of genomes
    and tools are accessible. Data can be retrieved using the navigation tools at
    the top right hand side of the page.</p><br>

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
      <li><i>Staphylococcus aureus</i> MRSA252</li>
      <li><i>Staphylococcus aureus</i> MSSA476</li>
      <li><i>Staphylococcus aureus</i> EMRSA15</li>
      <li><i>Staphylococcus aureus</i> TW20</li>
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
<td width="50%"><h2><!-- <b>Helminths</b>--></h2>
      <!--  <ul>
      <li><i>Schistosoma mansoni</i></li>
    </ul>--></td>
</tr>
<tr>
<td colspan="2">
        <h3><b>Example Genes</b></h3>
        <ul>
        <li><i>Plasmodium falciparum</i> <a href="<c:url value="/NamedFeature?name=PF14_0641"/>">PF14_0641</a></li>
        <li><i>Leishmania major</i> <a href="<c:url value="/NamedFeature?name=LmjF07.1060"/>">LmjF07.1060</a></li>
        <li><i>Trypanasoma brucei</i> <a href="<c:url value="/NamedFeature?name=Tb927.1.710"/>">Tb927.1.710</a></li>
        <!-- <li><i>Schistosoma mansoni</i> <a href="<c:url value="/NamedFeature?name=Smp_000030"/>">Smp_000030</a></li> -->
      </ul>
      </td>
</table>

    </div>
</format:genePageSection>
<p></p>

<format:footer />