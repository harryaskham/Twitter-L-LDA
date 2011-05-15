package uk.ac.cam.ha293.tweetlabel.topics;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

import uk.ac.cam.ha293.tweetlabel.types.Corpus;
import uk.ac.cam.ha293.tweetlabel.types.Document;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.SimpleLDA;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class SimpleMalletLDA implements Serializable {

        private static final long serialVersionUID = 7094157759480964151L;
        
        SimpleLDA LDA;
        Corpus corpus;
        int numTopics;
        int numIterations;
        
        public SimpleMalletLDA(Corpus corpus, int numTopics, int numIterations, double alpha, double beta) {
                this.corpus = corpus;
                this.numTopics = numTopics;
                this.numIterations = numIterations;
                LDA = new SimpleLDA(numTopics, alpha, beta);
        }
        
        public void setCorpus(Corpus corpus) {
                this.corpus = corpus;
        }
                
        public void evaluate() {
            Pipe pipe = buildPipe();
            InstanceList instances = new InstanceList(pipe);
            for(Document document : corpus.getDocuments()) {
                Instance instance = new Instance(document.getDocumentString(),null,null,document.getDocumentString());
                instance.setData(document.getDocumentString());
                instances.addThruPipe(instance);
            }
            LDA.addInstances(instances);
            try {
				LDA.sample(numIterations);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        public void print() {
            System.out.println(LDA.topWords(10));
        }

      //From MALLET website example
        //TODO: Need to do special tokenizing and parsing to account for @ and #
    public Pipe buildPipe() {
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Regular expression for what constitutes a token.
        //  This pattern includes Unicode letters, Unicode numbers, 
        //   and the underscore character. Alternatives:
        //    "\\S+"   (anything not whitespace)
        //    "\\w+"    ( A-Z, a-z, 0-9, _ )
        //    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
        //                                    a group of only punctuation marks)
        Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+|[\\p{P}]+");

        // Tokenize raw strings
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

        // Normalize all tokens to all lowercase
        pipeList.add(new TokenSequenceLowercase());

        // Remove stopwords from a standard English stoplist.
        //  options: [case sensitive] [mark deletions]
        pipeList.add(new TokenSequenceRemoveStopwords(false, false));

        // Rather than storing tokens as strings, convert 
        //  them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());

        // Do the same thing for the "target" field: 
        //  convert a class label string to a Label object,
        //  which has an index in a Label alphabet.
        //pipeList.add(new Target2Label());

        // Now convert the sequence of features to a sparse vector,
        //  mapping feature IDs to counts.
        //pipeList.add(new FeatureSequence2FeatureVector());
        //NOTE: Commented out because LDA wanted a FeatureSequence

        // Print out the features and the label
        pipeList.add(new PrintInputAndTarget());

        return new SerialPipes(pipeList);
    }                   
        
}