/**
 * http://usejsdoc.org/
 */

var imageData;

d3.json("NateSilver_Example/NateSilver_Example2.json").then(function(data) {
	imageData = data;
	loadIconImages();
	});


function loadIconImages()
{
	var iconMaps = document.getElementById("legend");
	console.log(iconMaps);
	for (var i = 0; i < 22; i++) {
		var div_node = document.createElement("div");
		div_node.setAttribute("class", "icon-image");
		var img = document.createElement("IMG");
		img.src = imageData[i].Image_url;
		div_node.appendChild(img);	
		iconMaps.appendChild(div_node);	
	}

}