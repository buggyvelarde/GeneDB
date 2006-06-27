<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>

<format:header name="PostgreSQL Lock Analysis" />

<style>
<!--
td.true {color: green}
td.false {color: red}
-->
</style>


<table width="100%" border="1">
	<tr>
	<th>Proc. id</th>
	<th>User</th>
	<th>Current query</th>
	<th>Backend start</th>
	<th>Relation</th>
	<th>Rel. name</th>
	<th>Transaction id</th>
	<th>Mode</th>
	</tr>
	<c:forEach items="${rows}" var="row">
		<tr>
			<td class="${row['granted']}">${row['procpid']}</td>
		    <td class="${row['granted']}">${row['usename']}</td>
		    <td class="${row['granted']}">${row["current_query"]}</td>
			<td class="${row["granted"]}">${row["backend_start"]}</td>
			<td class="${row["granted"]}">${row["relation"]}</td>
			<td class="${row["granted"]}">${row["relname"]}</td>
			<td class="${row["granted"]}">${row["transactionid"]}</td>
			<td class="${row["granted"]}">${row["mode"]}</td>
		</tr>
	</c:forEach>
</table>

<format:footer />
