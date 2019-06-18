/**
 * This javascripts render Index.html with image search results.
 */

var imageData;
var bar_color;

d3.json("NateSilver_Example/NateSilver_Example2.json").then(function(data) {

	imageData = data;
	initialize();

});

//initialize all the components;
function initialize() {
	initialColorScale(imageData);
	loadIconImages(imageData);
	drawBarChart(imageData);
	drawSliderBar(imageData);
}

//initialize color scale
function initialColorScale(dataArray) {
	bar_color = d3.scaleQuantile().range(
			[ "#feebe2", "#fbb4b9", "#f768a1", "#ae017e" ]);
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

	var min_score = d3.min(data, function(d) {
		return d.value.meanScore
	});
	var max_score = d3.max(data, function(d) {
		return d.value.meanScore
	});
	bar_color.domain([ min_score, max_score ]);

}

// load data into icon image gallery
function loadIconImages(data) {

	var gallery_container = d3.select('.map-gallery');

	//clean previous elements
	gallery_container.selectAll("*").remove();
	// append div for icon images
	var icon_maps = gallery_container.selectAll('.icon-image').data(data)
			.enter().append('div').attr('class', 'icon-image').style(
					'border-bottom-color', function(d) {
						return bar_color(d.Score);
					});

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

	//sort the data by date
	data.sort(function(a, b) {
		return new Date(a.key) - new Date(b.key);
	});

	//calculate the chart size according to the client browser size;
	var svg = d3.select('.bar-chart').select("svg"), margin = {
		top : 20,
		right : 20,
		bottom : 30,
		left : 40
	}, width = svg.node().clientWidth - margin.left - margin.right, height = svg
			.node().clientHeight
			- margin.top - margin.bottom;

	// define scales for x and y axis
	var x = d3.scaleBand().rangeRound([ 0, width ]).padding(0.1), y = d3
			.scaleLinear().rangeRound([ height, 0 ]);

	// project the coordinates due to the margin setting;
	var g = svg.append("g").attr("transform",
			"translate(" + margin.left + "," + margin.top + ")");

	//calculate the domain according to data, use keys as x, use max count as the y upper bound;
	x.domain(data.map(function(d) {
		return d.key;
	}));

	var max_frequency = d3.max(data, function(d) {
		return d.value.count;
	});
	y.domain([ 0, max_frequency ]);

	//draw x axis
	g.append("g").attr("class", "axis axis--x").attr("transform",
			"translate(0," + height + ")").call(d3.axisBottom(x));

	//draw y axis
	g.append("g").attr("class", "axis axis--y").call(d3.axisLeft(y).ticks(10))
			.append("text").attr("transform", "rotate(-90)").attr("y", 6).attr(
					"dy", "0.71em").attr("text-anchor", "end")
			.text("Frequency");

	//draw bars
	g.selectAll("rect").data(data).enter().append("rect").style('fill',
			function(d) {
				return bar_color(d.value.meanScore);
			}).attr("date", function(d) {
		return d.key;
	}).attr("x", function(d) {
		return x(d.key);
	}).attr("y", function(d) {
		return y(d.value.count);
	}).attr("width", x.bandwidth()).attr("height", function(d) {
		return height - y(d.value.count);
	}).on("click", function()
			{
		
		d3.select('.bar-selected').classed('bar-selected', false);
		d3.select(this).classed('bar-selected', true);
		
		update();
		
		
			});

}

function drawSliderBar(dataArray) {
	
	var svg = d3.select('.slider-bar').select("svg"), margin = {
		top : 40,
		right : 40,
		bottom : 60,
		left : 80
	}, width = svg.node().clientWidth - margin.left - margin.right, height = svg
			.node().clientHeight
			- margin.top - margin.bottom;

	var x = d3.scaleLinear().domain([ 0, 100 ]).range([ 0, width ]).clamp(true);

	var y = d3.scaleLinear().range([ height, 0 ]);

	var histogram = d3.histogram().value(function(d) {
		return d.Score;
	}).domain(x.domain()).thresholds(x.ticks(40));

	var bins = histogram(dataArray);
	console.log(bins);

	y.domain([ 0, d3.max(bins, function(d) {
		return d.length;
	}) ]);

	var hist = svg.append("g").attr("class", "histogram").attr("transform",
			"translate(" + margin.left + "," + margin.top + ")");

	var bar = hist.selectAll(".hist-bar").data(bins).enter().append("g").attr(
			"class", "hist-bar").attr("transform", function(d) {
		return "translate(" + x(d.x0) + "," + y(d.length) + ")";
	});

	bar.append("rect").attr("class", "hist-bar").attr("x", 1).attr("width",
			function(d) {
				return x(d.x1) - x(d.x0);
			}).attr("height", function(d) {
		return height - y(d.length);
	}).attr("fill", function(d) {
		return bar_color(d.x0);
	});

	var currentValue = 0;

	var slider = svg.append("g").attr("class", "slider").attr("transform",
			"translate(" + margin.left + "," + (margin.top + height + 5) + ")");

	slider.append("line").attr("class", "track").attr("x1", x.range()[0]).attr(
			"x2", x.range()[1]).select(function() {
		return this.parentNode.appendChild(this.cloneNode(true));
	}).attr("class", "track-inset").select(function() {
		return this.parentNode.appendChild(this.cloneNode(true));
	}).attr("class", "track-overlay").call(
			d3.drag().on("start.interrupt", function() {
				slider.interrupt();
			}).on("start drag", function() {
				currentValue = d3.event.x;
				//make sure the handle located within the range, assisted by the clamp setting of x axis;
				handle.attr("cx", x(x.invert(currentValue)));
			}).on("end", function() {
				var threshold = x.invert(d3.event.x);
				//               filterBySocre(threshold);

				handle.attr("score", x.invert(d3.event.x));
				
				var updateParams = {};

				var date = d3.select('.bar-selected');

				if (date.node() == null) {
					updateParams.date = null;
					updateParams.score = threshold;
				} else {
					updateParams.date = date.attr('id');
					updateParams.score = threshold;

				}
				update(updateParams);
			}));

	slider.insert("g", ".track-overlay").attr("class", "ticks").attr(
			"transform", "translate(0," + 18 + ")").selectAll("text").data(
			x.ticks(10)).enter().append("text").attr("x", x).attr("y", 10)
			.attr("text-anchor", "middle").text(function(d) {
				return d;
			});

	var handle = slider.insert("circle", ".track-overlay").attr("class",
			"handle").attr("r", 9).attr("score",0);

}


function update() {

	
	var score = d3.select('.handle').attr('score');
	var date;
	
	var selected_bar = d3.select('.bar-selected');
	if (selected_bar.node() == null) {
		date = null;
	} else {
		date = selected_bar.attr('date');
	}
	
	console.log(score);
	var sub_data = imageData.filter(function(d) {
		return d.Score >= score;
	});
	
	console.log(date);
	if(date!=null)
		{
		sub_data = sub_data
		.filter(function(d) {
			return d.Crawl_Date.split("-")[0] + "-"
					+ d.Crawl_Date.split("-")[1] == date
		});
		}
	
	loadIconImages(sub_data);

}
