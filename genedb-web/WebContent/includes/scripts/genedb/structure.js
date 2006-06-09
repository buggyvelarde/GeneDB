 
function toggleSection(section, path) {
	div = $("sect_"+section+"_content");
	img = $("sect_"+section+"_image");
	visible = Element.visible(div);
	//Element.toggle(div);
	if (visible) {
		Effect.Fade(div);
	} else {
		Effect.Appear(div);
	}

	img.src = getImage(path, !visible);
}

function getImage(path, expanded) {
	img = path + "/includes/images/tri.gif";
	if (expanded) {
		img = path + "/includes/images/tridown.gif";
	}
	return img;
}