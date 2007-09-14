<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<script src="<c:url value="/includes/scripts/script.aculo.us/prototype.js"/>" type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/script.aculo.us/scriptaculous.js"/>" type="text/javascript"></script>
<style type="text/css">.infoMacro { border-style: solid; border-width: 1px; border-color: #c0c0c0; background-color: #ffffff; text-align:left;}.informationMacroPadding { padding: 5px 0 0 5px; }</style>
<script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-base.js"/>"></script>     <!-- ENDLIBS -->
    <script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-all.js"/>"></script>
	 <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/ext-all.css"/>" />
	 <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/grid.css"/>" />
	<script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-history.js"/>"></script>
	<style id="topic-grid-cssrules" type="text/css">
		#topic-grid .x-grid-col-topic {
		white-space:normal;width:489px;
		}
		#topic-grid .x-grid-hd-topic {
		width:489px;}
		#topic-grid .x-grid-td-topic {
		
		}
		#topic-grid .x-grid-split-topic {
		
		}
		#topic-grid .x-grid-col-1 {
		width:99px;
		}
		#topic-grid .x-grid-hd-1 {
		width:99px;}
		#topic-grid .x-grid-td-1 {
		display:none;
		}
		#topic-grid .x-grid-split-1 {
		display:none;
		}
		#topic-grid .x-grid-col-last {
		width:149px;
		}
		#topic-grid .x-grid-hd-last {
		width:149px;}
		#topic-grid .x-grid-td-last {
		
		}
		#topic-grid .x-grid-split-last {
		
		}
	</style>
<format:header name="History">
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>

<div style="width:675px;" class="x-box-blue">
        <div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>
        <div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc">
            <h3 style="margin-bottom:5px;">Queries stored in History</h3>
            <div id="topic-grid" style="border:1px solid #99bbe8;overflow: hidden; width: 650px; height: 200px;position:relative;left:0;top:0;"></div>
        </div></div></div>
        <div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>
    </div>
    
    <select name="download" id="download" style="display: none;">
		<option value="NO">NO</option>
		<option value="YES">YES</option>
	</select>