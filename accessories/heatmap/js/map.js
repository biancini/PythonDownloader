var heatmap, dygraph;
var jsonTweets, timer;
var dateStart, dateEnd, maxNumTweet;
var animationStart, aminationEnd, moveTime, running;

function initializeMap() {
	var mapOptions = {
		zoom: 6,
		center: new google.maps.LatLng(46.776665, 3.07723),
		mapTypeId: google.maps.MapTypeId.SATELLITE
	};
	var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

	heatmap = new google.maps.visualization.HeatmapLayer();
	heatmap.set('dissipating', false);
	heatmap.set('radius', 0.5);
	heatmap.setMap(map);
	setMapData(null, null);
}
	
function setMapData(dateFrom, dateTo) {
	var tweetData = [];
	for (var i = 0; i < jsonTweets["hits"]["hits"].length; i++) {
		var tweet = jsonTweets["hits"]["hits"][i]["_source"];
		if (tweet["coordinates"] != null) {
			var coordinates = tweet["coordinates"].split(",");
			var created_at = new Date(tweet["created_at"].replace(" ", "T") + "+00:00");

			if ((!dateFrom || (created_at - dateFrom) >= 0) && (!dateTo || (created_at - dateTo) <= 0)) {
				tweetData.push(new google.maps.LatLng(parseFloat(coordinates[0]), parseFloat(coordinates[1])));
			}
		}
	}

	var pointArray = new google.maps.MVCArray(tweetData);
	heatmap.setData(pointArray);
}

function groupTweetsByMinute() {
	var tweetByMinute = {};
	for (var i = 0; i < jsonTweets["hits"]["hits"].length; i++) {
		var tweet = jsonTweets["hits"]["hits"][i]["_source"];
		if (tweet["coordinates"] != null) {
			var created_at = new Date(tweet["created_at"].replace(" ", "T") + "+00:00");

			if (dateStart === undefined) dateStart = new Date(created_at.getTime());
			if (dateEnd === undefined) dateEnd = new Date(created_at.getTime());
			if (created_at.getTime() < dateStart.getTime()) dateStart = new Date(created_at.getTime());
			if (created_at.getTime() > dateEnd.getTime()) dateEnd = new Date(created_at.getTime());

			created_at.setSeconds(0);
			var key = created_at.toString('yyyy-MM-dd HH:mm:ss');

			if (tweetByMinute[key] === undefined) {
				tweetByMinute[key] = 1;
			} else {
				tweetByMinute[key] = tweetByMinute[key] + 1;
			}
		}
	}

	maxNumTweet = 0;
	for (var key in tweetByMinute) {
		if (tweetByMinute[key] > maxNumTweet) maxNumTweet = tweetByMinute[key];
	}

	console.log("First date: " + dateStart + "\nLast date: " + dateEnd);
	moveTime = Math.floor((dateEnd.getTime() - dateStart.getTime()) / $('#graph').width());
	console.log("Move time: " + moveTime);
	return tweetByMinute;
}
	
function initializeGraph() {
	var data = "Minute,Tweets\n";
	var tweetByMinute = groupTweetsByMinute();
	for (var key in tweetByMinute) {
		data = data + (new Date(key)).toString("yyyy-MM-dd") + "," + tweetByMinute[key] + "\n";
	}

	dygraph = new Dygraph(document.getElementById("graph"), data, {
		drawYAxis: false,
		zoomCallback: function(minX, maxX, yRanges) {
			setMapData(minX, maxX);
		}
	});
}

function initializeWithData() {
	$.ajax({
		url: 'tweets.json',
		dataType: 'json',
		success: function(response) {
			jsonTweets = response;

			initializeGraph();
			initializeMap();
		}
	});
}

function moveRange() {
	if (running == true) {
		animationStart = new Date(animationStart.getTime() + moveTime);
		animationEnd = new Date(animationEnd.getTime() + moveTime);
		if (animationEnd >= dateEnd) {
			startStopAnimation();
		} else {
			setMapData(animationStart, animationEnd);
			dygraph.updateOptions({ dateWindow: [animationStart, animationEnd] });
		}
	}
}

function startStopAnimation() {
	if ($('#animation').html() == "Start Animation") {
		$('#animation').text("Stop Animation");
		running = true;

		heatmap.set('maxIntensity', maxNumTweet);

		animationStart = new Date(dateStart.getTime());
		animationEnd = new Date(dateStart.getTime() + 10*60000);

		dygraph.updateOptions({ dateWindow: [animationStart, animationEnd] });
		setMapData(animationStart, animationEnd);
		timer = setInterval(moveRange, 50);
	} else {
		$('#animation').text("Start Animation");
		running = false;

		heatmap.reset('maxIntensity');
		dygraph.resetZoom();

		clearInterval(timer);
	}
}
	
function updateSizes() {
	var graph_height = $('#graph').height();
	var remaining_height = parseInt($(window).height() - graph_height); 
	$('#map-canvas').height(remaining_height); 
	$('#panel').css("bottom",  (graph_height + 5) + "px");
	$('#graph').width($(window).width()); 
}

function resetConfigs() {
	setMapData(dateStart, dateEnd);
	dygraph.resetZoom();
}

$(document).ready(function() {
	updateSizes();
	initializeWithData();
});
	
$(window).resize(function() {
	updateSizes();
});
