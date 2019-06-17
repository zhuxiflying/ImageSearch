/**
 * This javascripts render Index.html with image search results.
 */

var imageData;

d3.json("NateSilver_Example/NateSilver_Example2.json").then(function(data) {
	imageData = data;
	loadIconImages();
});


//load data into icon image gallery
function loadIconImages() {

	var gallery_container = d3.select('.map-gallery');

	// append div for icon images
	var icon_maps = gallery_container.selectAll('.icon-image').data(imageData)
			.enter().append('div').attr('class', 'icon-image');

	// append image element for div container
	gallery_container.selectAll('.icon-image').append('img').attr('src',
			function(d) {
				return d.Image_url;
			});

	// add event listener for load original image when clicked
	gallery_container.selectAll('.icon-image').on("click", loadOriginalImage);
}


//This method load original image and information when the user click on the icon image;
function loadOriginalImage(data) {

	// test whether the original image exist
	
	if (typeof data.OriginImage !== "undefined") {
		
		d3.select('.image-container').html("");
		d3.select('.image-container').append('div').attr('class','origin-image');
		d3.select('.origin-image').append('img').attr('class', 'imageDiv');
		d3.select('.imageDiv').attr('src', data.OriginImage);

		//parse json object entries to array
		var labels_entries = Object.entries(data.label);
		var entities_entries = Object.entries(data.entity);
		var textArea = d3.select('.image-container').append('div').attr('class','image-info');

		//clear previous content;
		textArea.html("");
	
		//write the content of image information
		textArea.append('p').text("Domain: ").attr('class', 'text-title');
		textArea.append('a').text(data.Domain).attr('class', 'text-link').attr('href',data.Link).attr('target',"_blank");
		
		textArea.append('p').text("Score: ").attr('class', 'text-title');
		textArea.append('p').text(data.Score).attr('class', 'text-entry');
		
		textArea.append('p').text("Crawl Date: ").attr('class', 'text-title');
		textArea.append('p').text(data.Crawl_Date).attr('class', 'text-entry');
		
		textArea.append('p').text("Map Labels:").attr('class', 'text-title');
		textArea.selectAll('.text-entry1').data(labels_entries).enter().append('p').text(function(d) {return d[0] + " : " + d[1];}).attr('class', 'text-entry1');

		textArea.append('p').text("Map Entities:").attr('class', 'text-title');
		textArea.selectAll('.text-entry2').data(entities_entries).enter().append('p').text(function(d) {return d[0] + " : " + d[1];}).attr('class', 'text-entry2');


		
	} else {
		
		d3.select('.image-container').html("");
		d3.select('.image-container').append('div').attr('class','origin-image');
		d3.select('.origin-image').text("Not available");
		
		//if the original image is not available, the entities and labels derived from the original image are not available as well;
		var textArea = d3.select('.image-container').append('div').attr('class','image-info');
		textArea.html("");
		textArea.append('p').text("Domain: ").attr('class', 'text-title');
		textArea.append('a').text(data.Domain).attr('class', 'text-link').attr('href',data.Link).attr('target',"_blank");
		textArea.append('p').text("Score: ").attr('class', 'text-title');
		textArea.append('p').text(data.Score).attr('class', 'text-entry');
		textArea.append('p').text("Crawl Date: ").attr('class', 'text-title');
		textArea.append('p').text(data.Crawl_Date).attr('class', 'text-entry');
		
	}
}
