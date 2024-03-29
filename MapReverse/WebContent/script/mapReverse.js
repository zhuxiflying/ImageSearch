/**
 * This javascripts render Index.html with image search results.
 */

var imageData;
var originImage;
var bar_color;
var tiptool_div;

var urlParams = new URLSearchParams(window.location.search);

var exampleId = urlParams.get('exampleId');
if(exampleId==null)exampleId="test2";



d3.json(exampleId+"/matches.json").then(function(data) {

	imageData = data;
	originImage = data.slice(0, 1);
	console.log(originImage);
	initialize();
	tiptool_div = d3.select("body").append("div").attr("class", "tooltip").style("display", "none");
	
	d3.select('.control-panel').on('dblclick',initialize);

});

// initialize all the components;
function initialize() {
	initialColorScale(imageData);
	loadIconImages(imageData);
	drawBarChart(imageData);
	drawSliderBar(imageData);
	drawTreemap(imageData);
}

// initialize color scale
function initialColorScale(dataArray) {
	
	// initial color scale for bar chart
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

	// clean previous elements
	gallery_container.selectAll("*").remove();
	
	// append div for origin icon image
	var icon_origin = gallery_container.selectAll('.origin-icon').data(originImage)
	.enter().append('div').attr('class', 'origin-icon');
	
	// append iamge element for div container
	gallery_container.selectAll('.origin-icon').append('img').attr('src',
			function(d) {
				return d.Image_url;
			});
	
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

	// add event listener for load original image when clicked or hovered
	gallery_container.selectAll('.icon-image').on("click", loadOriginalImage);
	gallery_container.selectAll('.icon-image').on("mouseover", hoverOnImage);
	gallery_container.selectAll('.icon-image').on("mouseout", hoverOnImageEnd);
	gallery_container.selectAll('.origin-icon').on("click", backToGallery);
}

// This method highligh information when the user hover on the
// icon image;
function hoverOnImage(data)
{
// alert(data.Crawl_Date);
// alert(data.entity);
	var key = data.Crawl_Date.split("-")[0] + "-" + data.Crawl_Date.split("-")[1];
	d3.select("[date='" + key + "']").attr('class','bar-hovered');
	if(data.entity!=null)
	{
	var entity_keys = Object.keys(data.entity);
	console.log(entity_keys);
	for(var i=0;i<entity_keys.length;i++)
    {
		
		d3.select('.treemap').select("[name='" + entity_keys[i].replace(/[^a-zA-Z ]/g, "") + "']").attr('class','tree-rect-hovered');
    }
	}
}

function backToGallery()
{
	location.replace("http://localhost:8080/MapReverse/MapGallery.html");
}

function hoverOnImageEnd(data)
{
	d3.select(".bar-hovered").attr('class',null);
	d3.select('.treemap').selectAll(".tree-rect-hovered").attr('class','tree-rect');
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
		d3.select('.origin-image').text("Original Image Not Available");
		d3.select('.origin-image').append('img').attr('class', 'imageDiv');
		d3.select('.imageDiv').attr('src', data.Image_url);

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

// drawBarchart
function drawBarChart(dataArray) {

	// aggregate date by key, we use the year and month fields of the date as
	// the new key to aggregate data.
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

	// sort the data by date
	data.sort(function(a, b) {
		return new Date(a.key) - new Date(b.key);
	});

	// calculate the chart size according to the client browser size;
	var svg = d3.select('.bar-chart').select("svg"), margin = {
		top : 20,
		right : 20,
		bottom : 30,
		left : 40
	}, width = svg.node().clientWidth - margin.left - margin.right, height = svg
			.node().clientHeight
			- margin.top - margin.bottom;

	svg.html("");
	
	// define scales for x and y axis
	var x = d3.scaleBand().rangeRound([ 0, width ]).padding(0.1), y = d3
			.scaleLinear().rangeRound([ height, 0 ]);

	// project the coordinates due to the margin setting;
	var g = svg.append("g").attr("transform",
			"translate(" + margin.left + "," + margin.top + ")");

	// calculate the domain according to data, use keys as x, use max count as
	// the y upper bound;
	x.domain(data.map(function(d) {
		return d.key;
	}));

	var max_frequency = d3.max(data, function(d) {
		return d.value.count;
	});
	y.domain([ 0, max_frequency ]);

	// draw x axis
	g.append("g").attr("class", "axis axis--x").attr("transform",
			"translate(0," + height + ")").call(d3.axisBottom(x));

	// draw y axis
	g.append("g").attr("class", "axis axis--y").call(d3.axisLeft(y).ticks(10))
			.append("text").attr("transform", "rotate(-90)").attr("y", 6).attr(
					"dy", "0.71em").attr("text-anchor", "end")
			.text("Frequency");

	// draw bars
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
	
	var svg = d3.select('.slider-bar').select("svg"),
	margin = {top : 40,right : 40,bottom : 60,left : 80},
	width = svg.node().clientWidth - margin.left - margin.right, 
	height = svg.node().clientHeight- margin.top - margin.bottom;

	svg.html("");
	
	var x = d3.scaleLinear().domain([ 0, 100 ]).range([ 0, width ]).clamp(true);

	var y = d3.scaleLinear().range([ height, 0 ]);

	var histogram = d3.histogram().value(function(d) {
		return d.Score;
	}).domain(x.domain()).thresholds(x.ticks(40));

	var bins = histogram(dataArray);

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
				// make sure the handle located within the range, assisted by
				// the clamp setting of x axis;
				handle.attr("cx", x(x.invert(currentValue)));
			}).on("end", function() {
				var threshold = x.invert(d3.event.x);
				// filterBySocre(threshold);

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


function drawTreemap(dataArray)
{

	// aggregate map entities by key;
	let entities = dataArray.map(a => a.entity);
	var entries = Object.entries(entities);

	var entity_sum = {}
	for(var i=0;i<entities.length;i++)
	{
		var entity = entities[i];
		if(entity!=null){
		var keys = Object.keys(entity);
		for(var j=0;j<keys.length;j++)
		{
			var key = keys[j];
			if (!entity_sum[key]) {
				entity_sum[key] = entity[key];
			  } else {
				  entity_sum[key] += entity[key];
			  }
				
		}
	}
	}

	
	var output = Object.entries(entity_sum).map(([name, value]) => ({name,value}));
	
	
	
	treemap_color = d3.scaleQuantile().range(
			[ "#8dd3c7", "#ffffb3", "#bebada", "#fb8072" , "#80b1d3","#fdb462", "#b3de69", "#fccde5", "#d9d9d9" , "#bc80bd"]);
	
	var min_score = d3.min(output, function(d) {
		return d.value
	});
	var max_score = d3.max(output, function(d) {
		return d.value
	});
	treemap_color.domain([ 0, max_score ]);
	
	
	var data = {
			  "name": "",
			  "children": output
			}
	
	
	var svg = d3.select('.treemap').select("svg"),
	width = svg.node().clientWidth, 
	height = svg.node().clientHeight;
	
	svg.html("");

	
	var treemap = d3.treemap()
    .size([width, height])
    .paddingInner(1);
	
	var root = d3.hierarchy(data);
	
	root.sum(function(d) {
		  return d.value;
		});
	
	treemap(root);
	
	
	var nodes = svg.selectAll('g')
	  .data(root.descendants())
	  .enter()
	  .append('g')
	  .attr('transform', function(d) {return 'translate(' + [d.x0, d.y0] + ')'})

	nodes
	  .append('rect')
	  .attr('class','tree-rect')
	  .attr('width', function(d) { return d.x1 - d.x0; })
	  .attr('height', function(d) { return d.y1 - d.y0; })
	  .attr('name',function(d) { return d.data.name.replace(/[^a-zA-Z ]/g, "");})
	  .style('fill',function(d){ return treemap_color(d.value)})
	  

	nodes
	  .append('text')
	  .attr('width', function(d) { return d.x1 - d.x0; })
	  .attr('height', function(d) { return d.y1 - d.y0; })
	  .attr('content',function(d) { return d.data.name;})
	  .attr('class','treemap-text')
	  .text(function(d) {
	    return d.data.name;
	  })
	  .call(wrap2);
	
	nodes
    .on("mouseover", mouseover)
    .on("mousemove", mousemove)
    .on("mouseout", mouseout);


	  
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
	
	var sub_data = imageData.filter(function(d) {
		return d.Score >= score;
	});
	
	if(date!=null)
		{
		sub_data = sub_data
		.filter(function(d) {
			return d.Crawl_Date.split("-")[0] + "-"
					+ d.Crawl_Date.split("-")[1] == date
		});
		}
	
	loadIconImages(sub_data);
	drawTreemap(sub_data);
}


// wrap text function
function wrap(text, width) {
	

	
    text.each(function () {
        var text = d3.select(this),
// words = text.text().split(/\s+/).reverse(),
           words = text.text().split(" ").reverse(),
            word,
            line = [],
            lineNumber = 0,
            lineHeight = 1.1, // ems
            x = text.attr("x"),
            y = text.attr("y"),
            dx = 4,
            dy = 14, // parseFloat(text.attr("dy")),
            
            
            
            tspan = text.text(null)
                        .append("tspan")
                        .attr("x", x)
                        .attr("y", y)
                        .attr("dx",dx)
                        .attr("dy", dy);

        
        while (word = words.pop()) {
        	
            line.push(word);
            tspan.text(line.join(" "));
            if (tspan.node().getComputedTextLength() > width) {
                line.pop();
                tspan.text(line.join(" "));
                line = [word];
                tspan = text.append("tspan")
                            .attr("x", 0)
                            .attr("y", y)
                            .attr("dx", dx )
                            .attr("dy", ++lineNumber * lineHeight + dy )
                            .text(word);
            }
        }
    });
}


function wrap2(text) {
	  text.each(function() {
		  
		    var text = d3.select(this),
	           words = text.text().split(" ").reverse(),
		        word,
		        line = [],
		        lineNumber = 0,
		        lineHeight = 1.1, // ems
		        y = text.attr("y"),
		        dy = 14,
		        tspan = text.text(null).append("tspan").attr("x", 0).attr("y", y).attr("dy", dy);
		    

		var text_height = parseFloat(text.attr("height"));
		var text_width = parseFloat(text.attr("width"));
		var text_content = text.attr("content");
		var length = textWidth(text_content, "12px sans-serif");

		  
		if(text_height> 20&&text_width>length)
		{
	        tspan = text.append("tspan").attr("x", 0).attr("y", y).attr("dy", dy).text(text_content);
// while (word = words.pop()) {
//	        	
// line.push(word);
// tspan.text(line.join(" "));
// if (tspan.node().getComputedTextLength() > width) {
// line.pop();
// tspan.text(line.join(" "));
// line = [word];
// console.log(x);
// tspan = text.append("tspan")
// .attr("x", 0)
// .attr("y", y)
// .attr("dx", dx )
// .attr("dy", ++lineNumber * lineHeight + dy+"em" )
// .text(word);
// }
// }
	    }
		
	  });
	}

function textWidth(text, fontProp) {
    var tag = document.createElement("div");
    tag.style.position = "absolute";
    tag.style.left = "-99in";
    tag.style.whiteSpace = "nowrap";
    tag.style.font = fontProp;
    tag.innerHTML = text;

    document.body.appendChild(tag);

    var result = tag.clientWidth;

    document.body.removeChild(tag);

    return result;
}

//handle mouse over on 

function mouseover() {
	tiptool_div.style("display", "inline");
	}

function mousemove(d)
{
	 tiptool_div.text(d.data.name)	
    .style("left", (d3.event.pageX - 80) + "px")		
    .style("top", (d3.event.pageY - 10) + "px");	
}

function mouseout() {
	tiptool_div.style("display", "none");
	}


