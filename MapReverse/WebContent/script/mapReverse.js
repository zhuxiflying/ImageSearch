/**
 * This javascripts render Index.html with image search result.
 */

var imageData;

d3.json("NateSilver_Example/NateSilver_Example2.json").then(function(data) {
	imageData = data;
	loadIconImages();
});

function loadIconImages() {

	var gallery = d3.select('.map-gallery');
	
	var container = gallery.selectAll('icon-image').data(imageData, function(d) {
		return d.id;
	});

	container.enter().append('div').attr('class', 'icon-image')

	container.exit().remove();

}