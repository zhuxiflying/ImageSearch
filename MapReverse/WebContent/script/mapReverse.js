/**
 * This javascripts render Index.html with image search results.
 */

var imageData;

d3.json("NateSilver_Example/NateSilver_Example2.json").then(function(data) {
	imageData = data;
	loadIconImages();
});

function loadIconImages() {

	var gallery_container = d3.select('.map-gallery');
	
	//append div for icon images
	var icon_maps = gallery_container.selectAll('.icon-image').data(imageData).enter().append('div').attr('class', 'icon-image');

	//append image element for div container
	gallery_container.selectAll('.icon-image').append('img').attr('class', 'icon-image').attr('src', function(d) { return d.Image_url; });
	
}