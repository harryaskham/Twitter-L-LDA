<?php
session_start();

include 'jasen/EpiCurl.php';
include 'jasen/EpiOAuth.php';
include 'jasen/EpiTwitter.php';
include 'jasen/secret.php';

$database="ha293";
$link = mysql_connect(localhost,"ha293","6dNuWH8W");
@mysql_select_db($database) or die( "Unable to select database");

$api = $_POST['api'];
$pages = $_POST['pages'];

$numWordsAssignedToTopic = array();
$topicLookup = array();
$numTopics = 0;
$numWords = 0;
$alpha = 0.01;
$beta = 0.1;
if($api == "alchemy") {
	$numWords = 153702;
	$numTopics = 12;
	$numWordsAssignedToTopic[0] = 524369;
	$numWordsAssignedToTopic[1] = 68483;
	$numWordsAssignedToTopic[2] = 227443;
	$numWordsAssignedToTopic[3] = 676077;
	$numWordsAssignedToTopic[4] = 143178;
	$numWordsAssignedToTopic[5] = 63144;
	$numWordsAssignedToTopic[6] = 181083;
	$numWordsAssignedToTopic[7] = 25000;
	$numWordsAssignedToTopic[8] = 55475;
	$numWordsAssignedToTopic[9] = 49659;
	$numWordsAssignedToTopic[10] = 55006;
	$numWordsAssignedToTopic[11] = 2054;
	$topicLookup[0] = "arts_entertainment";
	$topicLookup[1] = "health";
	$topicLookup[2] = "culture_politics";
	$topicLookup[3] = "computer_internet";
	$topicLookup[4] = "sports";
	$topicLookup[5] = "science_technology";
	$topicLookup[6] = "business";
	$topicLookup[7] = "recreation";
	$topicLookup[8] = "gaming";
	$topicLookup[9] = "religion";
	$topicLookup[10] = "law_crime";
	$topicLookup[11] = "weather";
} else if($api == "calais") {
	$numWords = 151273;
	$numTopics = 17;
	$numWordsAssignedToTopic[0] = 16014;
	$numWordsAssignedToTopic[1] = 15650;
	$numWordsAssignedToTopic[2] = 154225;
	$numWordsAssignedToTopic[3] = 278659;
	$numWordsAssignedToTopic[4] = 503695;
	$numWordsAssignedToTopic[5] = 445841;
	$numWordsAssignedToTopic[6] = 86948;
	$numWordsAssignedToTopic[7] = 92384;
	$numWordsAssignedToTopic[8] = 44441;
	$numWordsAssignedToTopic[9] = 25480;
	$numWordsAssignedToTopic[10] = 224397;
	$numWordsAssignedToTopic[11] = 27931;
	$numWordsAssignedToTopic[12] = 20874;
	$numWordsAssignedToTopic[13] = 37795;
	$numWordsAssignedToTopic[14] = 17386;
	$numWordsAssignedToTopic[15] = 17192;
	$numWordsAssignedToTopic[16] = 43581;
	$topicLookup[0] = "Education";
	$topicLookup[1] = "Entertainment_Culture";
	$topicLookup[2] = "Technology_Internet";
	$topicLookup[3] = "Law_Crime";
	$topicLookup[4] = "Social Issues";
	$topicLookup[5] = "Health_Medical_Pharma";
	$topicLookup[6] = "Hospitality_Recreation";
	$topicLookup[7] = "Environment";
	$topicLookup[8] = "Sports";
	$topicLookup[9] = "Labor";
	$topicLookup[10] = "Religion_Belief";
	$topicLookup[11] = "Politics";
	$topicLookup[12] = "Business_Finance";
	$topicLookup[13] = "Human Interest";
	$topicLookup[14] = "Disaster_Accident";
	$topicLookup[15] = "War_Conflict";
	$topicLookup[16] = "Weather";
} else if($api = "textwise") {
	$numWords = 43809;
	$numTopics = 11;
	$numWordsAssignedToTopic[0] = 78568;
	$numWordsAssignedToTopic[1] = 137873;
	$numWordsAssignedToTopic[2] = 234588;
	$numWordsAssignedToTopic[3] = 356107;
	$numWordsAssignedToTopic[4] = 263248;
	$numWordsAssignedToTopic[5] = 98356;
	$numWordsAssignedToTopic[6] = 299091;
	$numWordsAssignedToTopic[7] = 174135;
	$numWordsAssignedToTopic[8] = 151390;
	$numWordsAssignedToTopic[9] = 120006;
	$numWordsAssignedToTopic[10] = 27488;
	$topicLookup[0] = "Computers";
	$topicLookup[1] = "Games";
	$topicLookup[2] = "Sports";
	$topicLookup[3] = "Recreation";
	$topicLookup[4] = "Arts";
	$topicLookup[5] = "Science";
	$topicLookup[6] = "Society";
	$topicLookup[7] = "Business";
	$topicLookup[8] = "Health";
	$topicLookup[9] = "Home";
	$topicLookup[10] = "Reference";
}

function getProfileString($twitterObj, $username, $pages) {
	$tweetString = "";
	for($i=0;$i<$pages;$i++) {
		$timeline = $twitterObj->get_statusesUser_timeline(array('screen_name'=>$username,'page'=>$i));
		$tweets = $timeline->response;
		foreach($tweets as $tweet) {
			$tweetString .= $tweet['text']."<br />";
		}
	}
	return $tweetString;
}

function stripTweet($profileString) {
	$lower = strtolower($profileString);
	$split = preg_split('/\s+/', $lower);
	$result = "";
	foreach($split as $tokenWithPunctuation) {
		if(strlen($tokenWithPunctuation) >= 4 && substr($tokenWithPunctuation,0,3) == "www" || substr($tokenWithPunctuation,0,4) == "http") continue;

		if($tokenWithPunctuation{0} == "@") continue;

		$token = removePunctuation($tokenWithPunctuation);
		if($token == "") continue;
		if($token == "rt") continue;
		if(isStopword($token)) continue;

		$result .= $token." ";
	}
	return $result;
}

function removePunctuation($string) {
	$result = "";
	for($i=0; $i<strlen($string); $i++) {
		if($string{$i} >= 'a' && $string{$i} <= 'z') {
			$result .= $string{$i};
		}
	}
	return $result;
}

function isStopword($token) {
	$stopwords1 = array("a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains");
	$stopwords2 = array("corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon");
	$stopwords3 = array("hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one");
	$stopwords4 = array("ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these");
	$stopwords5 = array("they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero");

	if(in_array($token,$stopwords1)) return true;
	if(in_array($token,$stopwords2)) return true;
	if(in_array($token,$stopwords3)) return true;
	if(in_array($token,$stopwords4)) return true;
	if(in_array($token,$stopwords5)) return true;
	return false;
}

function lldaInference($profileString) {
	global $api, $numTopics, $numWords, $numWordsAssignedToTopic, $topicLookup, $alpha, $beta;
	$burnIn = 10;
	$sampling = 10;

	$d = preg_split('/\s+/', $profileString);
	$fv = array();
	$count = 0;
	for($i=0; $i<sizeof($d); $i++) {
		$query = "SELECT * FROM $api"."WordIDs WHERE word='$d[$i]\n'";
		$result = mysql_query($query);
		$num = mysql_num_rows($result);
		if($num > 0) {
			$fv[$count] = mysql_result($result,0,"wordID");
			$count++;
		}
	}

	$z = array();
	$zCounts = array();
	$thetam = array();
	for($i=0;$i<$numTopics;$i++) {
		$zCounts[$i] = 0;
		$thetam[$i] = 0.0;
	}
	for($n=0;$n<sizeof($fv);$n++) {
		$z[$n] = rand(0,$numTopics-1);
		$zCounts[$z[$n]]++;
	}
	$newAssignments = array();
	for($i=0;$i<$burnIn+$sampling;$i++) {
		for($n=0; $n<count($fv); $n++) {
			$numWordsAssignedToTopic[$z[$n]]--;
			if(array_key_exists($fv[$n],$newAssignments)) {
				$numAssignments = $newAssignments[$fv[$n]];
			} else {
				$query = "SELECT * FROM $api"."WordTopicAssignments WHERE wordID=$fv[$n]";
				$result = mysql_query($query);
				if(mysql_num_rows($result) > 0) {
					$numAssignmentsString = mysql_result($result,0,"numAssignments");
					$numAssignments = explode(",",$numAssignmentsString);
					$newAssignments[$fv[$n]] = $numAssignments;
				} else {
					continue;
				}
			}

			//disclude here
			$numAssignments[$z[$n]]--;

			$p = array();
			for($k=0;$k<$numTopics;$k++) {
				$p[$k] = (($numAssignments[$k] + $beta) / ($numWordsAssignedToTopic[$k]+$numWords*$beta)) * (($zCounts[k] + $alpha) / (sizeof($fv) + $numTopics * $alpha));
			}

			for($k=1; $k<$numTopics;$k++) {
				$p[$k] += $p[$k - 1];
			}

			$threshold = (rand()/getrandmax()) * $p[$numTopics - 1];
			$sampledTopicID = 0;
			for($sampledTopicID=0;$sampledTopicID<$numTopics;$sampledTopicID++) {
				if($threshold < $p[$sampledTopicID]) {
					break;
				}
			}
			
			$zCounts[$z[$n]]--;
			$z[$n] = $sampledTopicID;
			$numWordsAssignedToTopic[$z[$n]]++;
			$numAssignments[$z[$n]]++;
			$newAssignments[$fv[$n]] = $numAssignments;
			$zCounts[$z[$n]]++;
		}

		if($i >= $burnIn) {
			for($k=0; $k<$numTopics; $k++) {
				$thetam[$k] += ($zCounts[$k] + $alpha) / (sizeof($fv) + $numTopics * $alpha);
			}	
		}
	}

	$results = array();
	for($k=0; $k<$numTopics; $k++) {
		$thetam[$k] /= $sampling;
		$results[$k] = $thetam[$k];
	}
	asort($results);

	return $results;
}

$twitterObj = new EpiTwitter($consumer_key, $consumer_secret);
if(isset($_COOKIE['oauth_token'])) {
	$twitterObj->setToken($_COOKIE['oauth_token']);
} else {
	echo fail;
	exit;
}

$username = $_POST['username'];
$topicDistribution = lldaInference(stripTweet(getProfileString($twitterObj, $username, $pages)));
foreach($topicDistribution as $k => $val) {
	echo $topicLookup[$k].",".$val.",";
}

?>
