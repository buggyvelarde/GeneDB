var dom = YAHOO.util.Dom;
var topItem;
var tops;
var items;
var ilength;

function init() {
	
	topItem = dom.get("topItems")
	tops = topItem.getAttribute("value").split(',');
	items = document.getElementById("itemsLength");
	ilength = items.getAttribute("value");
}

function hideAllMenus() {
	for(i=0;i<ilength;i++)	{
		//if (! inTops(i)) {
			var elementID = "mi_0_" + i;
			var element = document.getElementById(elementID);
			element.style.display = 'none';
			element.setAttribute('direction','');
		//}
	}
	adjustCoordinates();
}

function inTops(i) {
	for (j=0;j<=tops.length;j++) {
		if (i == tops[j]) {
			return true;
		} 
	}
	return false;
}

function boxclicked(id) {
	var active_element = document.getElementById("check_" + id);
	var curr_item;

	var active = active_element.getAttribute("name").split('_');
	for(i=0;i<ilength;i++)	{
		curr_item = document.getElementById("check_" + i);
		var curr = curr_item.getAttribute("name").split('_');
		var visib = true;
		for(j=0;j<active.length;j++) {
			if(active[j] != curr[j]) {
				visib = false;
				break;
			}
		}
		if(visib && (curr_item != active_element)) {
	        curr_item.disabled = active_element.checked;
	        if(curr_item.checked) {
	        	curr_item.checked = false;
	        }
		}
	}
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
		if(visib && (curr_item.style.display == 'none')) {
	        curr_item.style.display = '';
	        //alert(curr_item.attributes);
	        var rect = getCoordinates(curr_item);
	        var right = rect[0] + 154;
	        if(right > window.innerWidth || (active_item.getAttribute('direction') == 'left')
	        		&& (!inTops(i)) && (curr_item.getAttribute('direction') != 'left')) {
	        	curr_item.style.left = curr_item.offsetLeft - (154*2*curr_item.style.zIndex) + 'px';
	        	active_item.setAttribute('direction','left');
	        	curr_item.setAttribute('direction','left');
	        }
		} else if (visib){
			curr_item.style.display = '';
		} else {
			curr_item.style.display = 'none';
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
			curr_item.style.display = '';
		}	
	}

	function hidefirst() {
		this.timer = setTimeout('hideAllMenus();',200);
	}
	

function mouseout() {
	this.hide_timer = setTimeout('hideAllMenus();',200);			
}

var ilength;

function getCoordinates(obj) {
	var curleft = 0; 
	var curtop = 0;
	if (obj.offsetParent) {
		curleft = obj.offsetLeft
		curtop = obj.offsetTop
		while (obj = obj.offsetParent) {
			curleft += obj.offsetLeft
			curtop += obj.offsetTop
		}
	}
	return [curleft,curtop];
}

function adjustCoordinates() {
	var items = document.getElementById("itemsLength");
	ilength = items.getAttribute("value");

	for (var i=0; i< ilength; i++) {
		var elementID = "mi_0_" + i;
		var element = document.getElementById(elementID);
		var depth = element.getAttribute("name").split('_');
		element.style.left = ( depth.length * 154 ) + 1 + 'px';
		var top = -1;
		for (j=0;j<depth.length;j++) {
			top = top + depth[j] * 29;
		}
			element.style.top = top + 'px';
	}
}

function mouseclick(id) {
	var selected = document.getElementById("organism");
	selected.value = '';
	for (var i=0; i< this.ilength; i++) {
		var element = document.getElementById("check_" + i);
		if(element.checked) {
			if (selected.value == '') {
				selected.value =  element.value;
			} else {	
				selected.value = selected.value + ',' + element.value;
			}
		}	
	}
}

function resetall() {
	for (var i=0; i< ilength; i++) {
		this.checked[i] = false;
		var element = document.getElementById("menu_" + i);
		element.checked = false;
	}
	var selected = document.getElementById("selected");
	selected.value = '';
	var url = document.URL.split('?');
	window.location = url[0];
}

function check() {
	var selected = document.getElementById("selected");
	if (selected.value == '') {
		selected.style.border = "solid 1px red";
		alert ("Please select an organism from the tree");
		return false;
	} 
	var org = document.getElementById("textInput");
	if (org.value == '') {
		org.style.border = "solid 1px red";
		alert ("Please enter the search term in the input box");
		return false;
	} 
	
	
}