function ArrayList()
{
  this.array = new Array();
  
  this.add = function(obj){
    this.array[this.array.length] = obj;
  }
  
  this.iterator = function (){
    return new Iterator(this)
  }
  
  this.length = function (){
    return this.array.length;
  }
  
  this.get = function (index){
    return this.array[index];
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
  }
  
  this.remove = function (obj) {
	  for (var i=0;i<this.array.length;i++) {
		  if(this.array[i] == obj) {
			  for(var j=i; j<this.array.length)
		  }
	  }
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
