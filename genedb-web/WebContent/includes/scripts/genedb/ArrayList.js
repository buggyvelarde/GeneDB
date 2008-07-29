function ArrayList() {
  this.array = new Array();
  
  this.add = function(obj){
    if(!this.contains(obj)) {
    	this.array[this.array.length] = obj;
    }
  }
  
  this.iterator = function (){
    return new Iterator(this)
  }
  
  this.length = function (){
    return this.array.length;
  }
  
  this.get = function (index){
	  if(index < this.array.length) {
		  return this.array[index];
	  }
  }
  
  this.getIndex = function (obj){
	  for (var i=0;i<this.array.length;i++) {
		  if(this.array[i] == obj) {
			  return i;
		  }
	  }
	  return -1;
  }
  
  this.addAll = function (obj) {
    if (obj instanceof Array) {
      for (var i=0;i<obj.length;i++) {
        this.add(obj[i]);
      }
    } else if (obj instanceof ArrayList) {
      for (var i=0;i<obj.length();i++) {
        this.add(obj.get(i));
      }
    }
  }
  
  this.contains = function (obj) {
	  for (var i=0;i<this.array.length;i++) {
		  if(this.array[i] == obj) {
			  return true;
		  }
	  }
	  return false;
  }
  
  this.remove = function (obj) {
	  var temp = new Array();
	  var count = 0;
	  for (var i=0;i<this.array.length;i++) {
		  if(this.array[i] != obj) {
			  temp[count] = this.array[i];
			  count++;
		  }
	  }
	  this.array = temp;
  }
  
  this.toString = function() {
	  var str = "";
	  for (var i=0;i<this.array.length;i++) {
		  str += this.array[i] + ",";
	  }
	  return str.replace(/(.*),/,"$1");
  }
  
  this.toStringWithSpace = function() {
	  var str = "";
	  for (var i=0;i<this.array.length;i++) {
		  str += this.array[i] + ", ";
	  }
	  return str.replace(/(.*),/,"$1");
  }
}


function Iterator (arrayList) {
  this.arrayList;
  this.index = 0;
  
  this.hasNext = function () {
    return this.index < this.arrayList.length();
  }
  
  this.next = function() {
    return this.arrayList.get(index++);
  }
}
