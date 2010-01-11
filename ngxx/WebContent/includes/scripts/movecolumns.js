   
      //----------------------------------------------//
   
      //  Created by: Romulo do Nascimento Ferreira //
   
      //  Email: romulo.nf@gmail.com                    //
   
      //----------------------------------------------//
   
       
   
      // NOTICE: This code may be use dto any purpose without further
   
      // permission from the author. You may remove this notice from the
  
      // final code, however its appreciated if you keep the author name/email.
  
      // Email me if theres something needing improvement
  
       
  
      //set the id of the table that is gonna have the
  
      //moving column function in the tabelaDrag variable
  
      document.onmouseup = soltar;
  
      var drag = false;
  
       
  
      window.onload = init
  
       
  
      function init() {
 
      tabelaDrag = document.getElementById("tmp");
  
      rows = tabelaDrag.getElementsByTagName("TR");
  
      data = tabelaDrag.getElementsByTagName("TD");
  
      firstRow = tabelaDrag.rows[1]
  
      maxColumns = firstRow.cells.length
  
       
  
        tabelaDrag.onselectstart = function () { return false; }
  
        tabelaDrag.onmousedown = function () { return false; }
  
       
  
          for (x=10; x<data.length;x++) {
  
              arrastar(data[x])
  
              data[x].onmouseover = pintar
  
              data[x].onmouseout = pintar
  
          }
  
      }
  
       
  
      function captureColumn(obj) {
  
      column = obj.cellIndex
  
      return column
  
      }
  

      function hideColumn() {

	  var stl='none'

	  var tbl  = document.getElementById('tmp');
	  var rows = tbl.getElementsByTagName('tr');

	  for (var row=0; row<rows.length;row++) {
	      var cels = rows[row].getElementsByTagName('td')
	      cels[9].style.display=stl;
	  }
          
      }
       
      function dropDown() {
	  var sel = document.getElementById('addColumns');
	  var column = sel.options[sel.selectedIndex].value;

	  var tbl  = document.getElementById('tmp');
	  var cels = tbl.getElementsByTagName('td');

	  //for (var row=0; row<rows.length;row++) {
	  //    var cels = rows[row].getElementsByName(column)
	  //    cels[0].className="";
	  // }
	  
	  for (var row=0;row<cels.length;row++) {
	      if(cels[row].id==column) {
		  cels[row].className="";
	      }
	  }
	  
      }

      function hideMe(e) {
	 var tbl  = document.getElementById('tmp');
	  var rows = tbl.getElementsByTagName('tr');
	  targ = e.target;
	  id = targ.parentNode.cellIndex;
	  for (var row=0; row<rows.length;row++) {
	      var cels = rows[row].getElementsByTagName('td')
	      cels[id].className="hide";
	  } 

	 
     }

      function orderTd (obj) {
  
      destino = obj.cellIndex
  
       
  
      if (destino == null) return
  
      if (column == destino) return
  
       
  
          for (x=0; x<rows.length; x++) {
  
          tds = rows[x].cells
  
          var celula = rows[x].removeChild(tds[column])
  
              if (destino >= maxColumns || destino + 1 >= maxColumns) {
  
              rows[x].appendChild(celula)
  
              }
  
              else {
  
              rows[x].insertBefore(celula, tds[destino])
  
              }
  
          }
  
      }
  
       
  
      function soltar(e){
  
          if (!e) e=window.event
  
          if (e.target) targ = e.target
  
          else if (e.srcElement) targ=e.srcElement
  
          orderTd(targ)
  
          drag = false
  
         
  
          for(x=0; x<rows.length; x++) {
  
              for (y=0; y<rows[x].cells.length; y++) {
	      
		  if(rows[x].cells[y].className!="hide") {
		      rows[x].cells[y].className="";
		  }
              }
  
          }
  
      }
  
       
  
      function arrastar(obj){
  
          if(!obj) return;
  
          obj.onmousedown = function(ev){
  
              columnAtual = this.cellIndex
  
                  for (x=0; x<rows.length; x++) {
  
                  rows[x].cells[this.cellIndex].className="selecionado"
  
                  }
  
              drag = true
  
              captureColumn(this);
  
              return false;
  
          }
  
      }
  
       
  
      function pintar(e) {
  
      if (!e) e=window.event
  
      ev = e.type
  
       
  
          if (ev == "mouseover") {
  
              if (drag) {
  
                  for (x=0; x<rows.length; x++) {
  
                      if (this.className !="selecionado") {
  
                      rows[x].cells[this.cellIndex].className="hover"
  
                      }
  
                  }
  
              }
 
          }
  
         
  
          else if (ev == "mouseout") {
 
              for (x=0; x<rows.length; x++) {
 
                  if (this.className !="selecionado") {
 
                  rows[x].cells[this.cellIndex].className=""
 
                  }
 
              }
 
          }
 
      }