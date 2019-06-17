/**
 * This javascripts render Index.html with image search results.
 */

var imageData;

d3.json("NateSilver_Example/NateSilver_Example2.json").then(function(data) {
	imageData = data;
	loadIconImages();

	drawBarChart(imageData);

});

// load data into icon image gallery
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

// This method load original image and information when the user click on the
// icon image;
function loadOriginalImage(data) {

	// test whether the original image exist

	if (typeof data.OriginImage !== "undefined") {

		d3.select('.image-container').html("");
		d3.select('.image-container').append('div').attr('class',
				'origin-image');
		d3.select('.origin-image').append('img').attr('class', 'imageDiv');
		d3.select('.imageDiv').attr('src', data.OriginImage);

		// parse json object entries to array
		var labels_entries = Object.entries(data.label);
		var entities_entries = Object.entries(data.entity);
		var textArea = d3.select('.image-container').append('div').attr(
				'class', 'image-info');

		// clear previous content;
		textArea.html("");

		// write the content of image information
		textArea.append('p').text("Domain: ").attr('class', 'text-title');
		textArea.append('a').text(data.Domain).attr('class', 'text-link').attr(
				'href', data.Link).attr('target', "_blank");

		textArea.append('p').text("Score: ").attr('class', 'text-title');
		textArea.append('p').text(data.Score).attr('class', 'text-entry');

		textArea.append('p').text("Crawl Date: ").attr('class', 'text-title');
		textArea.append('p').text(data.Crawl_Date).attr('class', 'text-entry');

		textArea.append('p').text("Map Labels:").attr('class', 'text-title');
		textArea.selectAll('.text-entry1').data(labels_entries).enter().append(
				'p').text(function(d) {
			return d[0] + " : " + d[1];
		}).attr('class', 'text-entry1');

		textArea.append('p').text("Map Entities:").attr('class', 'text-title');
		textArea.selectAll('.text-entry2').data(entities_entries).enter()
				.append('p').text(function(d) {
					return d[0] + " : " + d[1];
				}).attr('class', 'text-entry2');

	} else {

		d3.select('.image-container').html("");
		d3.select('.image-container').append('div').attr('class',
				'origin-image');
		d3.select('.origin-image').text("Not available");

		// if the original image is not available, the entities and labels
		// derived from the original image are not available as well;
		var textArea = d3.select('.image-container').append('div').attr(
				'class', 'image-info');
		textArea.html("");
		textArea.append('p').text("Domain: ").attr('class', 'text-title');
		textArea.append('a').text(data.Domain).attr('class', 'text-link').attr(
				'href', data.Link).attr('target', "_blank");
		textArea.append('p').text("Score: ").attr('class', 'text-title');
		textArea.append('p').text(data.Score).attr('class', 'text-entry');
		textArea.append('p').text("Crawl Date: ").attr('class', 'text-title');
		textArea.append('p').text(data.Crawl_Date).attr('class', 'text-entry');

	}
}

//drawBarchart
function drawBarChart(dataArray) {
	
	//aggregate date by key, we use the year and month fields of the date as the new key to aggregate data.
	var data = d3.nest().key(function(d) {
		return d.Crawl_Date.split("-")[0] + "-" + d.Crawl_Date.split("-")[1];
	}).rollup(function(v) {
		return {
			count : v.length,
			meanScore : d3.mean(v, function(d) {
				return d.Score;
			})
		};
	}).entries(dataArray);

	console.log(data);
	
	//sort the data by date
	data.sort(function(a,b){
		  return new Date(a.key) - new Date(b.key);
		});
	
	
	//calculate the chart size according to the client browser size;
	var svg = d3.select("svg"),
    margin = {top: 20, right: 20, bottom: 30, left: 40},
    width = svg.node().clientWidth- margin.left - margin.right,
    height = svg.node().clientHeight - margin.top - margin.bottom;
	
	
	// define scales for x and y axis
	var x = d3.scaleBand().rangeRound([0, width]).padding(0.1),
    y = d3.scaleLinear().rangeRound([height, 0]);
	
	// project the coordinates due to the margin setting;
	var g = svg.append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	
	//calculate the domain according to data, use keys as x, use max count as the y upper bound;
    x.domain(data.map(function(d) { return d.key; }));
    y.domain([0, d3.max(data, function(d) { return d.value.count; })]);

    //draw x axis
    g.append("g")
        .attr("class", "axis axis--x")
        .attr("transform", "translate(0," + height + ")")
        .call(d3.axisBottom(x));

    //draw y axis
    g.append("g")
        .attr("class", "axis axis--y")
        .call(d3.axisLeft(y).ticks(10))
      .append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", "0.71em")
        .attr("text-anchor", "end")
        .text("Frequency");

    //draw bars
    g.selectAll(".bar")
      .data(data)
      .enter().append("rect")
        .attr("class", "bar")
        .attr("id",function(d) { return d.key; })
        .attr("x", function(d) { return x(d.key); })
        .attr("y", function(d) { return y(d.value.count); })
        .attr("width", x.bandwidth())
        .attr("height", function(d) { return height - y(d.value.count); });
    
}
