<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Search Results" />
<format:page>
<br>

<div id="col-2-1">
<h1>Results <span class="number">1</span> to <span class="number">30</span> of <span class="number">33,499</span> results shown.</h1>
<div class="full-light-grey-top"></div>
<div class="main-light-grey">
Previous &nbsp; <a href="">1</a>, <a href="">2</a>, <a href="">3</a>, <a href="">4</a>, <a href="">5</a>, <a href="">6</a>, <a href="">7</a>, <a href="">8</a> &nbsp; <a href="">Next</a>

</div>
<div class="full-light-grey-bot"></div>
<br />
<div class="full-blue-top"></div>

<div id="geneResultsPanel">
    <display:table name="items" id="row" pagesize="30" requestURI="/History/View" class="simple" sort="external" cellspacing="0" cellpadding="4" partialList="true" size="${fn:length(items)}">
        <display:column title="No." style="width: 100px;">
            ${row_rowNum}
        </display:column>
        <display:column title="History Type" style="width: 150px;">
            ${row.historyType}
        </display:column>
        <display:column title="Name" style="width: 150px;">
            ${row.name}
        </display:column>
        <display:column title="No. of Results" style="width: 150px;">
            ${row.numberItems}
        </display:column>
        <display:column title="Download" style="width: 150px;">
            <a href="/Download/${row_rowNum}">Download</a>
        </display:column>
        <display:column title="View/Edit" style="width: 150px;">
            <a href="/History/${row_rowNum}">Edit</a>
        </display:column>
        <display:column title="Remove" style="width: 150px;">
            <form:form method="DELETE" action="/History?historyItem=${row_rowNum}"><input type="submit" value="Remove"/></form:form>
        </display:column>
    </display:table>
</div>

</format:page>
