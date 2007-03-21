<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header name="Welcome to the GeneDB website Next Generation">
<link rel="stylesheet" href="<c:url value="/"/>includes/style/wtsi.css" type="text/css"/>
<link rel="stylesheet" href="<c:url value="/"/>includes/style/jimmac.css" type="text/css"/>
</format:header>
<table width="100%">
<tr>
	<td width="20%">
		<div class="fieldset">
		<div class="legend">24th Nov 2006</div>
		<div class="content">
			<table>
				<tr>
					<td>Gene Name: </td>
					<td><input type="text" size="12"/></td>
				</tr>
				<tr>
					<td><input type="submit" value="submit"/></td>
					<td><br></td>
				</tr>
			</table>
		</div>
		</div>
	</td>
	<td width="60%" align="center">
		<div class="fieldset" align="center">
		<div class="legend">Information</div>
		<div class="content">
		<table width="100%" align="center" >
			<tr>
				<td>The GeneDB project is a core part of the Sanger Institute Pathogen
		Sequencing Unit's (PSU) activities. Its primary goals are:<ul><li>to provide reliable storage of, and access to the latest sequence data and
		annotation/curation for the whole range of organisms sequenced by the PSU.</li>
		 <li>to develop the website and other tools to aid the community in accessing
		and obtaining the maximum value from this data.</li>
		</ul></td>
			</tr>
			<tr>
				<td>This is a quick set of example links</td>
			</tr>
		</table>
		<div class="fieldset" align="center">
		<div class="legend">Navigation</div>
		<div class="content">
		<table width="100%" align="center">
			<tr>
				<td width="50%">
					<a href="<c:url value="/Organism"/>">Organism List</a> (Partial)
				</td>
				<td width="50%">
					<a href="./Genome/Trypanasoma_brucei_brucei/current">Common URL</a> (Partial)
				</td>
			</tr>
			<tr>
				<td width="50%">
					<a href="<c:url value="/examples/JSMenuTest.jsp"/>">Query drop-down</a> (OK)
				</td>
				<td width="50%">
					<a href="./Admin/LockExaminer">Lock examiner</a> (OK)
				</td>
			</tr>
			<tr>
				<td width="50%">	
					<a href="./Search/FeatureByName?name=Tb927.2.4760">New tbrucei example</a>
				</td>
				<td width="50%">	
					<a href="./Search/FeatureByName?name=Tb927.2.4760">New pombe example</a>
				</td>
			</tr>
			<tr>
				<td width="50%">	
					<a href="./Search/DummyGeneFeature">New off-line example</a>
				</td>
				<td>
					<a href="<c:url value="/Search/BooleanQuery?taxId=12345"/>">Boolean query page</a> (Partial)
				</td>
			</tr>
			<tr>
				<td width="50%">			
					<a href="<c:url value="/examples/pfamTest.jsp"/>">Pfam auto-complete</a> (Partial)
				</td>
				<td width="50%">
					<a href="<c:url value="/dwr/test/PfamLookup"/>">DWR Pfam page</a> (OK)
				</td>
			</tr>
			<tr>
				<td width="50%">
					<a href="./Search/FindCvByName"/>CV Browser (check)</a>
				</td>
				<td width="50%">
					<a href="./Search/FeatureByName?name=PF08_0098">New malaria example</a>
				</td>
			</tr>
			<tr>
				<td width="50%">
					<a href="./Search/FeatureByName?name=Tb927.2.4760">New pombe alt-splicing example</a>
				</td>
				<td width="50%">
				</td>
			</tr>
		</table>
		</div>
		</div>
		<div class="fieldset">
		<div class="legend">Pathogen Sequencing - Projects</div>
		<div class="content">
		<table width="100%" align="center">
		<tbody>
		<tr>
		<td width="50%">
		<a href="http://www.sanger.ac.uk/Projects/Microbes/">Bacteria</a>:
		<a href="http://www.sanger.ac.uk/Projects/Microbes/"><img class="img-nb" src="<c:url value="/includes/images/N_meningitidis_55.gif"/>" alt="Bacteria" title="Bacteria" style="height: 55px; width: 55px; float: right;"></a>
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		<td width="50%">
		<a href="http://www.sanger.ac.uk/Projects/Protozoa/">Protozoa</a>:
		<a href="http://www.sanger.ac.uk/Projects/Protozoa/"><img class="img-nb" src="<c:url value="/includes/images/protozoa_55.jpg"/>" alt="Protozoa" title="Protozoa" style="height: 55px; width: 55px; float: right;"></a>
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		</tr>
		<tr>
		<td width="50%">
		<a href="http://www.sanger.ac.uk/Projects/Fungi/">Fungi</a>:
		<a href="http://www.sanger.ac.uk/Projects/Fungi/"><img class="img-nb" src="<c:url value="/includes/images/fungi_55.jpg"/>" alt="Fungi" title="Fungi" style="height: 55px; width: 55px; float: right;"></a>
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		<td width="50%">
		<a href="http://www.sanger.ac.uk/Projects/Helminths/">Helminths</a>:
		<a href="http://www.sanger.ac.uk/Projects/Helminths/"><img class="img-nb" src="<c:url value="/includes/images/helminths_55.jpg"/>" alt="Helminths" title="Helminths" style="height: 55px; width: 55px; float: right;"></a>
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		</tr>
		<tr>
		<td width="50%">
		<a href="http://www.sanger.ac.uk/Projects/Plasmids/">Plasmids</a>:
		<a href="http://www.sanger.ac.uk/Projects/Plasmids/"><img class="img-nb" src="<c:url value="/includes/images/plasmid_55.jpg"/>" alt="Plasmids" title="Plasmids" style="height: 55px; width: 55px; float: right;"></a>
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		<td width="50%"><!--run from /bin-offline/Website/lists/-->
		<a href="http://www.sanger.ac.uk/Projects/Vectors/">Vectors</a>:
		<a href="http://www.sanger.ac.uk/Projects/Vectors/"><img class="img-nb" src="<c:url value="/includes/images/vectors_55.gif"/>" alt="Vectors" title="Vectors" style="height: 55px; width: 55px; float: right;"></a>
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		</tr>
		<tr>
		<td width="50%">
		<a href="http://www.sanger.ac.uk/Projects/Phage/">Bacteriophage</a>:
		<a href="http://www.sanger.ac.uk/Projects/Phage/"><img class="img-nb" src="<c:url value="/includes/images/bacteriophage_55.jpg"/>" alt="Bacteriophage" title="Bacteriophage" style="height: 55px; width: 55px; float: right;"></a>
		<p>All data from these projects are immediately and freely
		available.</p>
		</td>
		<td width="50%">&nbsp;</td>
		</tr>
		</tbody>
		</table>
		</div>
		</div>
		</div>
		</div>
	</td>
	<td width="20%">
		<div class="fieldset">
		<div class="legend">News</div>
		<div class="content">
		<table>
			<tr>
				<td>
					<div align="center"> 
						<marquee scrollamount="2" direction="up" loop="true" width="100%"> <center> 
						<font color="#660000" size="+1"> <a href="www.google.com"> News<br> News News<br> News News<br> </a></font> </center> <br><br>
						<font color="#660000" size="+1"> <a href="www.sanger.ac.uk"> News1<br> News1 News1<br> News1 News1<br> </a></font> </center> 
						</marquee>
					</div>
				</td>
			</tr>
		</table>
		</div>
		</div>
	</td>
	</tr>
	</table>
<format:footer />