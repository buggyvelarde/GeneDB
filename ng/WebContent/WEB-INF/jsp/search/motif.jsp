<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Motif Search" />
<format:page>
<br>

<br>
<div id="geneDetails">
    <format:genePageSection id="motifSearch" className="whiteBox">
        <form:form commandName="query" action="${actionName}" method="GET">
            <table>
                <tr>
                    <td width=180>
                        <br><b>Motif Search:&nbsp;</b><br>
                        <h2><a style="cursor:pointer;" id='help'>Help!</a></h2> 
                    </td>
                    <td width=180>
                        <b>Organism:</b>
                        <br><db:simpleselect selection="${taxonNodeName}" />
                        <br><font color="red"><form:errors path="taxons" /></font>
                    </td>
                    <td width=180>
                        <b>Motif:</b>
                        <br><form:input id="search" path="search"/>
                        <br><font color="red"><form:errors path="search" /></font>
                    </td>
                      <td>&nbsp;&nbsp;&nbsp;</td>
                    <td>
                        <br><input type="submit" value="Submit" />
                    </td>
                    
                 </tr>
                 <tr>
                    <td></td>
                    <td colspan=3><font color="red"><form:errors  /></td>
                    <td></td>
                </tr>
            </table>

        </form:form>
    </format:genePageSection>
    
    <format:test-for-no-results />
    
</div>



<br><query:results />

<div id='help-text' style="display:none;">
 <P>Search examples:</P>
              <TABLE BORDER="0" WIDTH="100%">
            <TR>
                 <TD><TT>CADR</TT></TD>
                 <TD>will find</TD>
                 <TD>CADR</TD>
               </TR>
               <TR>
                 <TD><TT>CA[DE]R</TT></TD>
                 <TD>will find</TD>
                 <TD>CADR/CAER</TD>
               </TR>
               <TR>
                 <TD><TT>CA...R</TT></TD>
                 <TD> will find</TD>
                 <TD>CAXXXR</TD>
               </TR>
               <TR>
                 <TD><TT>CA.+R </TT></TD>
                 <TD>will find </TD>
                 <TD>CA(any number of one or more amino acids)R</TD>
               </TR>
               <TR>
                 <TD><TT>^ME </TT></TD>
                 <TD>will find </TD>
                 <TD>proteins beginning with ME</TD>
               </TR>
               <TR>
                 <TD><TT>LAA$</TT></TD>
                 <TD>will find </TD>
                 <TD>proteins terminating LAA</TD>
               </TR>
               <TR>
                 <TD><TT>^.{1,20}MCA</TT></TD>
                 <TD>will find </TD>
                 <TD>proteins with MCA in the first 20 amino acids</TD>
               </TR>
             </TABLE>
           </ul>
         </p>
         <P>Amino acid group codes (<b>Please note these options do not work in combination with square brackets</b>):</P>
         <TABLE BORDER="0" WIDTH="100%">
         <TR>
            <TH align="left">AA group</TH>
            <TH align="left">Code</TH>
            <TH align="left">Amino acids</TH>
         </TR>
         <TR>
                 <TD>acidic </TD>
                 <TD><B>0</B> </TD>
                 <TD><TT>DE</TT></TD>
         </TR>
         <TR>
                 <TD>alcohol </TD>
                 <TD><B>1</B> </TD>
                 <TD><TT>ST</TT></TD>
         </TR>
         <TR>
                 <TD>aliphatic </TD>
                 <TD><B>2</B> </TD>
                 <TD><TT>AGILV</TT></TD>
         </TR>
         <TR>
                 <TD>aromatic </TD>
                 <TD><B>3</B> </TD>
                 <TD><TT>FHWY</TT></TD>
         </TR>
         <TR>
                 <TD>basic </TD>
                 <TD><B>4</B> </TD>
                 <TD><TT>KRH</TT></TD>
         </TR>
         <TR>
                 <TD>charged </TD>
                 <TD><B>5</B> </TD>
                 <TD><TT>DEHKR</TT></TD>
         </TR>
         <TR>
                 <TD>hydrophobic </TD>
                 <TD><B>6</B> </TD>
                 <TD><TT>AVILMFYW</TT></TD>
         </TR>
         <TR>
                 <TD>hydrophilic </TD>
                 <TD><B>7</B> </TD>
                 <TD><TT>KRHDENQ</TT></TD>
         </TR>
         <TR>
                 <TD>polar </TD>
                 <TD><B>8</B> </TD>
                 <TD><TT>CDEHKNQRST</TT></TD>
         </TR>
         <TR>
                 <TD>small </TD>
                 <TD><B>9</B> </TD>
                 <TD><TT>ACDGNPSTV</TT></TD>
         </TR>
         <TR>
                 <TD>tiny </TD>
                 <TD><B>B</B> </TD>
                 <TD><TT>AGS</TT></TD>
         </TR>
         <TR>
                 <TD>turnlike </TD>
                 <TD><B>Z</B> </TD>
                 <TD><TT>ACDEGHKNQRST</TT></TD>
         </TR>
         </TABLE>
</div>

<script>
$(function() {
    $("#help").click(function(e) {
        $("#help-text").dialog({ width: 700, height: 530 , title :  "Help with Motif Searches" });
    });
    
});
</script>

</format:page>
