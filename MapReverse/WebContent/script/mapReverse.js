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
	gallery_container.selectAll('.icon-image').append('img').attr('src', function(d) { return d.Image_url; });
	
	
	gallery_container.selectAll('.icon-image').on("click",loadOriginalImage);
}

function loadOriginalImage(data){
	

	d3.select('.origin-image').append('img').attr('class','imageDiv');
	d3.select('.imageDiv').attr('src', data.OriginImage);
	

	var labels_keys = Object.keys(data.label);
	var labels_values = Object.values(data.label);
	var entities_keys = Object.keys(data.entity);
	var entities_values = Object.values(data.entity);
	
    var textArea = d3.select('.image-info');
    
    textArea.html("");
    
    textArea.append('p').text("Map Labels:").attr('class','text-title');
    
    textArea.selectAll('text-entry').data(labels_keys).enter().append('p').text(function(d) { return d; }).attr('class','text-entry');
    
    textArea.append('p').text("Map Entities:").attr('class','text-title');
    
    textArea.selectAll('text-entry2').data(entities_keys).enter().append('p').text(function(d) { return d; }).attr('class','text-entry2');
    
    textArea.append('p').text("Score:").attr('class','text-title');
}


