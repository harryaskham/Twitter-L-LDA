package uk.ac.cam.ha293.tweetlabel.twitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ha293.tweetlabel.types.Document;
import uk.ac.cam.ha293.tweetlabel.classify.AlchemyClassification;
import uk.ac.cam.ha293.tweetlabel.classify.AlchemyClassifier;
import uk.ac.cam.ha293.tweetlabel.classify.CalaisClassification;
import uk.ac.cam.ha293.tweetlabel.classify.CalaisClassifier;
import uk.ac.cam.ha293.tweetlabel.classify.FullAlchemyClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullCalaisClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullTextwiseClassification;
import uk.ac.cam.ha293.tweetlabel.classify.TextwiseClassification;
import uk.ac.cam.ha293.tweetlabel.classify.TextwiseClassifier;
import uk.ac.cam.ha293.tweetlabel.liwc.FullLIWCClassification;
import uk.ac.cam.ha293.tweetlabel.liwc.LIWCDictionary;
import uk.ac.cam.ha293.tweetlabel.types.Category;
import uk.ac.cam.ha293.tweetlabel.types.CategoryScore;
import uk.ac.cam.ha293.tweetlabel.types.Corpus;
import uk.ac.cam.ha293.tweetlabel.util.Tools;
import cc.mallet.types.Instance;

public class SimpleProfile implements Serializable {

	private static final long serialVersionUID = 4610549868654867470L;
	
	//private static LIWCDictionary liwc = LIWCDictionary.loadDictionaryFromFile("liwc/dictionary.obj");
	private long userID;
	private List<SimpleTweet> tweets;
	
	public SimpleProfile(long userID) {
		this.userID = userID;
		tweets = new ArrayList<SimpleTweet>();
	}
	
	public long getUserID() {
		return userID;
	}
	
	public void addTweet(SimpleTweet tweet) {
		tweets.add(tweet);
	}
	
	public void addTweets(List<SimpleTweet> tweetList) {
		tweets.addAll(tweetList);
	}
	
	public List<SimpleTweet> getTweets() {
		return tweets;
	}
	
	public void print() {
		System.out.println("Twitter Profile for "+userID);
		for(SimpleTweet tweet : tweets) {
			tweet.print();
		}
	}
	
	public void printUrls() {
		List<String> urls = new ArrayList<String>();
		for(SimpleTweet tweet : tweets) {
			urls.addAll(tweet.getUrls());
		}
		System.out.println("Urls Used: ");
		for(String url : urls) {
			System.out.println(url);
		}
	}
	
	public void printHashtags() {
		List<String> hashtags = new ArrayList<String>();
		for(SimpleTweet tweet : tweets) {
			hashtags.addAll(tweet.getHashtags());			
		}
		System.out.println("Hashtags Used: ");
		for(String hashtag : hashtags) {
			System.out.println(hashtag);
		}
	}
	
	public void printStripped() {
		System.out.println("Twitter Profile for "+userID);
		for(SimpleTweet tweet : tweets) {
			tweet.printStripped();
		}
	}
	
	public void save() {
		try {
			String profileFilename = "profiles/simple/"+userID+".sprofile";
			FileOutputStream fileOut = new FileOutputStream(profileFilename);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);
			objectOut.close();
			System.out.println("Saved simple profile for "+userID);
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't save simple profile for "+userID);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't save simple profile for "+userID);
			e.printStackTrace();			
		}
	}
	
	public void saveCSV() {
		try {
			String profileFilename = "profiles/csv/"+userID+".csv";
			FileOutputStream fileOut = new FileOutputStream(profileFilename);
			PrintWriter writeOut = new PrintWriter(fileOut);
			writeOut.println("\"userid\",\"tweetid\",\"text\"");
			for(SimpleTweet tweet : tweets) {
				writeOut.print(tweet.getUserID()+",");
				writeOut.print(tweet.getTweetID()+",");
				writeOut.print("\""+tweet.getText()+"\"");
				writeOut.println();
			}
			writeOut.close();
			System.out.println("Saved CSV profile for "+userID);
		} catch (IOException e) {
			System.out.println("Couldn't save CSV profile for "+userID);
			e.printStackTrace();
		}
	}
	
	/*
	//NOTE: We perform stripping while converting a profile to a Document Set
	//NOTE: NEED TO MEMOISE CLASSIFICATIONS
	public Set<Document> asDocumentSet(String topicType) {
		System.out.println("Converting profile to Document set...");
		Set<Document> documents = new HashSet<Document>();
		Map<Long,List<CategoryScore>> scores = null; //for LIWC
		int count = 0;
		for(SimpleTweet tweet : tweets) {
			if(count % 50 == 0) System.out.println(count+"/"+tweets.size()+" tweets converted...");
			count++;
			String strippedText = Tools.stripTweet(tweet.getText());
			//String strippedText = Tools.stripTweetVerbose(tweet.getText());
			Set<String> topics = new HashSet<String>();
			Document document = null;
			if(topicType != null) {
				//NOTE: We classify on the TWEET, not on the stemmed/stripped version!
				if(topicType.equals("liwc")) {
					if(scores == null) scores = liwc.getLIWCCategoryScores(userID);
					//List<CategoryScore> liwcClassifications = liwc.classifyTweet(tweet); //NOTE: this is already sorted by LIWCDictionary
					List<CategoryScore> liwcClassifications = scores.get(tweet.getTweetID());
					//Take the top 3... note that this is of limited use, I just need some topics to get started
					int getTop = 3;
					if(getTop > liwcClassifications.size()) getTop = liwcClassifications.size();
					for(int i=0; i<getTop; i++) {
						topics.add(liwcClassifications.get(i).getCategory().getTitle());
					}
					document = new Document(strippedText,tweet.getTweetID(),topics);		
				}
				
				else if(topicType.equals("liwcnb")) {
					if(scores == null) scores = liwc.getLIWCCategoryScoresNB(userID);
					//List<CategoryScore> liwcClassifications = liwc.classifyTweetNaiveBayes(tweet); //NOTE: this is already sorted by LIWCDictionary
					List<CategoryScore> liwcClassifications = scores.get(tweet.getTweetID());
					//Take the top 3... note that this is of limited use, I just need some topics to get started
					int getTop = 3;
					if(getTop > liwcClassifications.size()) getTop = liwcClassifications.size();
					for(int i=0; i<getTop; i++) {
						topics.add(liwcClassifications.get(i).getCategory().getTitle());
					}
					document = new Document(strippedText,tweet.getTweetID(),topics);	
				}
			} else {
				document = new Document(strippedText,tweet.getTweetID());
			}
			documents.add(document);
		}
		return documents;
	}
	
	public Set<Document> asDocumentSet() {
		return asDocumentSet(null);
	}
	
	
	public Corpus asCorpus(String topicType) {
		Corpus corpus = new Corpus(this.asDocumentSet(topicType),topicType);
		return corpus;
	}
	
	public Corpus asCorpus() {
		Corpus corpus = new Corpus(this.asDocumentSet(null));
		return corpus;
	}
	*/

	/* NOTE Details of weighted alchemy classification here:
	 * Rather than counting instances of each different classification
	 * by adding 1 every time a category appears, instead the score
	 * of that category (between 0.0 and 1.0) will be added to that
	 * category's count - this way a weighted count can be achieved.
	 * Two seperate counts are found - text count (based on tweet content)
	 * and URL count (based on the classifications of all the URLs of a
	 * profile. String categories therefore form the keys of the count
	 * hashmaps.  
	 * 
	 * Also NOTE: this method can take AGES
	 * 
	 * Also NOTE: this method ONLY SAVES A CATEGORY into teh csv if it appears
	 * in the profile or its URLS. it doesn't have a comprehensive list of all alchemy cats.
	 */
	public boolean classifyAlchemy() {
		
		System.out.println("Alchemy classifying profile "+userID);
		
		//Check if classification already exists, to save time and allow resuming classification if something goes wrong
		String classificationFilename = "classifications/alchemy/"+userID+".csv";
		File classificationFile = new File(classificationFilename);
		if(classificationFile.exists()) {
			System.out.println("Alchemy classification already exists for "+userID+", aborting");
			return true; //or do we want to return false...
		}
		
		FileOutputStream fileOutTweets = null;
		PrintWriter writeOutTweets = null;
		try {
			String tweetsFilename = "classifications/alchemy/tweets/"+userID+".csv";
			fileOutTweets = new FileOutputStream(tweetsFilename);
			writeOutTweets = new PrintWriter(fileOutTweets);
			writeOutTweets.println("\"tweetID\",\"category\",\"score\",\"urlcategories\",\"urlscores\"");
		} catch(IOException e) {
			System.err.println("Couldn't open tweets file to save to");
		}
		
		Map<String,Double> textCategoryScores = new HashMap<String,Double>();
		Map<String,Integer> textCategoryCounts = new HashMap<String,Integer>();
		Map<String,Double> urlCategoryScores = new HashMap<String,Double>();
		Map<String,Integer> urlCategoryCounts = new HashMap<String,Integer>();
		
		for(SimpleTweet tweet : tweets) {
			//Handle tweet content classification
			AlchemyClassification textClassification = AlchemyClassifier.classifyText(tweet.getText());
			if(textClassification == null) {
				System.err.println("Daily Transaction Limit has been reached (or a null classification was given weirdly)");
				return false;
			}
			
			writeOutTweets.print(tweet.getTweetID()+","+textClassification.getCategory()+","+textClassification.getScore());
			System.err.println("JUST WROTE TWEEET OUT");
			
			if(textCategoryScores.containsKey(textClassification.getCategory())) {
				//Add the new value, since the key already exists
				textCategoryScores.put(textClassification.getCategory(), textCategoryScores.get(textClassification.getCategory()) + textClassification.getScore());
				textCategoryCounts.put(textClassification.getCategory(), textCategoryCounts.get(textClassification.getCategory()) + 1);
			} else {
				//Insert the category into the map
				textCategoryScores.put(textClassification.getCategory(), textClassification.getScore());
				textCategoryCounts.put(textClassification.getCategory(), 1);
			}
			
			//Handle URL classification
			for(String url : tweet.getUrls()) {
				AlchemyClassification urlClassification = AlchemyClassifier.classifyURL(url);
				if(urlClassification == null) {
					System.err.println("Daily Transaction Limit has been reached (or a null classification was given weirdly)");
					return false;
				}
				
				writeOutTweets.print(","+urlClassification.getCategory()+","+urlClassification.getScore());
				
				if(urlCategoryScores.containsKey(urlClassification.getCategory())) {
					//Add the new value, since the key already exists
					urlCategoryScores.put(urlClassification.getCategory(), urlCategoryScores.get(urlClassification.getCategory()) + urlClassification.getScore());
					urlCategoryCounts.put(urlClassification.getCategory(), urlCategoryCounts.get(urlClassification.getCategory()) + 1);
				} else {
					//Insert the category into the map
					urlCategoryScores.put(urlClassification.getCategory(), urlClassification.getScore());
					urlCategoryCounts.put(urlClassification.getCategory(), 1);
				}
			}

			//writeOutTweets.println();
		}
		
		writeOutTweets.close();
		
		//Now save as a CSV Alchemy classification file containing the scores
		try {
			FileOutputStream fileOut = new FileOutputStream(classificationFilename);
			PrintWriter writeOut = new PrintWriter(fileOut);
			writeOut.println("\"category\",\"textscore\",\"textcount\",\"urlscore\",\"urlcount\"");
			
			//Need to merge the category sets from both text and URL classifications
			//in case one has a category the other does not have
	
			Set<String> categorySet = new HashSet<String>(textCategoryScores.keySet());
			categorySet.addAll(urlCategoryScores.keySet());
			
			//remove the useless categories
			categorySet.remove(null); //you can have a null key? weird
			categorySet.remove("unknown");
			
			for(String category : categorySet) {				
				Double textscore = textCategoryScores.get(category);
				Integer textcount = textCategoryCounts.get(category);
				Double urlscore = urlCategoryScores.get(category);
				Integer urlcount = urlCategoryCounts.get(category);
				
				//If no score exists for a category, it has not appeared in a profile
				if(textscore == null) {
					textscore = new Double(0.0);
					textcount = new Integer(0);
				}
				if(urlscore == null) {
					urlscore = new Double(0.0);
					urlcount = new Integer(0);
				}
				
				writeOut.print("\""+category+"\",");
				writeOut.print(textscore+",");
				writeOut.print(textcount+",");
				writeOut.print(urlscore+",");
				writeOut.print(urlcount);
				writeOut.println();
			}
			writeOut.close();
			System.out.println("Saved CSV Alchemy classification for "+userID);
			return true;
		} catch (IOException e) {
			System.out.println("Couldn't save CSV Alchemy classification for "+userID);
			e.printStackTrace();
			return false;
		}
	}
	
	/*
	//NOTE: Incorporates both naive frequency counting and naive bayes, stores both
	public boolean classifyLIWC() {
		System.out.println("LIWC classifying profile "+userID);
		
		//Check if classification already exists, to save time and allow resuming classification if something goes wrong
		String classificationFilename = "classifications/liwc/"+userID+".csv";
		String classificationFilenameCount = "classifications/liwc/count/"+userID+".csv";
		String classificationFilenameNB = "classifications/liwc/nb/"+userID+".csv";
		File classificationFileCount = new File(classificationFilenameCount);
		File classificationFileNB = new File(classificationFilenameNB);
		if(classificationFileCount.exists() || classificationFileNB.exists()) {
			System.out.println("LIWC classification already exists for "+userID+", aborting");
			return true; //or do we want to return false...
		}
							
		//Now save as a CSV LIWC classification file containing the scores
		try {
			FileOutputStream fileOut = new FileOutputStream(classificationFilename);
			FileOutputStream fileOutCount = new FileOutputStream(classificationFilenameCount);
			FileOutputStream fileOutNB = new FileOutputStream(classificationFilenameNB);
			PrintWriter writeOut= new PrintWriter(fileOut);
			PrintWriter writeOutCount = new PrintWriter(fileOutCount);
			PrintWriter writeOutNB = new PrintWriter(fileOutNB);
			writeOut.println("\"category\",\"countscore\",\"nbscore\"");
			writeOutCount.println("\"tweetid\",\"category\",\"countscore\"");
			writeOutNB.println("\"tweetid\",\"category\",\"nbscore\"");
			
			//To speed up full-profile classification
			Map<Category,Double> scoresNormal = new HashMap<Category,Double>();
			Map<Category,Double> scoresNaiveBayes = new HashMap<Category,Double>();
		
			//Individual tweet classifications
			int averageCount = 0;
			for(SimpleTweet tweet : tweets) {
				List<CategoryScore> scoresCount = liwc.classifyTweet(tweet);
				List<CategoryScore> scoresNB = liwc.classifyTweetNaiveBayes(tweet);
				Collections.sort(scoresCount);
				Collections.sort(scoresNB);
				Collections.reverse(scoresCount);
				Collections.reverse(scoresNB);
				
				writeOutCount.print(tweet.getTweetID()+",");
				String outString = "";
				for(CategoryScore score : scoresCount) {
					//update counts for full-profile
					if(scoresNormal.containsKey(score.getCategory())) {
						scoresNormal.put(score.getCategory(),scoresNormal.get(score.getCategory())+score.getScore());
					} else {
						scoresNormal.put(score.getCategory(),score.getScore());
					}
					
					outString += (score.getCategory().getTitle()+","+score.getScore()+",");	
				}
				if(!outString.isEmpty()) outString.subSequence(0, outString.length()-1);
				writeOutCount.print(outString);
				writeOutCount.println();
				
				writeOutNB.print(tweet.getTweetID()+",");
				outString = "";
				for(CategoryScore score : scoresNB) {
					//update counts for full-profile
					if(scoresNaiveBayes.containsKey(score.getCategory())) {
						//Moving average - hacky...
						scoresNaiveBayes.put(score.getCategory(),((scoresNaiveBayes.get(score.getCategory())*averageCount)+score.getScore()) / (averageCount+1));
					} else {
						scoresNaiveBayes.put(score.getCategory(),score.getScore());
					}
					
					outString += (score.getCategory().getTitle()+","+score.getScore()+",");	
				}
				if(!outString.isEmpty()) outString.subSequence(0, outString.length()-1);
				writeOutNB.print(outString);
				writeOutNB.println();
				averageCount++;
			}
			writeOutCount.close();
			writeOutNB.close();
			
			Set<Category> fullCatSet = new HashSet<Category>();
			fullCatSet.addAll(scoresNormal.keySet());
			fullCatSet.addAll(scoresNaiveBayes.keySet());
			for(Category cat : fullCatSet) {
				Double normalScore = scoresNormal.get(cat);
				if(normalScore == null) normalScore = 0.0;
				writeOut.println(cat.getTitle()+","+normalScore+","+scoresNaiveBayes.get(cat));
			}
			writeOut.close();
			
			System.out.println("Saved CSV LIWC classification for "+userID);
			return true;
		} catch (IOException e) {
			System.out.println("Couldn't save CSV LIWC classification for "+userID);
			e.printStackTrace();
			return false;
		}
	}
	*/
	
	//Performs the same weighted sum as the Alchemy classification does
	//ie sums the scores for all categories over all tweets.
	public boolean classifyCalais() {
		System.out.println("Calais classifying profile "+userID);
		
		//Check if classification already exists, to save time and allow resuming classification if something goes wrong
		String classificationFilename = "classifications/calais/"+userID+".csv";
		File classificationFile = new File(classificationFilename);
		if(classificationFile.exists()) {
			System.out.println("Calais classification already exists for "+userID+", aborting");
			return true; //or do we want to return false...
		}
		
		Map<String,Double> resultsMap = new HashMap<String,Double>();
		Map<String,Integer> countsMap = new HashMap<String,Integer>();
		
		for(SimpleTweet tweet : tweets) {
			CalaisClassification classification = CalaisClassifier.classifyText(tweet.getText());
			if(classification == null) {
				//Normally this is because of a <100 char tweet
				continue;
			}
			for(String category : classification.getCategories()) {
				double score = classification.lookupScore(category);			
				if(resultsMap.containsKey(category)) {
					resultsMap.put(category, resultsMap.get(category) + score);
					countsMap.put(category, countsMap.get(category) + 1);
				} else {
					resultsMap.put(category, score);
					countsMap.put(category, 1);
				}
			}
		}
							
		//Now save as a CSV Calais classification file containing the scores
		try {
			FileOutputStream fileOut = new FileOutputStream(classificationFilename);
			PrintWriter writeOut = new PrintWriter(fileOut);
			writeOut.println("\"category\",\"score\",\"countscore\"");
		
			for(String category : resultsMap.keySet()) {
				writeOut.print("\""+category+"\",");
				writeOut.print(resultsMap.get(category)+",");
				writeOut.print(countsMap.get(category));
				writeOut.println();
			} 
			writeOut.close();
			System.out.println("Saved CSV Calais classification for "+userID);
			return true;
		} catch (IOException e) {
			System.out.println("Couldn't save CSV Calais classification for "+userID);
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean classifyTextwise() {
		
		System.out.println("Textwise classifying profile "+userID);
		
		//Check if classification already exists, to save time and allow resuming classification if something goes wrong
		String classificationFilename = "classifications/textwise/"+userID+".csv";
		File classificationFile = new File(classificationFilename);
		if(classificationFile.exists()) {
			System.out.println("Textwise classification already exists for "+userID+", aborting");
			return true; //or do we want to return false...
		}
		
		Map<String,Double> textCategoryScores = new HashMap<String,Double>();
		Map<String,Integer> textCategoryCounts = new HashMap<String,Integer>();
		Map<String,Double> urlCategoryScores = new HashMap<String,Double>();
		Map<String,Integer> urlCategoryCounts = new HashMap<String,Integer>();
		
		for(SimpleTweet tweet : tweets) {
			//System.err.println(tweet.getText());
			
			TextwiseClassification textClassification = TextwiseClassifier.classify(tweet.getText(), false);
			
			textClassification.print();
			
			for(String category : textClassification.getCategories()) {
				if(textCategoryScores.containsKey(category)) {
					textCategoryScores.put(category, textCategoryScores.get(category) + textClassification.lookupScore(category));
					textCategoryCounts.put(category, textCategoryCounts.get(category) + 1);
				} else {
					textCategoryScores.put(category, textClassification.lookupScore(category));
					textCategoryCounts.put(category, 1);
				}
			}
			
			for(String url : tweet.getUrls()) {
				
				//System.err.println(url);
				
				TextwiseClassification urlClassification = TextwiseClassifier.classify(url, true);
				
				urlClassification.print();
				
				for(String category : urlClassification.getCategories()) {
					if(urlCategoryScores.containsKey(category)) {
						urlCategoryScores.put(category, urlCategoryScores.get(category) + urlClassification.lookupScore(category));
						urlCategoryCounts.put(category, urlCategoryCounts.get(category) + 1);
					} else {
						urlCategoryScores.put(category, urlClassification.lookupScore(category));
						urlCategoryCounts.put(category, 1);
					}
				}
			}
			
		}
		
		//Now save as a CSV Textwise classification file containing the scores
		try {
			FileOutputStream fileOut = new FileOutputStream(classificationFilename);
			PrintWriter writeOut = new PrintWriter(fileOut);
			writeOut.println("\"category\",\"textscore\",\"textcount\",\"urlscore\",\"urlcount\"");
			
			//Need to merge the category sets from both text and URL classifications
			//in case one has a category the other does not have
	
			Set<String> categorySet = new HashSet<String>(textCategoryScores.keySet());
			categorySet.addAll(urlCategoryScores.keySet());
			
			//remove the useless categories
			//categorySet.remove(null); //you can have a null key? weird
			
			for(String category : categorySet) {				
				Double textscore = textCategoryScores.get(category);
				Integer textcount = textCategoryCounts.get(category);
				Double urlscore = urlCategoryScores.get(category);
				Integer urlcount = urlCategoryCounts.get(category);
				
				//If no score exists for a category, it has not appeared in a profile
				if(textscore == null) {
					textscore = new Double(0.0);
					textcount = new Integer(0);
				}
				if(urlscore == null) {
					urlscore = new Double(0.0);
					urlcount = new Integer(0);
				}
				
				writeOut.print("\""+category+"\",");
				writeOut.print(textscore+",");
				writeOut.print(textcount+",");
				writeOut.print(urlscore+",");
				writeOut.print(urlcount);
				writeOut.println();
			}
			writeOut.close();
			System.out.println("Saved CSV Textwise classification for "+userID);
			return true;
		} catch (IOException e) {
			System.out.println("Couldn't save CSV Textwise classification for "+userID);
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean classifyTextwiseProper() {
		
		System.out.println("Textwise classifying profile "+userID);
		
		//Check if classification already exists, to save time and allow resuming classification if something goes wrong
		String classificationFilename = "classifications/textwiseproper/"+userID+".csv";
		File classificationFile = new File(classificationFilename);
		if(classificationFile.exists()) {
			System.out.println("Textwise classification already exists for "+userID+", aborting");
			return true; //or do we want to return false...
		}
		
		//Concatenate tweets
		String concat = "";
		for(SimpleTweet tweet : tweets) {
			concat += tweet.getText();
		}
		
		Map<String,Double> classifications = new HashMap<String,Double>();
		Map<String,Integer> classificationsCount = new HashMap<String,Integer>();
		for(int i=0; i<concat.length()-500; i+=500) {
			TextwiseClassification textClassification;
			if(i+500>=concat.length()) textClassification = TextwiseClassifier.classify(concat.substring(i,concat.length()), false);
			else textClassification = TextwiseClassifier.classify(concat.substring(i,i+500), false);	
			Map<String,Double> scores = textClassification.getCategoryScores();
			for(String cat : textClassification.getCategories()) {
				if(classifications.containsKey(cat)) {
					classifications.put(cat,classifications.get(cat)+scores.get(cat));
					classificationsCount.put(cat,classificationsCount.get(cat)+1);
				} else {
					classifications.put(cat,scores.get(cat));
					classificationsCount.put(cat,1);
				}
			}
		}
		
		//Now save as a CSV Textwise classification file containing the scores
		try {
			FileOutputStream fileOut = new FileOutputStream(classificationFilename);
			PrintWriter writeOut = new PrintWriter(fileOut);
			writeOut.println("\"category\",\"score\",\"count\"");
			
			//remove the useless categories
			//categorySet.remove(null); //you can have a null key? weird
			for(String category : classifications.keySet()) {				
				writeOut.println("\""+category+"\","+classifications.get(category)+","+classificationsCount.get(category));
			}
			writeOut.close();
			System.out.println("Saved CSV Textwise classification for "+userID);
			return true;
		} catch (IOException e) {
			System.out.println("Couldn't save CSV Textwise classification for "+userID);
			e.printStackTrace();
			return false;
		}
		
	}
	
	//Concatenate all the tweets and topics
	//NOTE: This basically is only for LIWC - can be used later if we want individual tweet stuff
	public Document asDocument(String topicType) {
		if(topicType != null) {
			if(topicType.equals("alchemy")) { //TODO: Hacky...
				return asLLDADocument("alchemy");
			}
			if(topicType.equals("calais")) {
				return asLLDADocument("calais");
			}
			if(topicType.equals("textwise")) {
				return asLLDADocument("textwise");
			}
			if(topicType.equals("textwiseproper")) {
				return asLLDADocument("textwiseproper");
			}
			if(topicType.equals("liwc")) {
				return asLLDADocument("liwc");
			}
			if(topicType.equals("liwcnb")) {
				return asLLDADocument("liwcnb");
			}
		}
		
		
		//Concatenate all tweets
		String concat = "";
		for(SimpleTweet tweet : tweets) {
			concat += tweet.getText()+" ";
		}
		String strippedText = Tools.stripTweet(concat);
		Document document = new Document(strippedText, userID);
		return document;
	}
	
	public void reduceBy(int reduction) {
		if(reduction > 9) return;
		int newSize = (int)Math.ceil(tweets.size() * (1.0 - reduction/10.0));
		List<SimpleTweet> newTweets = new ArrayList<SimpleTweet>();
		Collections.shuffle(tweets);
		for(int i=0; i<newSize; i++) {
			newTweets.add(tweets.get(i));
		}
		tweets = newTweets;
	}
	
	//For use in Corpus' getFullProfileCorpus(topicType) method
	public Document asLLDADocument(String topicType) {
		Set<String> topics = new HashSet<String>();
		//Note: if not LIWC or LIWCNB, we have no topics yet!
		if(topicType.equals("alchemy")) {
			FullAlchemyClassification fac = new FullAlchemyClassification(userID);
			int topTopics = 3;
			//alchemy too sparse to threshold
			int count = 0;
			for(String topic : fac.getCategorySet()) {
				if(count == topTopics) break; //stop getting more than 3 topics
				//if(fac.getScore(topic) < scoreThreshold) break; //stop getting low-prob topics
				topics.add(topic);
				count++;
			}
		} else if(topicType.equals("calais")) {
			FullCalaisClassification fcc = new FullCalaisClassification(userID);
			int topTopics = 3;
			int count = 0;
			for(String topic : fcc.getCategorySet()) {
				if(topic.equals("Other")) continue; //really prominent...
				if(count == topTopics) break; //stop getting more than 3 topics
				//if(fac.getScore(topic) < scoreThreshold) break; //stop getting low-prob topics
				topics.add(topic);
				count++;
			}
		} else if(topicType.equals("textwise")) {
			FullTextwiseClassification ftc = new FullTextwiseClassification(userID,false);
			int topTopics = 3;
			int count = 0;
			for(String topic : ftc.getCategorySet()) {
				if(count == topTopics) break; //stop getting more than 3 topics
				//if(fac.getScore(topic) < scoreThreshold) break; //stop getting low-prob topics
				topics.add(topic);
				count++;
			}
		} else if(topicType.equals("textwiseproper")) {
			FullTextwiseClassification ftc = new FullTextwiseClassification(userID,true);
			int topTopics = 3;
			int count = 0;
			for(String topic : ftc.getCategorySet()) {
				if(count == topTopics) break; //stop getting more than 3 topics
				//if(fac.getScore(topic) < scoreThreshold) break; //stop getting low-prob topics
				topics.add(topic);
				count++;
			}
		}else if(topicType.equals("liwc")) {
			FullLIWCClassification flc = new FullLIWCClassification(false,userID);
			int topTopics = 3;
			int count = 0;
			for(String topic : flc.getCategorySet()) {
				if(count == topTopics) break;
				topics.add(topic);
				count++;
			}
		} else if(topicType.equals("liwcnb")) {
			FullLIWCClassification flc = new FullLIWCClassification(true,userID);
			int topTopics = 3;
			double threshold = 0.2;
			int count = 0;
			for(String topic : flc.getCategorySet()) {
				if(count == topTopics) break;
				if(flc.getScore(topic) < threshold) break;
				topics.add(topic);
				count++;
			}
		} else {
			System.out.println("invalid topic type");
			return null;
		}
		
		//Concatenate all tweets
		String concat = "";
		for(SimpleTweet tweet : tweets) {
			concat += tweet.getText()+" ";
		}
		String strippedText = Tools.stripTweet(concat);
		Document document = new Document(strippedText, userID, topics);
		return document;
	}
	
	public Document asDocument() {
		return asDocument(null);
	}
}
