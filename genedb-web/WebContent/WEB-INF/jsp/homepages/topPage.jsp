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
      <li><i>Plasmodium falciparum</i> <a href="<c:url value="/NamedFeature?name=PF14_0641"/>">PF14_0641</a></li>
        <li><i>Plasmodium berghei</i> <a href="<c:url value="/NamedFeature?name=PB000600.02.0"/>">PB000600.02.0</a></li>
        <li><i>Plasmodium chabaudi</i> <a href="<c:url value="/NamedFeature?name=PCAS_010190"/>">PCAS_010190</a></li>
        <li><i>Plasmodium knowlesi</i> <a href="<c:url value="/NamedFeature?name=PKH_010100"/>">PKH_010100</a></li>
        <li><i>Plasmodium vivax</i> <a href="<c:url value="/NamedFeature?name=PVX_087765"/>">PVX_087765</a></li>
        <li><i>Plasmodium yoelii</i> <a href="<c:url value="/NamedFeature?name=PY00001"/>">PY00001</a></li>
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
      <li><i>Leishmania braziliensis</i> <a href="<c:url value="/NamedFeature?name=LbrM31_V2.1650"/>">LbrM31_V2.1650</a></li>
      <li><i>Leishmania infantum</i> <a href="<c:url value="/NamedFeature?name=LinJ31_V3.1460"/>">LinJ31_V3.1460</a></li>
      <li><i>Leishmania major</i> <a href="<c:url value="/NamedFeature?name=LmjF31.1430"/>">LmjF31.1430</a></li>
      <li><i>Trypanasoma brucei brucei 427</i> <a href="<c:url value="/NamedFeature?name=Tb427.BES40.22"/>">Tb427.BES40.22</a></li>
      <li><i>Trypanasoma brucei brucei 927</i> <a href="<c:url value="/NamedFeature?name=Tb927.1.700"/>">Tb927.1.700</a></li>
      <li><i>Trypanasoma congolense</i> <a href="<c:url value="/NamedFeature?name=TcIL3000.1.120"/>">TcIL3000.1.120</a></li>
      <li><i>Trypanasoma vivax</i> <a href="<c:url value="/NamedFeature?name=TvY486_0102140"/>">TvY486_0102140</a></li>
    </ul>
    </td>
<td width="50%"><h2><b>Helminths</b></h2>
      <ul>
      <li><i>Schistosoma mansoni</i> <a href="<c:url value="/NamedFeature?name=Smp_000030.1:mRNA"/>">Smp_000030</a></li>
    </ul></td>
</tr>

</table>

    </div>
</format:genePageSection>
<p></p>

<format:footer />