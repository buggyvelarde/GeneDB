<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Feature: ${dto.uniqueName}">
<!--<script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/jquery/jquery-genePage-combined.js"/>"></script>
--><script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/contextMap.js"/>"></script>
<misc:url value="/" var="base"/>
</format:header>
<format:page onLoad="initContextMap('${base}', '${dto.organismCommonName}', '${dto.topLevelFeatureUniqueName}', ${dto.topLevelFeatureLength}, ${dto.min}, ${dto.max}, '${dto.uniqueName}');">
<br>

<div id="col-2-1">
<div id="navigatePages">
    <query:navigatePages />
</div>
<%-- Here we put those styles that contain URLs --%>
<style>
* html img#chromosomeThumbnailImage {
    position:relative;
    behavior: expression((this.runtimeStyle.behavior="none")&&(this.pngSet?this.pngSet=true:(this.nodeName == "IMG" && this.src.toLowerCase().indexOf('.png')>-1?(this.runtimeStyle.backgroundImage = "none",
        this.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + this.src + "', sizingMethod='image')",
        this.src = "<misc:url value="/includes/images/transparentPixel.gif"/>"):(this.origBg = this.origBg? this.origBg :this.currentStyle.backgroundImage.toString().replace('url("','').replace('")',''),
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
    background: transparent url(<misc:url value="/includes/images/chr-buttons.png"/>) 0px 0px no-repeat;
}
.closeButton a:hover {
    background: transparent url(<misc:url value="/includes/images/chr-buttons.png"/>) 0px -16px no-repeat;
}
.homeButton a {
    background: transparent url(<misc:url value="/includes/images/chr-buttons.png"/>) -16px 0px no-repeat;
}
.homeButton a:hover {
    background: transparent url(<misc:url value="/includes/images/chr-buttons.png"/>) -16px -16px no-repeat;
}
</style>


<!-- Context Map -->
<div id="contextMapOuterDiv">
    <div id="contextMapTopPanel">
        <div id="contextMapThumbnailDiv"></div>
    </div>
    <div id="contextMapDiv">
        <div id="contextMapLoading">
            <img src="<misc:url value="/includes/image/loading.gif"/>" id="contextMapLoadingImage">
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
<%--
<format:genePageSection id="geneDetailsLoading" className="greyBox">
    <img src="<misc:url value="/includes/image/loading.gif"/>">
    Loading Gene Details...
</format:genePageSection>
 --%>

<br />
<div id="geneDetails">
    <jsp:include page="geneDetails.jsp"/>
</div>

</div>
</format:page>
