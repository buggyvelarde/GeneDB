<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url value="/" var="base"/>

<format:headerRound name="${dto.organismHtmlShortName}" organism="${dto.organismCommonName}" title="Gene Page ${dto.geneName}" bodyClass="genePage"
onLoad="initContextMap('${base}', '${dto.organismCommonName}', '${dto.topLevelFeatureUniqueName}', ${dto.topLevelFeatureLength}, ${dto.min}, ${dto.max}, '${dto.uniqueName}');">

<st:init />
<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
<%-- Here we put those styles that contain URLs --%>
<style>
* html img#chromosomeThumbnailImage {
    position:relative;
    behavior: expression((this.runtimeStyle.behavior="none")&&(this.pngSet?this.pngSet=true:(this.nodeName == "IMG" && this.src.toLowerCase().indexOf('.png')>-1?(this.runtimeStyle.backgroundImage = "none",
        this.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + this.src + "', sizingMethod='image')",
        this.src = "<c:url value="/includes/images/transparentPixel.gif"/>"):(this.origBg = this.origBg? this.origBg :this.currentStyle.backgroundImage.toString().replace('url("','').replace('")',''),
        this.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + this.origBg + "', sizingMethod='crop')",
        this.runtimeStyle.backgroundImage = "none")),this.pngSet=true)
    );
}

.closeButton a, .homeButton a {
    position: absolute;
    top: 0px;
    left: 0px;
    width: 16px;
    height: 16px;
    cursor: pointer;
}
.closeButton a {
    background: transparent url(<c:url value="/includes/images/chr-buttons.png"/>) 0px 0px no-repeat;
}
.closeButton a:hover {
    background: transparent url(<c:url value="/includes/images/chr-buttons.png"/>) 0px -16px no-repeat;
}
.homeButton a {
    background: transparent url(<c:url value="/includes/images/chr-buttons.png"/>) -16px 0px no-repeat;
}
.homeButton a:hover {
    background: transparent url(<c:url value="/includes/images/chr-buttons.png"/>) -16px -16px no-repeat;
}
</style>

<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/jquery/jquery-genePage-combined.js"/>"></script>
<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/genedb/contextMap.js"/>"></script>
<script language="javascript">
//<![CDATA[
function addToBasket(geneid){
    var url = "/ci-web/Basket";
    var postData = "name="+geneid;
    var callback = {
        success: function(o) {
            document.getElementById('basketbutton').src = "/ci-web/includes/images/alreadyInBasket.gif";
            document.getElementById('basketbutton').onclick = null;
        },
        failure: function(o) {
            write("AJAX request for add button failed");
        }
    }

    var transaction = YAHOO.util.Connect.asyncRequest('POST', url, callback, postData);
}
//]]>
</script>
</format:headerRound>

<div id="navigatePages">
	<query:navigatePages />
</div>
<!-- Context Map -->
<div id="contextMapOuterDiv">
    <div id="contextMapTopPanel">
        <div id="contextMapThumbnailDiv"></div>
    </div>
    <div id="contextMapDiv">
        <div id="contextMapLoading">
            <img src="<c:url value="/includes/images/default/grid/loading.gif"/>" id="contextMapLoadingImage">
            Loading...
        </div>
        <div class="homeButton"><a href="#" title="Home" onclick="selectLoaded(); return false;"></a></div>
        <div id="contextMapContent" class="contextMapContent"><div class="highlighter"></div></div>
    </div>
</div>
<%-- IE6 fails if this is nested within the contextMapOuterDiv. --%>
<div id="contextMapInfoPanel">
    <div class="closeButton"><a href="#"></a></div>
    <div id="loadDetails"><a href="#">Load details &raquo;</a></div>
    <div class="value" id="selectedGeneName"></div>
    <div class="value" id="selectedGeneProducts"></div>
</div>

<format:genePageSection id="geneDetailsLoading" className="greyBox">
    <img src="<c:url value="/includes/images/default/grid/loading.gif"/>">
    Loading Gene Details...
</format:genePageSection>
<div id="geneDetails">
    <jsp:include page="geneDetails.jsp"/>
</div>

<format:footer />