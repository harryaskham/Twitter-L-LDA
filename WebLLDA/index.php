<?php
session_start();
?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
        <title>TweetLabel</title>
	 	<style type="text/css">
            @import url("styles.css");
			#map_canvas {height: 400px;}
        </style>
        <script src="jquery.js"></script>
		<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="excanvas.min.js"></script><![endif]-->
		<script src="jquery.flot.js"></script>
		<script src="jquery.flot.pie.js"></script>
        <script src="tweetlabel.js"></script>
		<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>
		<script type="text/javascript">

			var alchemyMap = {
				arts_entertainment: "i/m"+1+".png",
				computer_internet: "i/m"+2+".png",
				culture_politics: "i/m"+3+".png",
				gaming: "i/m"+4+".png",
				law_crime: "i/m"+5+".png",
				science_technology: "i/m"+6+".png",
				recreation:"i/m"+7+".png",
				business: "i/m"+8+".png",
				religion: "i/m"+9+".png",
				sports: "i/m"+10+".png",
				health: "i/m"+11+".png",
				weather: "i/m"+12+".png"
			};

			var calaisMap = {
				Law_Crime: "i/m"+1+".png",
				Religion_Belief: "i/m"+2+".png",
				Education: "i/m"+3+".png",
				Health_Medical_Pharma: "i/m"+4+".png",
				Human_Interest: "i/m"+5+".png",
				Environment: "i/m"+6+".png",
				Technology_Internet: "i/m"+7+".png",
				Social_Issues: "i/m"+8+".png",
				Hospitality_Recreation: "i/m"+9+".png",
				Politics: "i/m"+10+".png",
				Sports: "i/m"+11+".png",
				Weather: "i/m"+12+".png",
				Disaster_Accident: "i/m"+13+".png",
				Entertainment_Culture: "i/m"+14+".png",
				Business_Finance: "i/m"+15+".png",
				Labor: "i/m"+16+".png",
				War_Conflict: "i/m"+17+".png"
			};

			var textwiseMap = {
				Science: "i/m"+1+".png",
				Business: "i/m"+2+".png",
				Home: "i/m"+3+".png",
				Recreation: "i/m"+4+".png",
				Society: "i/m"+5+".png",
				Games: "i/m"+6+".png",
				Sports: "i/m"+7+".png",
				Arts: "i/m"+8+".png",
				Reference: "i/m"+9+".png",
				Health: "i/m"+10+".png",
				Computers: "i/m"+11+".png"
			};

function getFile(fileName){
    oxmlhttp = null;
    try{
        oxmlhttp = new XMLHttpRequest();
        oxmlhttp.overrideMimeType("text/xml");
    }
    catch(e){
        try{
            oxmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
        }
        catch(e){
            return null;
        }
    }
    if(!oxmlhttp) return null;
    try{
       oxmlhttp.open("GET",fileName,false);
       oxmlhttp.send(null);
    }
    catch(e){
       return null;
    }
    return oxmlhttp.responseText;
}
			var topicType = "alchemy";
			function initialize() {
				var lat = 51.4963;
				var lng = -0.15527;
				var latlng = new google.maps.LatLng(lat,lng);
			    var myOptions = {
			      zoom: 8,
			      center: latlng,
			      mapTypeId: google.maps.MapTypeId.ROADMAP,
			    };
		    	var map = new google.maps.Map(document.getElementById("map_canvas"),myOptions);

				var datafile = getFile("locations.csv");
				var lines = datafile.split("\n");
				for(var i=0; i<lines.length; i++) {
					var split = lines[i].split(",");
					if(topicType == "alchemy") {
						var topic = split[3];
						var icon = alchemyMap[topic];
					} else if(topicType == "calais") {
						var topic = split[4];
						if(topic == "Human Interest") topic = "Human_Interest";
						if(topic == "Social Issues") topic = "Social_Issues";
						var icon = calaisMap[topic];
					} else if(topicType == "textwise") {
						var topic = split[5];
						var icon = textwiseMap[topic];
					}
					var lat = parseFloat(split[1]);
					var lng = parseFloat(split[2]);

					var marker = new google.maps.Marker({
						map: map,
						position: new google.maps.LatLng(lat,lng),
						icon: icon,
						clickable: true,
						title:  "User ID: "+split[0]+" - Top Topic: "+topic
					});
					marker.string =  "<b>User ID:</b> "+split[0]+"<br /><b>Top Topic:</b> "+topic;
			    }
			}

			function alchemy() {
				topicType = "alchemy";
				document.getElementById("alchemyKey").style.display = "block";
				document.getElementById("calaisKey").style.display = "none";
				document.getElementById("textwiseKey").style.display = "none";
				initialize();
			}

			function calais() {
				topicType = "calais";
				document.getElementById("calaisKey").style.display = "block";
				document.getElementById("alchemyKey").style.display = "none";
				document.getElementById("textwiseKey").style.display = "none";
				initialize();
			}

			function textwise() {
				topicType = "textwise";
				document.getElementById("textwiseKey").style.display = "block";
				document.getElementById("calaisKey").style.display = "none";
				document.getElementById("alchemyKey").style.display = "none";
				initialize();
			}
				
		</script>
	</head>
	<body onload="initialize()">
		<div id="head"></head>
		<div id="content">
<?php
			include 'jasen/EpiCurl.php';
include 'jasen/EpiOAuth.php';
include 'jasen/EpiTwitter.php';
include 'jasen/secret.php';

				$twitterObj = new EpiTwitter($consumer_key, $consumer_secret);
				if(isset($_GET['oauth_token'])) {
					$twitterObj->setToken($_GET['oauth_token']);
					$token = $twitterObj->getAccessToken();
					$twitterObj->setToken($token->oauth_token, $token->oauth_token_secret);
					@setcookie('oauth_token', $token->oauth_token);
					@setcookie('oauth_token_secret', $token->oauth_token_secret);
					echo '<div id="classifyBox">';
					echo '<h1>L-LDA Classifier</h1>';
					echo 'Enter a Twitter username to classify, choose a topic set, the number of pages of tweets to analyse, and then click Classify:</br />';
					echo '(Note: The more pages chosen, the longer classification will take - <span style="color:#444">currently only works in Google Chrome</span>)<br /></br />';
					echo '@<input type="text" id="usernameInput" size=20></input>	';
					echo '<select id="apiSelect"><option>Alchemy</option><option>OpenCalais</option><option>Textwise</option></select>	';
					echo '<input type="text" id="pagesInput" size=3 value="1"></input>	';
					echo '<a id="classify" href="#" onclick="llda()">Classify</a></br /><br />';
					echo '</div>';
				} else {
					echo '<div id="signinInstructions">';
					echo 'To use TweetLabel, please authenticate via Twitter.';
					echo '<br /><br />';
					echo '<a id="signin" href="' . $twitterObj->getAuthorizationUrl() . '"><img src="signin.png" /></a>';
					echo '</div>';
				}
			?>
			<div id="results"></div>
			<div id="pie"></div>
			<div id="interactive"></div>
			<h1>Geolocated Twitter Dataset Classifications</h1>
			(Hover over a user for more detailed information)<br /><br />
			<div id="mapContainer">
				<div id="map_canvas" style="width:600px; height:400px;"></div>
				<a name="undermap"></a>
				<br />Classification Type: 
				<a href="#undermap" onclick="alchemy()">Alchemy</a> - 
				<a href="#undermap" onclick="calais()">OpenCalais</a> - 
				<a href="#undermap" onclick="textwise()">Textwise</a>
			</div>
			<div class="key" id="alchemyKey">
				arts_entertainment<img src="i/m1.png" /><br />
				computer_internet<img src="i/m2.png" /><br />
				culture_politics<img src="i/m3.png" /><br />
				gaming<img src="i/m4.png" /><br />
				law_crime: <img src="i/m5.png" /><br />
				science_technology: <img src="i/m6.png" /><br />
				recreation:<img src="i/m7.png" /><br />
				business: <img src="i/m8.png" /><br />
				religion: <img src="i/m9.png" /><br />
				sports: <img src="i/m10.png" /><br />
				health: <img src="i/m11.png" /><br />
				weather: <img src="i/m12.png" />
			</div>
			<div class="key" id="calaisKey">
				Law_Crime<img src="i/m1.png" /><br />
				Religion_Belief<img src="i/m2.png" /><br />
				Education<img src="i/m3.png" /><br />
				Health_Medical_Pharma<img src="i/m4.png" /><br />
				Human_Interest<img src="i/m5.png" /><br />
				Environment<img src="i/m6.png" /><br />
				Technology_Internet<img src="i/m7.png" /><br />
				Social_Issues<img src="i/m8.png" /><br />
				Hospitality_Recreation<img src="i/m9.png" /><br />
				Politics<img src="i/m10.png" /><br />
				Sports<img src="i/m11.png" /><br />
				Weather<img src="i/m12.png" /><br />
				Disaster_Accident<img src="i/m13.png" /><br />
				Entertainment_Culture<img src="i/m14.png" /><br />
				Business_Finance<img src="i/m15.png" /><br />
				Labor<img src="i/m16.png" /><br />
				War_Conflict<img src="i/m17.png" />

			</div>
			<div class="key" id="textwiseKey">
				Science<img src="i/m1.png" /><br />
				Business<img src="i/m2.png" /><br />
				Home<img src="i/m3.png" /><br />
				Recreation<img src="i/m4.png" /><br />
				Society<img src="i/m5.png" /><br />
				Games<img src="i/m6.png" /><br />
				Sports<img src="i/m7.png" /><br />
				Arts<img src="i/m8.png" /><br />
				Reference<img src="i/m9.png" /><br />
				Health<img src="i/m10.png" /><br />
				Computers<img src="i/m11.png" />
			</div>

		</div>
		<div id="loading"></div>
	</body>
</html>
