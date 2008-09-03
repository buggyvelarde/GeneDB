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
      <h1>Welcome to GeneDB</h1><br>
      <p>This site is to provide access to the latest annotation on the
      <i>P. falciparum</i> project. Periodic snapshots of the data are
      also made available at
      <a href="http://www.plasmodb.org">PlasmoDB</a></p><br>

      <p>It also provides a preview of the new GeneDB website. This site
      is currently under development, and not running on fault resistant
      hardware, so may not be constantly available.</p><br>

      <p>To locate a gene, enter its ID or product, using wildcards, in
      the text box in the top right, or use the browsable categories in
      the menu just below it.</p><br>

        <h3>Example Genes</h3>
        <ul>
        <li><a href="<c:url value="/NamedFeature?name=PF14_0641"/>">PF14_0641</a></li>
        <li><a href="<c:url value="/NamedFeature?name=PF07_0048"/>">PF07_0048 (VAR)</a></li>
        <li><a href="<c:url value="/NamedFeature?name=PFB0888w"/>">PFB0888w</a> - an alternately spliced gene</li>
      </ul>


      <h3>Organism List</h3>
      <ul>
      <li><a href="<c:url value="/Homepage?organism=Pfalciparum"/>"><i>Plasmodium falciparum</i></a></li>
        <li><a href="<c:url value="/Homepage?organism=Pberghei"/>"><i>Plasmodium berghei</i></a></li>
        <li><a href="<c:url value="/Homepage?organism=Pchabaudi"/>"><i>Plasmodium chabaudi</i></a></li>
        <li><a href="<c:url value="/Homepage?organism=Pknowlesi"/>"><i>Plasmodium knowlesi</i></a></li>
        <li><a href="<c:url value="/Homepage?organism=Pvivax"/>"><i>Plasmodium vivax</i></a></li>
        <li><a href="<c:url value="/Homepage?organism=Pyoelli"/>"><i>Plasmodium yoelii</i></a></li>
    </ul>
    </div>
</format:genePageSection>
<p></p>

<format:footer />