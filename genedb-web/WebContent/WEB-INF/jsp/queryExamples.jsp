<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header title="GeneDB" name="GeneDB" >Welcome to the GeneDB website<br />Version 4.0</format:header>

<h3>Initial state</h3>
<p><query:question />


<h3>Later on...</h3>


<query:box>
    <query:box nest="true">
        <query:box>
            <query:question />
            <query:op op="INTERSECT" />
            <query:question />
        </query:box>
        <query:op op="UNION" nest="true"/>
        <query:question />
    </query:box>
    <query:op op="INTERSECT" />
    <query:box nest="true">
        <query:question />
        <query:op op="SUBTRACT" nest="true"/>
        <query:question />
    </query:box>
</query:box>

<format:footer />
