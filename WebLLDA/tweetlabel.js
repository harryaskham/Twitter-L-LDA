var username = "";
function llda() {
	username = document.getElementById("usernameInput").value;
	var api = document.getElementById("apiSelect").value;
	if(api=="Alchemy") api = "alchemy";
	if(api=="OpenCalais") api = "calais";
	if(api=="Textwise") api = "textwise";
	var pages = parseInt(document.getElementById("pagesInput").value);
	if(pages > 5) {
		alert("There is a 5-page limit.");
		return;
	}
	dump(api);
	if(username == "") {
		document.getElementById("results").innerHTML = "Please enter a username";
		return;
	}
	dump(pages);
	document.getElementById("results").innerHTML = "Retrieving results";
	document.getElementById("pie").innerHTML = "";
	document.getElementById("interactive").innerHTML = "";
	showLoading();
	dump(username);
	$.post("llda.php", {username: username,api: api,pages: pages},lldaCallback);
}

function lldaCallback(data) {
	var classification = new Array();
	dump(data);
	if(data == "fail") {
		window.location = "http://www.srcf.ucam.org/~ha293/tweetlabel";
		return;
	}
	hideLoading();
	document.getElementById("pie").style.display = "block";
	var split = data.split(",");
	var count = 0;
	for(var i=0; i<split.length-1; i+=2) {
		classification[count] = {
			topic: split[i],
			score: parseFloat(split[i+1])
		};
		count++;
	}
	
	document.getElementById("results").innerHTML = "<h1>Profile is mostly about: <span id='topic'>"+classification[classification.length-1].topic+"</span></h1><h2>Topic Distribution for @"+username+"<br />";
	var graphData = new Array();
	for(var j=classification.length-1; j>=0; j--) {
		//document.getElementById("results").innerHTML += classification[j].topic+": "+classification[j].score+"<br />";
		graphData.push({label:classification[j].topic,data:classification[j].score});
	}


	plotGraph(graphData);
}

function plotGraph(data) {
	dump(data);
	$.plot($("#pie"), data,
	{
   	    series: {
       	    pie: { 
               	show: true,
				innerRadius: 0.5,				
			}
   	  	},
        grid: {
            hoverable: true,
            clickable: true
        }
	});
	$("#pie").bind("plothover", pieHover);
}

function pieHover(event, pos, obj) 
{
	if (!obj) return;
	percent = parseFloat(obj.series.percent).toFixed(2);
	$("#interactive").html('<span style="font-weight: bold; color: '+obj.series.color+'">'+obj.series.label+' ('+percent+'%)</span>');
}

function showLoading() {
	document.getElementById("loading").style.visibility = "visible";
	document.getElementById("classify").onclick = Function('alert("Already performing classification.")'); 
}

function hideLoading() {
	document.getElementById("loading").style.visibility = "hidden";
	document.getElementById("classify").onclick = Function('llda()')
}

function dump(thing) {
	//console.log(JSON.stringify(thing, null, 2));
}
