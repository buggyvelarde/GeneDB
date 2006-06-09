<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>

<format:header name="Cv Page" />

<dl>
	<dt><b>Name:</b></dt>
	<dd>${cv.name}</dd>

	<dt><b>Definition:</b></dt>
	<dd>${cv.definition}</dd>

	<dt><b>Id:</b></dt>
	<dd>${cv.cvId}</dd>
</dl>

<p><a href="./CvTermByCvName?cvName=${cv.name}&cvTermName=%">List all CvTerms</a> in
this controlled vocabulary</p>


<format:footer />
