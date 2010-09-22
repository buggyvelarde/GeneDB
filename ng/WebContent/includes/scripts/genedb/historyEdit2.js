
function checkAll() {
	$("#col-2-1 input:checkbox").attr('checked', true);
}

function uncheckAll() {
	$("#col-2-1 input:checkbox").attr('checked', false);
}

function toggle() {
	$("#col-2-1 input:checkbox").each(function () {
		this.checked = !this.checked;
	});
}




$(function(){
	
	$("input[name='sequenceType']").change(function (event) {
		
        var value = $(event.target).val();
        
        // actually disabling the form elements results in their value not being sent
        if (value == "INTERGENIC_5") {
        	$('#prime5').show();
        	$('#prime3').hide();
        	
        } else if (value == "INTERGENIC_3") {
        	
        	$('#prime5').hide();
        	$('#prime3').show();
        	
        } else if (value == "INTERGENIC_3and5") {
        	
        	$('#prime5').show();
        	$('#prime3').show();
        	
        } else {
        	
        	$('#prime5').hide();
        	$('#prime3').hide();
        	        	
        }
    });
	
	$("input[name='cust_format']").change(function (event) {
		
		var value = $(event.target).val();
		
		if (value == "TAB") {
			$(".tabOutput").show();
			$(".noTabOutput").hide();
			$("#selectFieldTitle").html("Select fields for TAB columns");
		
		} else if ((value == "HTML") ||  (value == "EXCEL")) { // we can treat HTML and EXCEL to have equivalent options for now
			$(".htmlOutput").show();
			$(".noHtmlOutput").hide();
			$("#selectFieldTitle").html("Select fields for HTML columns");
		
		} else if (value == "FASTA") {
			$(".sequenceOutput").show();
			$(".noSequenceOutput").hide();
			$("#selectFieldTitle").html("Select fields for FASTA headers");
			
		}
		
	});
	
	$("input[name='sequenceType']:first").change();
	$("input[name='cust_format']:first").change();
	
	
});
