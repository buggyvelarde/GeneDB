var tops;
var topItem = document.getElementById("topItems");
tops = topItem.getAttribute("value").split(',');
var items = document.getElementById("itemsLength");
var ilength = items.getAttribute("value");

function hideAllMenus() {
	for(i=0;i<ilength;i++)	{
		//if (! inTops(i)) {
			var elementID = "mi_0_" + i;
			var element = document.getElementById(elementID);
			element.style.visibility = "hidden";
		//}
	}
}

function inTops(i) {
	for (j=0;j<=tops.length;j++) {
		if (i == tops[j]) {
			return true;
		} 
	}
	return false;
}

function mouseover(id){
	this.active_item = document.getElementById("mi_0_" + id);
	clearTimeout(this.hide_timer);
	//doSomething(this.active_item);
	var curr_item, visib;
	for(i=0;i<ilength;i++)	{
		curr_item = document.getElementById("mi_0_" + i);
		var temp = curr_item.getAttribute("name").split('_');
		var curr = temp.slice(0,curr_item.style.zIndex).join('_');
		temp = active_item.getAttribute("name").split('_');
        var active = temp.slice(0, curr_item.style.zIndex).join('_');
        visib = (curr == active);
		if(visib) {
	        curr_item.style.visibility = 'visible';
		} else {
			curr_item.style.visibility = 'hidden';
		}
	}
}

function showfirst() {
		var tops;
		var topItem = document.getElementById("topItems");
		tops = topItem.getAttribute("value").split(',');
		clearTimeout(this.timer);
		for (i=0;i<tops.length;i++) {
			var curr_item = document.getElementById("mi_0_" + tops[i]);
			curr_item.style.visibility = 'visible';
		}	
	}

	function hidefirst() {
		this.timer = setTimeout('hideAllMenus();',200);
	}
	
/*
function doSomething(obj) {
		var name = obj.getAttribute("name");
		var curleft = curtop = 0;
		if (obj.offsetParent) {
			curleft = obj.offsetLeft
			curtop = obj.offsetTop
			while (obj = obj.offsetParent) {
				curleft += obj.offsetLeft
				curtop += obj.offsetTop
			}
		}
		alert("left for " + name + " is " + curleft);
		alert("top for " + name + " is " + curtop);
}*/
function mouseout() {
	this.hide_timer = setTimeout('hideAllMenus();',200);			
}
