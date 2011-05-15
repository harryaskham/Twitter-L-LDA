package uk.ac.cam.ha293.tweetlabel.eval;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.*;

import org.jCharts.axisChart.*;
import org.jCharts.chartData.*;
import org.jCharts.chartData.interfaces.IScatterPlotDataSet;
import org.jCharts.encoders.PNGEncoder;
import org.jCharts.properties.*;
import org.jCharts.test.*;
import org.jCharts.types.ChartType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import uk.ac.cam.ha293.tweetlabel.util.Tools;

import javax.imageio.*;

public class Grapher {

	private String title;
	private List<Double> xVals;
	private List<Double> yVals;
	private String xLabel;
	private String yLabel;
	JFreeChart chart;
	
	public Grapher() {
		title = "";
		xVals = new ArrayList<Double>();
		yVals = new ArrayList<Double>();
		xLabel = "";
		yLabel = "";
		chart = null;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void addPoint(double x, double y) {
		xVals.add(x);
		yVals.add(y);
	}
	
	public void setLabels(String xLabel, String yLabel) {
		this.xLabel = xLabel;
		this.yLabel = yLabel;
	}
	
	public void makeScatter() {
		DefaultXYDataset dataset = new DefaultXYDataset();
		double[][] data = new double[2][xVals.size()];
		for(int i=0; i<xVals.size(); i++) {
			data[0][i] = yVals.get(i);
			data[1][i] = xVals.get(i);
		}
		dataset.addSeries("SeriesName", data);
		chart = ChartFactory.createScatterPlot(title, yLabel, xLabel, dataset, PlotOrientation.HORIZONTAL, false,false,false);
		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		renderer.setSeriesShape(0, new java.awt.Rectangle(0,0,1,1));
	}
	
	public void addSimilarityPoints(SimilarityMatrix sm1, SimilarityMatrix sm2, double portion) {
		for(int i=0; i<sm1.dimension(); i++) {
			for(int j=i; j<sm1.dimension(); j++) {
				if(Math.random()<portion) addPoint(sm1.getIndex(i, j),sm2.getIndex(i, j));
			}
		}
	}
	
	public void save(String name) {
		BufferedImage chartImage = chart.createBufferedImage(500,300);
		try {
			ImageIO.write(chartImage, "png", new File("graphs/"+name+".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void saveHD(String name) {
		BufferedImage chartImage = chart.createBufferedImage(1000,600);
		try {
			ImageIO.write(chartImage, "png", new File("graphs/hd/"+name+".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public static void lldaGraphRoutine() {
		//lldas vs ground truths all-pairs similarities
		String[] topicTypes = {"alchemy","calais","textwise"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		double[] portions = {0.001,0.005,0.01};
		//double[] portions = {0.1};
		for(String topicType : topicTypes) {
			for(double alpha : alphas) {
				for(double portion : portions) {
					Grapher g = new Grapher();
					g.setTitle(topicType+" Ground Truth vs "+topicType+" LLDA("+alpha+") Inferred Profile Cosine Similarities");
					g.setLabels("Ground Truth Cosine Similarities", "Inferred Cosine Similarities");
					SimilarityMatrix sm1 = SimilarityMatrix.load(topicType);
					SimilarityMatrix sm2 = SimilarityMatrix.load("llda-"+topicType+"-"+alpha);
					g.addSimilarityPoints(sm1, sm2, portion);
					g.makeScatter();
					//g.save(topicType+"-gt-llda-"+alpha+"-"+portion);
					g.saveHD(topicType+"-gt-llda-"+alpha+"-"+portion);
					System.out.println("Saved graph "+topicType+"-gt-llda-"+alpha+"-"+portion);
				}
			}
		}
	}
	
	public static void diversityGraphRoutine() {

		String[] topicTypes = {"alchemy","calais","textwise"};
		String[] diversityTypes = {"simpson","shannon"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		/*
		//baselines simpson vs shannon - worthwhile?
		for(String topicType : topicTypes) {
			System.out.println("Making baseline graph for "+topicType);
			Grapher g = new Grapher();
			g.setTitle("Simpson vs Shannon Diversity, "+topicType+" Baseline");
			g.setLabels("Simpson Diversity", "Shannon Diversity");
			Map<Long,Double> simpson = Diversity.loadDiversities(topicType, "simpson");
			Map<Long,Double> shannon = Diversity.loadDiversities(topicType, "shannon");
			for(long uid : simpson.keySet()) {
				g.addPoint(simpson.get(uid), shannon.get(uid));
			}
			g.makeScatter();
			g.save("diversity/baseline-"+topicType+"-simpson-vs-shannon");
			g.saveHD("diversity/baseline-"+topicType+"-simpson-vs-shannon");		
		}
		
		//baseline vs llda simpson
		for(String topicType : topicTypes) {
			for(String diversityType : diversityTypes) {
				for(double alpha : alphas) {
					System.out.println("Making baseline/llda graph for "+topicType+" "+alpha+" "+diversityType);
					Grapher g = new Grapher();
					g.setTitle(topicType+" Baseline vs LLDA("+alpha+") "+diversityType+" Diversity");
					g.setLabels("Baseline "+diversityType+" Diversity", "Inferred "+diversityType+" Diversity");
					Map<Long,Double> baseline = Diversity.loadDiversities(topicType, diversityType);
					Map<Long,Double> llda = Diversity.loadDiversities(topicType, alpha, diversityType);
					for(long uid : baseline.keySet()) {
						g.addPoint(baseline.get(uid), llda.get(uid));
					}
					g.makeScatter();
					g.save("diversity/baseline-"+topicType+"-"+alpha+"-"+diversityType);
					g.saveHD("diversity/baseline-"+topicType+"-"+alpha+"-"+diversityType);	
				}
			}
		}
		*/
		
		//llda diversities vs cosines
		for(String topicType : topicTypes) {
			for(String diversityType : diversityTypes) {
				for(double alpha : alphas) {
					System.out.println("Making llda diversity/cosine graph for "+topicType+" "+alpha+" "+diversityType);
					Grapher g = new Grapher();
					g.setTitle(topicType+" LLDA("+alpha+") Cosine Similarity to Baseline vs "+diversityType+" Diversity");
					g.setLabels("Baseline "+diversityType+" Diversity", "Inferred Cosine Similarity to Baseline");
					Map<Long,Double> baseline = Diversity.loadDiversities(topicType, diversityType);
					Map<Long,Double> cosines = CosineManager.loadCosineSimilarities(topicType, alpha);
					for(long uid : baseline.keySet()) {
						g.addPoint(baseline.get(uid), cosines.get(uid));
					}
					g.makeScatter();
					g.save("diversity/cosine/baseline-"+diversityType+"-vs-"+topicType+"-"+alpha+"-cosine");
					g.saveHD("diversity/cosine/baseline-"+diversityType+"-vs-"+topicType+"-"+alpha+"-cosine");	
				}
			}
		}
	}
	
}
