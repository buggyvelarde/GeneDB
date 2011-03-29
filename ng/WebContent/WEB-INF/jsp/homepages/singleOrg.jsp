<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="db" uri="db"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<format:header title="Homepage for ${fulltext}" />

<script type="text/javascript"
	src="<misc:url value="/includes/scripts/genedb/chromosoml.js"/>"></script>

<format:page>
	<br>

	<style>
.region,.region_type {
	width: 100%;
	display: block;
}
</style>

	<script>
		$(function() {
		    
			// all spinners here will use the same defaults
			$.fn.CallStatusSpinner.defaults = {
					height : 11.5,
	                width : 50,
	                img : '<misc:url value="/includes/image/spinner.gif"/>'
			};
			
			$('.spinner1').CallStatusSpinner();
			$('.spinner2').CallStatusSpinner();
			
			$('#annotation_statistics').AnnotationModificationReporter({
				organism : "com:${node.label}",
				baseHREF : '<misc:url value="/gene/"/>',
				spinner : ".spinner1"
			}); 
		    
			$("#regions").ChromosomePicker({
				organism : "com:${node.label}",
				on_select : function(region) {
					window.location = getBaseURL()
						+ "Homepage/${node.label}?region="
						+ region;
				},
				spinner : ".spinner2"
			});  
		    
			
		});
		
		
		
		
		
		
		
	</script>





	<div id="readableContent" style="display: none;"
		class="ui-state-default ui-corner-all">${content}</div>


	<center>
		<div id="imgcontainer" style="display:none;" ></div>
		<h1>The ${full} homepage on GeneDB</h1>
	</center>
	
	

	<div id="col-1-2">

		<h2>
			Annotation statistics <span class="spinner1">&nbsp;&nbsp;&nbsp;</span>
		</h2>

		<div class="light-grey-top"></div>
		<div class="light-grey">
			<div id='annotation_statistics'>None available.</div>
		</div>
		<div class="light-grey-bot"></div>

		<h2>
			Scaffolds <span class="spinner2">&nbsp;&nbsp;&nbsp;</span>
		</h2>

		<div class="baby-blue-top"></div>
		<div class="baby-blue">

			<div id="regions"></div>

		</div>
		<div class="baby-blue-bot"></div>

		<h2>Information</h2>
        
		<div class="light-grey-top"></div>
		<div class="light-grey">
                
			<p class="block-para">
				About <br /> &raquo; <a id="about">${full} on GeneDB </a> <br />

			</p>
		</div>
		<div class="light-grey-bot"></div>

	</div>




	<div id="col-1-2">


		<h2>Blast</h2>
		<div class="baby-blue-top"></div>
		<div class="baby-blue">
		<div id="regions2"></div>
			<p class="block-para">
				 &raquo; <a
					href="${baseUrl}blast/submitblast/GeneDB_${node.label}">Blast
					${full} </a> <br /> &raquo; <a
					href="<misc:url value="/blast/submitblast/GeneDB_proteins/omni" />">Multi-organism
					(proteins)</a><br /> &raquo; <a
					href="<misc:url value="/blast/submitblast/GeneDB_transcripts/omni" />">Multi-organism
					(transcripts and contigs/chromosomes)</a><br />
			</p>
		</div>
		<div class="baby-blue-bot"></div>

		<h2>Tools</h2>

		<div class="light-grey-top"></div>
		<div class="light-grey">
            
            
            <p class="block-para">
                 &raquo; <a href="http://www.genedb.org/web-artemis/">
                    Web artemis</a> <br /> &raquo; <a
                    href="${baseUrl}jbrowse/${node.label}/?tracks=Complex%20Gene%20Models/">Jbrowse</a>
                <br /> &raquo; <a href="<misc:url value="/cgi-bin/amigo/go.cgi"/>">AmiGO</a><br />
            </p>
		</div>
		<div class="light-grey-bot"></div>
        
        
        
        <h2>Searches</h2>
        <div class="baby-blue-top"></div>
        <div class="baby-blue">
            <P class="block-para">
                
                <c:forEach items="${queries}" var="query">
            
            &raquo; <a title="${query.queryDescription}"
                        href="<misc:url value="/Query/${query.realName}" />?taxonNodeName=${node.label}">${query.queryName}</a>
                    <br />

                </c:forEach>
            </P>
        </div>
        <div class="baby-blue-bot"></div>

	</div>

	<div id="col-1-2">

        

		<h2>Links</h2>
		<div class="light-grey-top"></div>
		<div class="light-grey">
		       
			<div id="readableContentLinks">${links}</div>
		</div>
		<div class="light-grey-bot"></div>
        
        

	</div>




</format:page>
