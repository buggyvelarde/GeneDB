<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<html>
<head>
<link rel="stylesheet" href="<c:url value="/"/>includes/style/wtsi.css" type="text/css"/>
<link rel="stylesheet" href="<c:url value="/"/>includes/style/jimmac.css" type="text/css"/>
<link rel="stylesheet" href="<c:url value="/"/>includes/style/test2.css" type="text/css"/>
<title>
	Welcome to GeneDB
</title>
</head>
<body style="background-color: #333333;">
<div align="center">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr>
		<td class="outline">
			<div id="header_outer">
				<div id="top_outer_left">
				</div>
				<div id="header">
					<br/>
					<h1 align="center">Welcome to the GeneDB website - NG</h1>
				</div>
			</div>
			<div id="left_outer">
		  			<div id="left_inner">
							<div class="moduletable">
								<h3>
									Recent News				
								</h3>
								<table width="100%" border="0" cellspacing="0" cellpadding="0" align="center">
									<tr>
										<td>
											
											Latest News can go here if wanted
											Latest News can go here if wanted
											Latest News can go here if wanted
											Latest News can go here if wanted
											Latest News can go here if wanted
											Latest News can go here if wanted
										</td>
									</tr>
								</table>
							</div>
		  					<div class="moduletable">
								<h3>
									Main Menu
								</h3>
								<table width="100%" border="0" cellpadding="0" cellspacing="0">
								<tr align="left"><td><a href="www.genedb.org" class="mainlevel" id="active_menu">Home</a></td></tr>
								<tr align="left"><td><a href="www.genedb.org" class="mainlevel">News</a></td></tr>
								<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >Links</a></td></tr>
								<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >Search</a></td></tr>
								<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >FAQs</a></td></tr>
								</table>		
							</div>
							<div class="moduletable">
								<h3>
									Information				
								</h3>
								<table width="100%" border="0" cellspacing="0" cellpadding="0" align="center">
									<tr>
										<td>
											<a href="www.genedb.org">Guide to GeneDB</a><br/>
											What is GeneDB, and what's in it?<br/>
											Navigating/Searching GeneDB<br/>
											Contacting Us/Feedback <br/>
											Privacy Policy<br/>
											Data Release Policy<br/>
										</td>
									</tr>
								</table>
							</div>
					</div>
			</div>
			<div id="content_outer">
					<div id="content_inner">
						<table border="0" cellpadding="0" cellspacing="0" width="100%" class="content_table">
							<tr>
								<td colspan="0" class="body_outer">
				  					 <table class="blog" cellpadding="0" cellspacing="0">
				  					 	<tr>
				  					 	<td valign="top">
				  					 		<div id="test">
				  					 			<table class="contentpaneopen">
													<tr>
														<td class="contentheading" width="100%">
															Welcome to Genedb	<br><br>								
														</td>
													</tr>
													<tr>
														<td><font style="font-size: 12px;">
															The GeneDB project is a core part of the Sanger Institute Pathogen
															Sequencing Unit's (PSU) activities. Its primary goals are:<br><br>
															<ul>
																<li>to provide reliable storage of, and access to the latest sequence data and
																	annotation/curation for the whole range of organisms sequenced by the PSU.<br><br></li>
																 <li>to develop the website and other tools to aid the community in accessing
																     and obtaining the maximum value from this data.<br><br></li>
															</ul>
														</font></td>
													<tr>
												</table>
												<span class="article_seperator">&nbsp;</span>
												<table class="contentpaneopen">
													<tr>
														<td class="contentheading" width="100%">
															This is a quick set of example links									
														</td>
													</tr>
													<tr>
														<td>
															<div>
																<table class="contentpaneopen">
																	<tr>
																		<td width="100%" style="text-align: left;">
																			<ul id="mainlevel-nav">
																				<li id="mainlevel-nav"><a href="<c:url value="/Organism"/>">Organism List</a></li>
																				<li id="mainlevel-nav"><a href="<c:url value="/examples/JSMenuTest.jsp"/>">Query drop-down</a></li>
																				<li id="mainlevel-nav"><a href="./Search/FeatureByName?name=Tb927.2.4760">New tbrucei example</a></li>
																				<li id="mainlevel-nav"><a href="./Search/DummyGeneFeature">New off-line example</a></li>
																				<li id="mainlevel-nav"><a href="<c:url value="/examples/pfamTest.jsp"/>">Pfam auto-complete</a></li>
																				<li id="mainlevel-nav"><a href="<c:url value="/dwr/test/PfamLookup"/>">DWR Pfam page</a></li>
																				<li id="mainlevel-nav"><a href="./Search/FeatureByName?name=PF08_0098">New malaria example</a></li>
																				<li id="mainlevel-nav"><a href="http://localhost:8080/genedb-web/Genome/Trypanasoma_brucei_brucei/current">Common URL</a></li>
																				<li id="mainlevel-nav"><a href="./Admin/LockExaminer">Lock examiner</a></li>
																				<li id="mainlevel-nav"><a href="./Search/FeatureByName?name=Tb927.2.4760">New pombe example</a></li>
																				<li id="mainlevel-nav"><a href="<c:url value="/Search/BooleanQuery?taxId=12345"/>">Boolean query page</a></li>
																				<li id="mainlevel-nav"><a href="./Search/FindCvByName"/>CV Browser (check)</a></li>
																				<li id="mainlevel-nav"><a href="./Search/FeatureByName?name=Tb927.2.4760">New pombe alt-splicing example</a></li>
																			</ul>
																		</td>
																	</tr>
																</table>															
															</div>
														</td>
													<tr>
												</table>
												<span class="article_seperator">&nbsp;</span>
												<table class="contentpaneopen" width="100%">
													<tr>
														<td class="contentheading" width="100%">
															Example Gene Search 
														</td>
													</tr>
													<tr>
														<td>
															<div style="width: 100%;">
															<div style="width: 26%; float: left;">
															<div class="Article">
																<h3><p>Searches</p></h3>
																<div class="ArticleBody">
																<table width="100%" align="center">
																	<tr align="center">
																			<td align="center">
																		
																		    <form action="/genedb/Dispatcher" method="GET" name="ohm1">
																		    <input type="hidden" name="formType" value="navBar">
																			<br>
																			  <b>Search for gene by ID/description in:</b>
																				<select name="organism">
																		
																			    <option value="All:*">All organisms
																			      <optgroup label="Fungi">
																				<option value="asp">A. fumigatus
																				<option value="cdubliniensis">C. dubliniensis
																				<option value="cerevisiae">S. cerevisiae
																				<option value="pombe">S. pombe
																			   </optgroup>
																			   <optgroup label="Protozoa">
																		      	 <option value="dicty">D. discoideum</option>
																		      	 <option value="ehistolytica">E. histolytica</option>
																		
																		      	 <option value="etenella">E. tenella</option>
																		      	 <option value="lbraziliensis">L. braziliensis</option>
																				 <option value="linfantum">L. infantum</option>
																			     <option value="leish">L. major</option>
																				 <option value="pberghei">P. berghei</option>
																				 <option value="pchabaudi">P. chabaudi</option>
																		
																				 <option value="malaria">P. falciparum</option>
																			     <option value="pknowlesi">P. knowlesi</option>
																				 <option value="annulata">T. annulata</option>
																				 <option value="tryp">T. brucei</option>
																				 <option value="tbrucei427">T. brucei strain 427</option>
																				 <option value="tcongolense">T. congolense</option>
																		
																				 <option value="tcruzi">T. cruzi</option>
																				 <option value="tgambiense">T. b. gambiense</option>
																				 <option value="tvivax">T. vivax</option>
																			   </optgroup>
																			   <optgroup label="Parasitic Helminths">
																		      	 <option value="smansoni">S. mansoni
																			   </optgroup>
																			   <optgroup label="Bacteria">
																		
																		      	        <option value="bronchi">B. bronchiseptica
																		      	        <option value="bfragilis">B. fragilis
																		      	        <option value="parapert">B. parapertussis
																		      	        <option value="pert">B. pertussis
																		      	        <option value="bpseudomallei">B. pseudomallei
																		      	        <option value="cabortus">C. abortus
																		      	        <option value="diphtheria">C. diphtheriae
																		      	        <option value="ecarot">E. carotovora
																		      	        <option value="rleguminosarum">R. leguminosarum
																		      	        <option value="saureusMRSA">S. aureus MRSA
																		      	        <option value="saureusMSSA">S. aureus MSSA
																		      	        <option value="scoelicolor">S. coelicolor
																		      	        <option value="styphi">S. typhi
																			      </optgroup>
																			      <optgroup label="Parasite Vectors">
																		
																		      	        <option value="glossina">G. morsitans
																			      </optgroup>
																			      <optgroup label="Viruses">
																		      	        <option value="ehuxleyi">E. huxleyi virus 86
																			      </optgroup>
																			      <optgroup label="Group options">
																				<option value="Yeast:pombe:cerevisiae:asp">Yeast
																				<option value="Kinetoplastids:tryp:tbrucei427:leish:linfantum">Kinetoplastids
																			      </optgroup>
																			  </select>
																			<br/><br/><b>ID: </b><input type="text" size="14" name="name">
																		
																		        <br>&nbsp;
																			<br><input type="checkbox" checked name="desc" value="yes">Include description in search
																			<br><input type="checkbox" name="wildcard" value="yes">Add wildcards to search term
																			<br>&nbsp;
																			<br><input type="submit" name="submit" value="Search">&nbsp&nbsp;&nbsp;<input type=reset>
																		</form>
																		</td>
																	</tr>
																</table>
																</div>
																<div class="ArticleFooter"><p></p></div>
															</div>
															</div>
															<div style="width: 28%; float: left;">
															<div class="Article">
															<h3><p>Sequence Searches</p></h3>
															<div class="ArticleBody">
																<br/>
																<table>
																	<tr>
																       <td align="center" width="33%"><br/><a href="/genedb/seqSearch.jsp">omniBLAST</a> (Multi-organism BLAST)
																		    <form action="/genedb/Dispatcher" method="GET" name="ohm2">
																		    <input type="hidden" name="formType" value="navBar">
															
																		        <br>&nbsp;
																		        <br><hr>
																		        <br>&nbsp;
																			<br><input type="submit" name="pages" value="Go To"><b>&nbsp;single organism BLAST:</b> <br><br>
																		 <select name='ohmr'
																		onChange="document.location.href=document.forms['ohm2'].ohmr.options[document.forms['ohm2'].ohmr.selectedIndex].value">
																		<option selected value='/'>Choose...</option>
																		<optgroup label="Fungi">
																		<option value='/genedb/asp/blast.jsp'>A. fumigatus</option>
																		<option value='/genedb/cdubliniensis/blast.jsp'>C. dubliniensis</option>
																		<option value='/genedb/cerevisiae/blast.jsp'>S. cerevisiae</option>
																		<option value='/genedb/pombe/blast.jsp'>S. pombe</option>
																		
																		</optgroup>
																		<optgroup label="Protozoa">
																		<option value='/genedb/dicty/blast.jsp'>D. discoideum</option>
																		<option value='/genedb/ehistolytica/blast.jsp'>E. histolytica</option>
																		<option value='/genedb/etenella/blast.jsp'>E. tenella</option>
																		<option value='/genedb/lbraziliensis/blast.jsp'>L. braziliensis</option>
																		<option value='/genedb/linfantum/blast.jsp'>L. infantum</option>
																		<option value='/genedb/leish/blast.jsp'>L. major</option>
																		<option value='/genedb/pberghei/blast.jsp'>P. berghei</option>
																		<option value='/genedb/pchabaudi/blast.jsp'>P. chabaudi</option>
																		
																		<option value='/genedb/malaria/blast.jsp'>P. falciparum</option>
																		<option value='/genedb/pknowlesi/blast.jsp'>P. knowlesi</option>
																		<option value='/genedb/annulata/blast.jsp'>T. annulata</option>
																		<option value='/genedb/tryp/blast.jsp'>T. brucei</option>
																		<option value='/genedb/tbrucei427/blast.jsp'>T. brucei strain 427</option>
																		<option value='/genedb/tcongolense/blast.jsp'>T. congolense</option>
																		<option value='/genedb/tcruzi/blast.jsp'>T. cruzi</option>
																		<option value='/genedb/tgambiense/blast.jsp'>T. b. gambiense</option>
																		<option value='/genedb/tvivax/blast.jsp'>T. vivax</option>
																		
																		</optgroup>
																		<optgroup label="Parasitic Helminths">
																		<option value='/genedb/smansoni/blast.jsp'>S. mansoni</option>
																		</optgroup>
																		<optgroup label="Bacteria">
																		<option value='/genedb/bronchi/blast.jsp'>B. bronchiseptica</option>
																		<option value='/genedb/bfragilis/blast.jsp'>B. fragilis</option>
																		<option value='/genedb/parapert/blast.jsp'>B. parapertussis</option>
																		<option value='/genedb/pert/blast.jsp'>B. pertussis</option>
																		<option value='/genedb/pseudomallei/blast.jsp'>B. pseudomallei</option>
																		<option value='/genedb/cabortus/blast.jsp'>C. abortus</option>
																		
																		<option value='/genedb/diphtheria/blast.jsp'>C. diphtheriae</option>
																		<option value="/genedb/ecarot/blast.jsp">E. carotovora</option>
																		<option value="/genedb/rleguminosarum/blast.jsp">R. leguminosarum</option>
																		<option value='/genedb/saureusMRSA/blast.jsp'>S. aureus MRSA</option>
																		<option value='/genedb/saureusMSSA/blast.jsp'>S. aureus MSSA</option>
																		<option value='/genedb/scoelicolor/blast.jsp'>S. coelicolor</option>
																		<option value='/genedb/styphi/blast.jsp'>S. typhi</option>
																		</optgroup>
																		<optgroup label="Parasite Vectors">
																		<option value='/genedb/glossina/blast.jsp'>G. morsitans</option>
																		
																		</optgroup>
																		<optgroup label="Viruses">
																		<option value='/genedb/ehuxleyi/blast.jsp'>E. huxleyi</option>
																		</optgroup>
																		</select>
																		<br/>&nbsp;
																		</form>
																		</td>
																	</tr>
																</table>
																</div>
																<div class="ArticleFooter"><p></p></div>
															</div>
															</div>
															<!-- Row 3 for going to datasets -->
															<div style="width: 45%; float: left;">
															<div class="Article">
															<h3><p>Datasets</p></h3>
															<div class="ArticleBody">
																<br/>
																<table>
																	<tr>
																		<td align="center">
																			<div>
																		    <form action="/genedb/Dispatcher" method="GET" name="ohm3">
																		    <input type="hidden" name="formType" value="navBar">
																		     <div style="float: left;margin: 5px;">   
																		        <b>Fungi</b>
																		        <br><input type="submit" name="fungiHomePage" value="Go To">
																		     	        <select name="fungiOrganism"
																		                 onChange="document.location.href=document.forms['ohm3'].fungiOrganism.options[document.forms['ohm3'].fungiOrganism.selectedIndex].value">
																		                  <option selected value='/'>Choose...</option>
																		                  <option value="/genedb/asp/">A. fumigatus
																		                  <option value="/genedb/cdubliniensis/">C. dubliniensis
																		                  <option value="/genedb/cerevisiae/">S. cerevisiae
																		                  <option value="/genedb/pombe/">S. pombe
																		                </select>
																			</div>
																		    <div style="float: left;margin: 5px;">
																				<b>Protozoa</b>
																		        <br><input type="submit" name="protozoaHomePage" value="Go To">
																		       	        <select name="protozoaOrganism"
																		                        onChange="document.location.href=document.forms['ohm3'].protozoaOrganism.options[document.forms['ohm3'].protozoaOrganism.selectedIndex].value">
																		                  <option selected value='/'>Choose...</option>
																		                  <option value="/genedb/dicty/">D. discoideum
																		                  <option value='/genedb/ehistolytica/'>E. histolytica
																		                  <option value='/genedb/etenella/'>E. tenella
																				          <option value='/genedb/lbraziliensis/'>L. braziliensis
																				          <option value='/genedb/linfantum/'>L. infantum
																		                  <option value="/genedb/leish/">L. major
																		                  <option value="/genedb/pberghei/">P. berghei
																		                  <option value="/genedb/pchabaudi/">P. chabaudi
																		                  <option value="/genedb/malaria/">P. falciparum
																		                  <option value='/genedb/pknowlesi'>P. knowlesi
																		                  <option value="/genedb/annulata/">T. annulata
																		                  <option value="/genedb/tryp/">T. brucei
																		                  <option value="/genedb/tbrucei427/">T. brucei strain 427
																		                  <option value="/genedb/tcongolense/">T. congolense
																		                  <option value="/genedb/tcruzi/">T. cruzi
																		                  <option value="/genedb/tgambiense/">T. b. gambiense
																		                  <option value="/genedb/tvivax/">T. vivax
																		                </select>
																				</div>
																		        <div style="float: left;margin: 5px;">
																		        <br><b>Parasitic Helminths</b>
																		        <br><input type="submit" name="helminthsHomePage" value="Go To">
																		       	        <select name="helminthsOrganism"
																		                 onChange="document.location.href=document.forms['ohm3'].helminthsOrganism.options[document.forms['ohm3'].helminthsOrganism.selectedIndex].value">
																		                  <option selected value='/'>Choose...</option>
																		                  <option value="/genedb/smansoni/">S. mansoni
																		                </select>
																				</div>
																				<div style="float: left;margin: 5px;">
																		        <br><b>Bacteria</b>
																		        <br><input type="submit" name="bacteriaHomePage" value="Go To">
																		       	        <select name="bacteriaOrganism"
																		                 onChange="document.location.href=document.forms['ohm3'].bacteriaOrganism.options[document.forms['ohm3'].bacteriaOrganism.selectedIndex].value">
																		                  <option selected value='/'>Choose...</option>
																		                  <option value='/genedb/bronchi/'>B. bronchiseptica</option>
																		                  <option value='/genedb/bfragilis/'>B. fragilis</option>
																		
																		                  <option value='/genedb/parapert/'>B. parapertussis</option>
																		                  <option value='/genedb/pert/'>B. pertussis</option>
																		                  <option value='/genedb/bpseudomallei'>B. pseudomallei
																		                  <option value='/genedb/cabortus/'>C. abortus</option>
																		                  <option value='/genedb/diphtheria/'>C. diphtheriae</option>
																		                  <option value='/genedb/ecarot/'>E. carotovora</option>
																		                  <option value='/genedb/rleguminosarum/'>R. leguminosarum</option>
																		
																		                  <option value='/genedb/saureusMRSA/'>S. aureus MRSA</option>
																		                  <option value='/genedb/saureusMSSA/'>S. aureus MSSA</option>
																		                  <option value='/genedb/scoelicolor/'>S. coelicolor</option>
																		                  <option value='/genedb/styphi/'>S. typhi</option>
																		                </select>
																				</div>
																				<div style="float: left;margin: 5px;">																		
																		        <br><b>Parasite Vectors</b>
																		        <br><input type="submit" name="parasiteVectorsHomePage" value="Go To">
																		       	        <select name="parasiteVectorsOrganism"
																		                 onChange="document.location.href=document.forms['ohm3'].parasiteVectorsOrganism.options[document.forms['ohm3'].parasiteVectorsOrganism.selectedIndex].value">
																		                  <option selected value='/'>Choose...</option>
																		                  <option value='/genedb/glossina/'>G. morsitans</option>
																		                </select>
																				</div>
																				<div style="float: left;margin: 5px;">
																		        <br><b>Viruses</b>
																		        <br><input type="submit" name="virusesHomePage" value="Go To">
																		       	        <select name="virusesOrganism"
																		onChange="document.location.href=document.forms['ohm3'].virusesOrganism.options[document.forms['ohm3'].virusesOrganism.selectedIndex].value">
																		                  <option selected value='/'>Choose...</option>
																		                  <option value='/genedb/ehuxleyi/'>E. huxleyi virus 86</option>
																		                </select>
																		         </div>
																					</form>
																			</div>
																			</td>
																																				
																	</tr>
																</table>
																</div>
																<div class="ArticleFooter"><p></p></div>
																</div>
															</div>
															</div>
														</td>
													</tr>
												</table>
												<span class="article_seperator">&nbsp;</span>
												<table class="contentpane">
													<tr>
														<td class="contentheading" width="100%">
															Other Information
														</td>
													</tr>
													<tr>
														<td>
															<br><br>GeneDB currently provides access to 37 genomes, from various stages of the sequencing curation pipeline, from early access to partial genomes with automatic annotation through to complete genomes with extensive manual curation.  (Details correct as of May 2006)<br><br>
														</td>
													</tr>
													<tr>
														<td>
															<br><br><b>Note:</b>This site (data and/or code) is updated approximately weekly. If things do not appear to be working - let us know, and we will try and fix them. Please see our help page before creating links to our site. If you have any suggestions or requests about the site please contact the technical team <br><br> 
														</td>
													</tr>
												</table>
										</div>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</div>
		</div>
	</td>
</tr>
<tr>
	<td width="100%">
		<br>
		<div>
			<img src="<c:url value="/"/>includes/images/purpleDot.gif" style="height: 3px; width: 100%">
		</div>
	</td>
</tr>
</table>
<table width="100%">
      <tr>
	<td width="50%" align="left"><font color="white">Hosted by the <a href="http://www.sanger.ac.uk/">Sanger Institute</a></font></td>

	<td width="50%" align="right"><p align="right"><a href="/genedb/feedback.jsp">Send us your comments on GeneDB</a></td>
      </tr>
</table>
</div>
<br><br>
</body>
</html>