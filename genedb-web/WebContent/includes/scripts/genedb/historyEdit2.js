
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

function tabOutputChosen() {
	$(".tabOutput").show();
	$(".noTabOutput").hide();
	$("selectFieldTitle").text("Select fields for columns");
}

function htmlOutputChosen() {
	$(".htmlOutput").show();
	$(".noHtmlOutput").hide();
	$("selectFieldTitle").text("Select fields for columns");
}

function fastaOutputChosen() {
	$(".sequenceOutput").show();
	$(".noSequenceOutput").hide();
	$("selectFieldTitle").text("Select fields for FASTA headers");
}

