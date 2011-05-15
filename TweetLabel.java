package uk.ac.cam.ha293.tweetlabel;

import uk.ac.cam.ha293.tweetlabel.classify.AlchemyClassifier;
import uk.ac.cam.ha293.tweetlabel.classify.CalaisClassifier;
import uk.ac.cam.ha293.tweetlabel.classify.TextwiseClassifier;

public class TweetLabel {
	
	public static void init() {
		Tools.init();
		AlchemyClassifier.init();
		CalaisClassifier.init();
		TextwiseClassifier.init();
	}
	
	public static void main(String args[]) {	
		TweetLabel.init();
		//Place API calls here to do topic modeling
		//...
		//eg:
		/*
		Corpus corpus = Corpus.loadLabelled("alchemy", "allprofiles-unstemmed-alchemy-top3");
		LLDATopicModel llda = new LLDATopicModel(corpus,1000,100,0,1,0.01);
		llda.runCVGibbsSampling(0, 9);
		llda.printDocumentsVerbose(10);
		*/
		//Otherwise, warn user application is currently doing nothing:
		System.out.println("Application not configured to perform topic modeling.");

	}
	
}
