/**
 * 
 */

loadIconImages();

// load data into icon image gallery
function loadIconImages() {

	var gallery_container = d3.select('.map-gallery-container');

	// append div for origin icon image
	gallery_container.append('div').attr('class', 'map_icon').attr('id', 'test1').append('img').attr('src',"test1\\test1.png");
	gallery_container.append('div').attr('class', 'map_icon').attr('id', 'test2').append('img').attr('src',"test2\\test2.jpg");
	gallery_container.append('div').attr('class', 'map_icon').attr('id', 'test3').append('img').attr('src',"test3\\test3.jpg");
	gallery_container.append('div').attr('class', 'map_icon').attr('id', 'test4').append('img').attr('src',"test4\\test4.jpg");
	gallery_container.append('div').attr('class', 'map_icon').attr('id', 'test5').append('img').attr('src',"test5\\test5.png");
	gallery_container.append('div').attr('class', 'map_icon').attr('id', 'test6').append('img').attr('src',"test6\\test6.png");
	gallery_container.append('div').attr('class', 'map_icon').attr('id', 'test7').append('img').attr('src',"test7\\test7.png");
	gallery_container.append('div').attr('class', 'map_icon').attr('id', 'test8').append('img').attr('src',"test8\\test8.png");
	
	gallery_container.selectAll('.map_icon').on("click", redirect);

}

function redirect()
{
	location.replace("http://localhost:8080/MapReverse/MapReverse.html?exampleId="+this.id);
}