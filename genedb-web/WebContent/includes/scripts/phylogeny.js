var tops;
var topItem = document.getElementById("topItems");
tops = topItem.getAttribute("value").split(',');
var items = document.getElementById("itemsLength");
var ilength = items.getAttribute("value");

function hideAllMenus() {
	for(i=0;i<ilength;i++)	{
		if (! inTops(i)) {
			var elementID = "mi_0_" + i;
			var element = document.getElementById(elementID);
			element.style.visibility = "hidden";
		}
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

function mouseout() {
	this.hide_timer = setTimeout('hideAllMenus();',200);			
}
