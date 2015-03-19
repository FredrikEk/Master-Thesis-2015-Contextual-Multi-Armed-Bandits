package com.mapr.bandit;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.joda.time.DateTime;

import com.mapr.objects.Category;
import com.mapr.objects.Item;
import com.mapr.objects.Order;
import com.mapr.objects.User;
import com.mapr.stats.bandit.ContextualBandit;
import com.mapr.stats.bandit.ContextualBayesArm;
import com.mapr.stats.bandit.ContextualSetting;

public class BanditHittepa2 {

    private static Random gen = RandomUtils.getRandom();
    
    public final static boolean useCategories = true;
    public final static boolean useGender = true;
    public final static boolean useAge = true;
    public final static boolean useLocation = true;
    public final static boolean usePopularity = true;
    // 771338 - 859594 3 months, 2012-05-01
    // 771338 - 777471, 1 week, 2012-05-01
 	public final static int TYPE_RANDOM_MATRIX = 0;
 	public final static int TYPE_NORMAL = 1;
 	public final static int TYPE_MOST_BUYS = 2;
    public final static int startPlace = 700000;
    public final static int numberOfTraining = 71338;
	public final static int trainingSet = startPlace + numberOfTraining;
	public final static int numberOfTests = 6133;
	public final static int orderToEndAt = trainingSet + numberOfTests;
	public final static int numberOfArms = 50;
	public final static int numberOfFeatures = 0 + (useGender 		? 2 : 0) + 
												   (useAge	 		? 3 : 0) + 
												   (useLocation 	? 1 : 0) + 
												   (usePopularity 	? 2 : 0) + 
												   (useCategories 	? Category.numberOfCategories : 0);
	public final static int extraFeatures = 10;
	public final static int debugOutPrint = 100000;
	public final static int itemsRecommendedPerTurn = 10;
	public final static boolean baseLine = false;
	
	public static ContextualBayesArm bestArm = null;
    
	static String[] csvFiles = {"data/JunkyardItems.csv", "data/JunkyardUser.csv", "data/JunkyardOrders.csv" };
	static String csvSplitBy = ",";
	
	public static PrintWriter logWriter = null;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		int TYPE = TYPE_NORMAL;
				
	 	long start1 = System.currentTimeMillis();
	 	String logFileName = "logs/logfile" + DateTime.now().toString().replaceAll("[^a-zA-Z0-9.-]", "_");
	 	
	 	ContextualSetting contextualSetting = null;
	 	
		try {
			logWriter = new PrintWriter(new File(logFileName + ".log"));
		    
			BufferedReader[] br = {new BufferedReader(new FileReader(csvFiles[0])), 
								   new BufferedReader(new FileReader(csvFiles[1])), 
								   new BufferedReader(new FileReader(csvFiles[2]))};
			
			contextualSetting = new ContextualSetting(br);
			
			br[0].close();
			br[1].close();
			br[2].close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch( Exception e ) {
			e.printStackTrace();
		}
		double[][] randomizedMatrix = new double[extraFeatures][numberOfFeatures];
		
		System.out.println("");
		System.out.println("Startup information:");
		System.out.println("First order in training stage: " + startPlace);
		System.out.println("First order in evaluation stage: " + trainingSet);
		System.out.println("Last order in evaluation stage: " + orderToEndAt);
		System.out.println("");
		System.out.println("Number of orders in training stage: " + numberOfTraining);
		System.out.println("Number of orders in evaluation stage: " + numberOfTests);
		System.out.println("");
		
		logWriter.println("");
		logWriter.println("Startup information:");
		logWriter.println("First order in training stage: " + startPlace);
		logWriter.println("First order in evaluation stage: " + trainingSet);
		logWriter.println("Last order in evaluation stage: " + orderToEndAt);
		logWriter.println("");
		logWriter.println("Number of orders in training stage: " + numberOfTraining);
		logWriter.println("Number of orders in evaluation stage: " + numberOfTests);
		logWriter.println("");
		
		
		ArrayList<XYSeries> allSeries = new ArrayList<XYSeries>();
		
		if(TYPE == TYPE_RANDOM_MATRIX) {
			for(int i = 0; i < extraFeatures; i++) {
				for (int j = 0; j < numberOfFeatures; j++) {
					randomizedMatrix[i][j] = gen.nextDouble();
				}
			}
			
			Matrix characterMatrix = new DenseMatrix(randomizedMatrix);
			
			ContextualBandit cb = new ContextualBandit(numberOfArms, extraFeatures);
			
			for(int i = startPlace; i < trainingSet; i++) {
				Order o = contextualSetting.getOrderByDate(i);
				List<Item> itemList = o.getItems();
				for(Item item : itemList) {
					item.buy(o);
				}
			}
			
			XYSeries seriesAll = testMatrixArm("All arms", cb, contextualSetting, characterMatrix);

			Item.mostPopularMonthly = new ArrayList<Item>();
			Item.mostPopularWeekly = new ArrayList<Item>();
			
			allSeries.add(seriesAll);
			
			ContextualSetting contextualSettingEvaluation = contextualSetting.copy();
			
			ArrayList<ContextualBayesArm> bestArms = new ArrayList<ContextualBayesArm>();
			bestArms.add(bestArm.copy());
			ContextualBandit cbEvaluation = new ContextualBandit(bestArms);
			
			for(int i = startPlace; i < trainingSet; i++) {
				Order o = contextualSettingEvaluation.getOrderByDate(i);
				List<Item> itemList = o.getItems();
				for(Item item : itemList) {
					item.buy(o);
				}
			}
			
			XYSeries seriesReference = testMatrixArm("Best arm", cbEvaluation, contextualSettingEvaluation, characterMatrix);

			Item.mostPopularMonthly = new ArrayList<Item>();
			Item.mostPopularWeekly = new ArrayList<Item>();
			
			allSeries.add(seriesReference);
		} else {
			ContextualBandit cb;
			
			cb = new ContextualBandit(numberOfArms, numberOfFeatures);		
			
			for(int i = startPlace; i < trainingSet; i++) {
				Order o = contextualSetting.getOrderByDate(i);
				List<Item> itemList = o.getItems();
				for(Item item : itemList) {
					item.buy(o);
				}
			}
			
			XYSeries seriesAll = testVectorArm("All arms", cb, contextualSetting, TYPE);

			allSeries.add(seriesAll);

			Item.mostPopularMonthly = new ArrayList<Item>();
			Item.mostPopularWeekly = new ArrayList<Item>();
			
			ContextualSetting contextualSettingEvaluation = contextualSetting.copy();
			
			ArrayList<ContextualBayesArm> bestArms = new ArrayList<ContextualBayesArm>();
			bestArms.add(bestArm.copy());
			ContextualBandit cbEvaluation = new ContextualBandit(bestArms);
			
			
			for(int i = startPlace; i < trainingSet; i++) {
				Order o = contextualSettingEvaluation.getOrderByDate(i);
				List<Item> itemList = o.getItems();
				for(Item item : itemList) {
					item.buy(o);
				}
			}
			
			XYSeries seriesReference = testVectorArm("Best arm", cbEvaluation, contextualSettingEvaluation, TYPE);

			Item.mostPopularMonthly = new ArrayList<Item>();
			Item.mostPopularWeekly = new ArrayList<Item>();
			
			allSeries.add(seriesReference);
		}
		
		if(baseLine || TYPE == TYPE_MOST_BUYS) {
			ContextualBandit cbBaseline = new ContextualBandit(1, 1);
			
			ContextualSetting contextualSettingBaseline = contextualSetting.copy();
			
			for(int i = startPlace; i < trainingSet; i++) {
				Order o = contextualSettingBaseline.getOrderByDate(i);
				List<Item> itemList = o.getItems();
				for(Item item : itemList) {
					item.buy(o);
				}
			}
			
			XYSeries seriesReference = testVectorArm("Baseline (most bought last month)", cbBaseline, contextualSettingBaseline, TYPE_MOST_BUYS);

			Item.mostPopularMonthly = new ArrayList<Item>();
			Item.mostPopularWeekly = new ArrayList<Item>();
			
			allSeries.add(seriesReference);
		}
		
		savePlot(allSeries, logFileName);
		
		System.out.println((System.currentTimeMillis() - start1) / 1000);
		logWriter.println((System.currentTimeMillis() - start1) / 1000 + "");
		logWriter.close();
	  }
		
	
	private static void plot(ArrayList<XYSeries> allSeries) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for(XYSeries series : allSeries) {
        	dataset.addSeries(series);
        }
        NumberAxis domain = new NumberAxis("Number of tries");
        NumberAxis range = new NumberAxis("Correct predictions (%)");
        XYSplineRenderer r = new XYSplineRenderer(3);
        XYPlot xyplot = new XYPlot(dataset, domain, range, r);
        JFreeChart chart = new JFreeChart(xyplot);
        ChartPanel chartPanel = new ChartPanel(chart){

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1920, 1080);
            }
        };
        JFrame frame = new JFrame("Bandit Evaluation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chartPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
	
	private static void savePlot(ArrayList<XYSeries> allSeries, String fileName) {
		XYSeriesCollection dataset = new XYSeriesCollection();
        for(XYSeries series : allSeries) {
        	dataset.addSeries(series);
        }
        NumberAxis domain = new NumberAxis("Number of tries");
        NumberAxis range = new NumberAxis("Correct predictions (%)");
        DateAxis dateAxis = new DateAxis("Time zone");
        
        XYSplineRenderer r = new XYSplineRenderer(3);
        XYPlot xyplot = new XYPlot(dataset, domain, range, r);
        JFreeChart chart = new JFreeChart(xyplot);
        writeAsPNG(chart, 1920, 1080, fileName);
	}
	
	public static void writeAsPNG( JFreeChart chart, int width, int height, String fileName ) 
	{ 
		try { 
	        OutputStream out = new FileOutputStream(fileName + ".png");
			BufferedImage chartImage = chart.createBufferedImage( width, height, null); 
			ImageIO.write( chartImage, "png", out ); 
			out.close();
		} 
		catch (Exception e) {
			
		} 
	}
	
	public static XYSeries testMatrixArm(String plotName, ContextualBandit cb, ContextualSetting contextualSetting, Matrix characterMatrix) {
		
		String debugString = "";

		XYSeries series = new XYSeries(plotName);
		int numberOfSuccess = 0;
		try {
			for(int i = trainingSet; i < orderToEndAt; i++) {
				boolean debug = i % 1000 == 0;
				boolean evaluate = i % 100 == 0;
				TreeMap<Double, ArrayList<Long>> sortedMap = new TreeMap<Double, ArrayList<Long>>();
				Order currentOrder = contextualSetting.getOrderByDate(i);
				User u = currentOrder.getUser();
				ContextualBayesArm cba = cb.getArm();
				
				debugString = currentOrder.getOrderId() + "";
				
				Vector userContext;
				userContext = cba.getContext().times(u.getUserContextVector(characterMatrix, currentOrder.getPlacedOrder()));
				
				Set<Long> itemKeys = contextualSetting.getItemKeySet();
				int zipcode = u.getZipCode(currentOrder.getPlacedOrder());
				for(Long key : itemKeys) {
					double result = 0;
					Vector itemVec;
					itemVec = contextualSetting.getItem(key).getUserContextVector(characterMatrix, currentOrder.getPlacedOrder());
					
					for(int j = 0; j < userContext.size(); j++) {
						result += userContext.get(j) * itemVec.get(j);
					}
					if(sortedMap.containsKey(result)) {
						sortedMap.get(result).add(key);
					} else {
						ArrayList<Long> newKeyList = new ArrayList<Long>();
						newKeyList.add(key);
						sortedMap.put(result, newKeyList);	
					}
				}
				
				if(debug) {
					System.out.println("The current arm is " + cba.getArmNumber() + " with vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta());
					System.out.println("The order information:");
					System.out.println("Date: " + currentOrder.getPlacedOrder().toString());
					System.out.println("It predicted these items for order " + currentOrder.getOrderId() + " :");
					
					logWriter.println("The current arm is " + cba.getArmNumber() + " with vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta());
					logWriter.println("The order information:");
					logWriter.println("Date: " + currentOrder.getPlacedOrder().toString());
					logWriter.println("It predicted these items for order " + currentOrder.getOrderId() + " :");
				}

				boolean success = false;
				for(int j = 0; j < itemsRecommendedPerTurn; ) {
					double key = sortedMap.lastKey();
					ArrayList<Long> itemKeys2 = sortedMap.remove(key);
					for(Long itemKey : itemKeys2) {
						Item item = contextualSetting.getItem(itemKey);
						if(currentOrder.getItems().contains(item)) success = true;
						if(debug) {
							System.out.println("   " + item.getProductId() + " with score of " + key);
							logWriter.println("   " + item.getProductId() + " with score of " + key);
						}
						j++;
						if(!(j < itemsRecommendedPerTurn)) break;
					}
				}
				
				cba.train(success);
				if(success) numberOfSuccess++;
				if(debug) {
					System.out.println("These are the actual items bought: ");	
					logWriter.println("These are the actual items bought: ");	
				}
				for(Item item : currentOrder.getItems()) {
					item.buy(currentOrder);
					if(debug) {
						System.out.println("   " + item.getProductId());
						logWriter.println("   " + item.getProductId());
					}
				}
				if(evaluate) {
					series.add(i - trainingSet, (((double) numberOfSuccess)/((double) i - trainingSet))*100 );
				}
			}
		}
		catch (Exception e) {
			System.out.println(debugString);
			logWriter.println(debugString);
			e.printStackTrace();
		}
		int numberOfRightGuesses = 0;
		ContextualBayesArm thisRunBestArm = null;
		for(ContextualBayesArm cba : cb.getAllArms()) {
			numberOfRightGuesses += cba.getNumberOfBuys();
			System.out.println("The arm " + cba.getArmNumber() + " has vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta() + ", percentage " + ((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries())*100);
			logWriter.println("The arm " + cba.getArmNumber() + " has vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta() + ", percentage " + ((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries())*100);
			if(thisRunBestArm == null) {
				thisRunBestArm = cba;
			}
			if(((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries()) > ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())) {
				thisRunBestArm = cba;
			}
		}
		
		System.out.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		logWriter.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		
		if(bestArm == null || ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries()) > ((double)bestArm.getNumberOfBuys()/(double)bestArm.getNumberOfTries())) {
			bestArm = thisRunBestArm;
		}
		System.out.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		
		System.out.println("The right guesses are: " + numberOfRightGuesses + " which means " + (((double) numberOfRightGuesses)/((double) numberOfTests)*100) + "% correct");
		System.out.println("Done");
		
		logWriter.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		
		logWriter.println("The right guesses are: " + numberOfRightGuesses + " which means " + (((double) numberOfRightGuesses)/((double) numberOfTests)*100) + "% correct");
		logWriter.println("Done");
		
		return series;
	}
	
	public static XYSeries testVectorArm(String plotName, ContextualBandit cb, ContextualSetting contextualSetting, int TYPE) {
		String debugString = "";

		XYSeries series = new XYSeries(plotName);
		int numberOfSuccess = 0;
		try {
			for(int i = trainingSet; i < orderToEndAt; i++) {
				boolean debug = i % 1000 == 0;
				boolean evaluate = i % 100 == 0;
				TreeMap<Double, ArrayList<Long>> sortedMap = new TreeMap<Double, ArrayList<Long>>();
				Order currentOrder = contextualSetting.getOrderByDate(i);
				User u = currentOrder.getUser();
				ContextualBayesArm cba = cb.getArm();
				
				debugString = currentOrder.getOrderId() + "";
				
				Vector userContext;
				if(TYPE == TYPE_NORMAL) {
					userContext = cba.getContext().times(u.getContextVector(currentOrder.getPlacedOrder()));
				} else {
					// Timefocus only
					userContext = new DenseVector(new double[]{1.0});
				}
				Set<Long> itemKeys = contextualSetting.getItemKeySet();
				int zipcode = u.getZipCode(currentOrder.getPlacedOrder());
				for(Long key : itemKeys) {
					double result = 0;
					Vector itemVec;
					if(TYPE == TYPE_NORMAL) {
						itemVec = contextualSetting.getItem(key).getContextVector(zipcode, currentOrder.getPlacedOrder());
					} else {
						// Timefocus only
						itemVec = contextualSetting.getItem(key).getNumberOfBuysInLastMonth(currentOrder.getPlacedOrder());
					}
					for(int j = 0; j < userContext.size(); j++) {
						result += userContext.get(j) * itemVec.get(j);
					}
					if(sortedMap.containsKey(result)) {
						sortedMap.get(result).add(key);
					} else {
						ArrayList<Long> newKeyList = new ArrayList<Long>();
						newKeyList.add(key);
						sortedMap.put(result, newKeyList);	
					}
				}
				
				if(debug) {
					System.out.println("The current arm is " + cba.getArmNumber() + " with vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta());
					System.out.println("The order information:");
					System.out.println("Date: " + currentOrder.getPlacedOrder().toString());
					System.out.println("It predicted these items for order " + currentOrder.getOrderId() + " :");
					
					logWriter.println("The current arm is " + cba.getArmNumber() + " with vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta());
					logWriter.println("The order information:");
					logWriter.println("Date: " + currentOrder.getPlacedOrder().toString());
					logWriter.println("It predicted these items for order " + currentOrder.getOrderId() + " :");
				}

				boolean success = false;
				for(int j = 0; j < itemsRecommendedPerTurn; ) {
					double key = sortedMap.lastKey();
					ArrayList<Long> itemKeys2 = sortedMap.remove(key);
					for(Long itemKey : itemKeys2) {
						Item item = contextualSetting.getItem(itemKey);
						if(currentOrder.getItems().contains(item)) success = true;
						if(debug) {
							System.out.println("   " + item.getProductId() + " with score of " + key);
							logWriter.println("   " + item.getProductId() + " with score of " + key);
						}
						j++;
						if(!(j < itemsRecommendedPerTurn)) break;
					}
				}
				
				cba.train(success);
				if(success) numberOfSuccess++;
				if(debug) {
					System.out.println("These are the actual items bought: ");	
					logWriter.println("These are the actual items bought: ");	
				}
				for(Item item : currentOrder.getItems()) {
					item.buy(currentOrder);
					if(debug) {
						System.out.println("   " + item.getProductId());
						logWriter.println("   " + item.getProductId());
					}
				}
				if(evaluate) {
					series.add(i - trainingSet, (((double) numberOfSuccess)/((double) i - trainingSet))*100 );
				}
			}
		}
		catch (Exception e) {
			System.out.println(debugString);
			logWriter.println(debugString);
			
			e.printStackTrace();
		}
		int numberOfRightGuesses = 0;
		ContextualBayesArm thisRunBestArm = null;
		for(ContextualBayesArm cba : cb.getAllArms()) {
			numberOfRightGuesses += cba.getNumberOfBuys();
			System.out.println("The arm " + cba.getArmNumber() + " has vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta() + ", percentage " + ((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries())*100);
			logWriter.println("The arm " + cba.getArmNumber() + " has vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta() + ", percentage " + ((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries())*100);
			if(thisRunBestArm == null) {
				thisRunBestArm = cba;
			}
			if(((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries()) > ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())) {
				thisRunBestArm = cba;
			}
		}
		
		System.out.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		logWriter.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		
		if(bestArm == null || ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries()) > ((double)bestArm.getNumberOfBuys()/(double)bestArm.getNumberOfTries())) {
			bestArm = thisRunBestArm;
		}
		
		System.out.println("The right guesses are: " + numberOfRightGuesses + " which means " + (((double) numberOfRightGuesses)/((double) numberOfTests)*100) + "% correct");
		System.out.println("Done");
		logWriter.println("The right guesses are: " + numberOfRightGuesses + " which means " + (((double) numberOfRightGuesses)/((double) numberOfTests)*100) + "% correct");
		logWriter.println("Done");
		
		return series;
	}
	
}
