var TinEye = require('tineye-api')
var fs = require('fs')

var pub_key = 'paF_CEoXrXyP*CujKZfG';
var pri_key = '+JJzqLEdEUVWmLXXLpdj42xZx4MvMtKK84yeVYTF';

var input = 'D:/ViralMap/test4.jpg';
var output = 'D:/ViralMap/test4.json';


var api = new TinEye('https://api.tineye.com/rest/', pub_key, pri_key);
var img = fs.readFileSync(input);
var params = {
  'offset': 0,
  'limit': 400,
  'sort': 'score',
  'order': 'desc'
};
api.searchData(img, params)
  .then(function(response) {
    console.log(response);
    var result = JSON.stringify(response);
    fs.writeFile(output, result, function(err) {
        if (err) {
            console.log(err);
        }
    });
  })
  .catch(function(error) {
    console.log(error);
  });