package com.mapr.bandit;

import java.awt.Dimension;
import java.awt.Font;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import JaccardSimilarity.JaccardDistanceAlgorithm;

import com.mapr.objects.ConstantHolder;
import com.mapr.objects.Item;
import com.mapr.objects.Order;
import com.mapr.objects.User;
import com.mapr.stats.bandit.ContextualBandit;
import com.mapr.stats.bandit.ContextualBayesArm;
import com.mapr.stats.bandit.ContextualSetting;

public class BanditHittepa3 {

    private static Random gen = RandomUtils.getRandom();
    
	public static ContextualBayesArm bestArm = null;
    
	static String[] csvFiles = {"data/JunkyardItems.csv", "data/JunkyardUser.csv", "data/JunkyardOrders.csv" };
	static String csvSplitBy = ",";
	
	public static ArrayList<User> userBought = new ArrayList<User>();
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		//int TYPE = TYPE_JACCARD_DISTANCE | TYPE_NORMAL | TYPE_MOST_BUYS;
		ConstantHolder.initialize();
		
		long start1 = System.currentTimeMillis();
	 	String logFileName = "logs/logfile" + DateTime.now().toString().replaceAll("[^a-zA-Z0-9.-]", "_");
		
	 	ConstantHolder.plotWriter.println("test");
		
	 	ContextualSetting contextualSetting = null;
	 	
		try {
			    
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
		double[][] randomizedMatrix = new double[ConstantHolder.extraFeatures][ConstantHolder.numberOfFeatures];
		
		System.out.println("");
		System.out.println("Startup information:");
		System.out.println("First order in training stage: " + ConstantHolder.startPlace);
		System.out.println("First order in evaluation stage: " + ConstantHolder.trainingSet);
		System.out.println("Last order in evaluation stage: " + ConstantHolder.orderToEndAt);
		System.out.println("");
		System.out.println("Number of orders in training stage: " + ConstantHolder.numberOfTraining);
		System.out.println("Number of orders in evaluation stage: " + ConstantHolder.numberOfTests);
		System.out.println("");
		
		ConstantHolder.logWriter.println("");
		ConstantHolder.logWriter.println("Startup information:");
		ConstantHolder.logWriter.println("First order in training stage: " + ConstantHolder.startPlace);
		ConstantHolder.logWriter.println("First order in evaluation stage: " + ConstantHolder.trainingSet);
		ConstantHolder.logWriter.println("Last order in evaluation stage: " + ConstantHolder.orderToEndAt);
		ConstantHolder.logWriter.println("");
		ConstantHolder.logWriter.println("Number of orders in training stage: " + ConstantHolder.numberOfTraining);
		ConstantHolder.logWriter.println("Number of orders in evaluation stage: " + ConstantHolder.numberOfTests);
		ConstantHolder.logWriter.println("");
		
		
		ArrayList<HashMap<Integer, Double>> allSeries = new ArrayList<HashMap<Integer, Double>>();
		HashMap<Integer, Double> bestArmSeries = null;
		ArrayList<XYSeries> allSeriesPlot = new ArrayList<XYSeries>();
		
		if((ConstantHolder.TYPE & ConstantHolder.TYPE_RANDOM_MATRIX) > 0) {
			for(int i = 0; i < ConstantHolder.extraFeatures; i++) {
				for (int j = 0; j < ConstantHolder.numberOfFeatures; j++) {
					randomizedMatrix[i][j] = gen.nextDouble();
				}
			}
			
			ContextualSetting contextualSettingMatrix = contextualSetting.copy();
			
			Matrix characterMatrix = new DenseMatrix(randomizedMatrix);
			
			ContextualBandit cb = new ContextualBandit(ConstantHolder.numberOfArms, ConstantHolder.extraFeatures);
			
			for(int i = ConstantHolder.startPlace; i < ConstantHolder.trainingSet; i++) {
				Order o = contextualSettingMatrix.getOrderByDate(i);
				List<Item> itemList = o.getItems();
				for(Item item : itemList) {
					item.buy(o);
				}
			}
			
			HashMap<Integer, Double> seriesAll = testMatrixArm("All arms", cb, contextualSettingMatrix, characterMatrix);

			Item.mostPopularMonthly = new ArrayList<Item>();
			Item.mostPopularWeekly = new ArrayList<Item>();
			
			allSeries.add(seriesAll);
			
			ContextualSetting contextualSettingEvaluation = contextualSettingMatrix.copy();
			
			ArrayList<ContextualBayesArm> bestArms = new ArrayList<ContextualBayesArm>();
			bestArms.add(bestArm.copy());
			ContextualBandit cbEvaluation = new ContextualBandit(bestArms);
			
			for(int i = ConstantHolder.startPlace; i < ConstantHolder.trainingSet; i++) {
				Order o = contextualSettingEvaluation.getOrderByDate(i);
				List<Item> itemList = o.getItems();
				for(Item item : itemList) {
					item.buy(o);
				}
			}
			
			HashMap<Integer, Double> seriesReference = testMatrixArm("Best arm", cbEvaluation, contextualSettingEvaluation, characterMatrix);
			
			Item.mostPopularMonthly = new ArrayList<Item>();
			Item.mostPopularWeekly = new ArrayList<Item>();
			
			allSeries.add(seriesReference);
			
			XYSeries xys = new XYSeries("All arms");
			for(Integer i : seriesAll.keySet()) {
				xys.add((double) i, 1 - (seriesAll.get(i) / bestArmSeries.get(i)));
			}
			allSeriesPlot.add(xys);
		}
		
		
		if((ConstantHolder.TYPE & ConstantHolder.TYPE_NORMAL) > 0){
			ContextualBandit cb;
			
			Item.mostPopularMonthly = new ArrayList<Item>();
			Item.mostPopularWeekly = new ArrayList<Item>();
			
			ContextualSetting contextualSettingVector = contextualSetting.copy();
			
			cb = new ContextualBandit(ConstantHolder.numberOfArms, ConstantHolder.numberOfFeatures);		
			
			for(int i = ConstantHolder.startPlace; i < ConstantHolder.trainingSet; i++) {
				Order o = contextualSettingVector.getOrderByDate(i);
				List<Item> itemList = o.getItems();
				for(Item item : itemList) {
					item.buy(o);
				}
			}
			
			HashMap<Integer, Double> seriesAll = testVectorArm("All arms", cb, contextualSettingVector, ConstantHolder.TYPE);

			allSeries.add(seriesAll);

			Item.mostPopularMonthly = new ArrayList<Item>();
			Item.mostPopularWeekly = new ArrayList<Item>();
			
			ContextualSetting contextualSettingEvaluation = contextualSettingVector.copy();
			
			ArrayList<ContextualBayesArm> bestArms = new ArrayList<ContextualBayesArm>();
			bestArms.add(bestArm.copy());
			ContextualBandit cbEvaluation = new ContextualBandit(bestArms);
			
			
			for(int i = ConstantHolder.startPlace; i < ConstantHolder.trainingSet; i++) {
				Order o = contextualSettingEvaluation.getOrderByDate(i);
				List<Item> itemList = o.getItems();
				for(Item item : itemList) {
					item.buy(o);
				}
			}
			
			HashMap<Integer, Double> seriesReference = testVectorArm("Best arm", cbEvaluation, contextualSettingEvaluation, ConstantHolder.TYPE);
			bestArmSeries = seriesReference;
			
			Item.mostPopularMonthly = new ArrayList<Item>();
			Item.mostPopularWeekly = new ArrayList<Item>();
			
			allSeries.add(seriesReference);
			
			
			XYSeries xys = new XYSeries("All arms");
			for(Integer i : seriesAll.keySet()) {
				xys.add((double) i, 1 - (seriesAll.get(i) / bestArmSeries.get(i)));
			}
			allSeriesPlot.add(xys);
		}
		if((ConstantHolder.TYPE & ConstantHolder.TYPE_JACCARD_DISTANCE) > 0) {
			
			ContextualSetting contextualSettingJaccard = contextualSetting.copy();
			
			for(int i = ConstantHolder.startPlace; i < ConstantHolder.trainingSet; i++) {
				Order o = contextualSettingJaccard.getOrderByDate(i);
				List<Item> itemList = o.getItems();
				User u = o.getUser();
				for(Item item : itemList) {
					item.buy(o);
				}
				if(!userBought.contains(u)) userBought.add(u);
			}
			
			HashMap<Integer, Double> seriesJaccard = testJaccardSimilarity("Jaccard Similarity", contextualSettingJaccard);
			
			allSeries.add(seriesJaccard);
			
			
			XYSeries xys = new XYSeries("Jaccard Distance");
			for(Integer i : seriesJaccard.keySet()) {
				xys.add((double) i, 1 - (seriesJaccard.get(i) / bestArmSeries.get(i)));
			}
			allSeriesPlot.add(xys);
		}
		if(ConstantHolder.baseLine || (ConstantHolder.TYPE & ConstantHolder.TYPE_MOST_BUYS) > 0) {
			ContextualBandit cbBaseline = new ContextualBandit(1, 1);
			
			ContextualSetting contextualSettingBaseline = contextualSetting.copy();
			
			for(int i = ConstantHolder.startPlace; i < ConstantHolder.trainingSet; i++) {
				Order o = contextualSettingBaseline.getOrderByDate(i);
				List<Item> itemList = o.getItems();
				for(Item item : itemList) {
					item.buy(o);
				}
			}
			
			HashMap<Integer, Double> seriesReference = testVectorArm("Baseline (most bought last week)", cbBaseline, contextualSettingBaseline, ConstantHolder.TYPE_MOST_BUYS);

			Item.mostPopularMonthly = new ArrayList<Item>();
			Item.mostPopularWeekly = new ArrayList<Item>();
			
			allSeries.add(seriesReference);
			
			
			XYSeries xys = new XYSeries("Most buys last week");
			for(Integer i : seriesReference.keySet()) {
				xys.add((double) i, 1 - (seriesReference.get(i) / bestArmSeries.get(i)));
			}
			allSeriesPlot.add(xys);
		}
		
		/*
		String[] names = {"All arms", "Jaccard Similarity", "Most buys last week"};
		int k = 0;
		
		for(HashMap<Integer, Double> hashMap : allSeries) {
			if(hashMap != bestArmSeries) {
				XYSeries xys = new XYSeries(names[k]);
				for(Integer i : hashMap.keySet()) {
					xys.add((double) i, 1 - (hashMap.get(i) / bestArmSeries.get(i)));
				}
				allSeriesPlot.add(xys);
				k++;
			}
		}
		*/
		
		//savePlot(allSeriesPlot, logFileName);
		System.out.println((System.currentTimeMillis() - start1) / 1000);
		ConstantHolder.logWriter.println((System.currentTimeMillis() - start1) / 1000 + "");
		ConstantHolder.logWriter.close();
		savePlotToText(allSeries);
	}
		
	
	private static void plot(ArrayList<XYSeries> allSeries) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for(XYSeries series : allSeries) {
        	dataset.addSeries(series);
        }
        NumberAxis domain = new NumberAxis("Number of tries");
        NumberAxis range = new NumberAxis("Regret (%)");
        XYSplineRenderer r = new XYSplineRenderer(3);
        XYPlot xyplot = new XYPlot(dataset, domain, range, r);
        Font font3 = new Font("Dialog", Font.PLAIN, 25);
        xyplot.getDomainAxis().setLabelFont(font3); // Increase size of "Number of tries"
        xyplot.getRangeAxis().setLabelFont(font3); // Increase size of "Correct predicitons(%)"
        xyplot.getDomainAxis().setTickLabelFont(font3); // Increase size of "Number-of-tries-ticks"
        xyplot.getRangeAxis().setLabelFont(font3); // Increase size of "Correct-predictions(%)-ticks"
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
        NumberAxis range = new NumberAxis("Regret in relation to best arm (%)");
        
        XYSplineRenderer r = new XYSplineRenderer(3);
        XYPlot xyplot = new XYPlot(dataset, domain, range, r);
        
        Font font3 = new Font("Dialog", Font.PLAIN, 25);
        xyplot.getDomainAxis().setLabelFont(font3); // Increase size of "Number of tries"
        xyplot.getRangeAxis().setLabelFont(font3); // Increase size of "Correct predicitons(%)"
        xyplot.getDomainAxis().setTickLabelFont(font3); // Increase size of "Number-of-tries-ticks"
        xyplot.getRangeAxis().setTickLabelFont(font3); // Increase size of "Correct-predictions(%)-ticks"
        
        JFreeChart chart = new JFreeChart(xyplot);
        chart.getLegend().setItemFont(font3); // Increase size of Legend
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
	
	public static HashMap<Integer, Double> testMatrixArm(String plotName, ContextualBandit cb, ContextualSetting contextualSetting, Matrix characterMatrix) {
		
		String debugString = "";

		HashMap<Integer, Double> series = new HashMap<Integer, Double>();
		int numberOfSuccess = 0;
		try {
			for(int i = ConstantHolder.trainingSet; i < ConstantHolder.orderToEndAt; i++) {
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
					
					ConstantHolder.logWriter.println("The current arm is " + cba.getArmNumber() + " with vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta());
					ConstantHolder.logWriter.println("The order information:");
					ConstantHolder.logWriter.println("Date: " + currentOrder.getPlacedOrder().toString());
					ConstantHolder.logWriter.println("It predicted these items for order " + currentOrder.getOrderId() + " :");
				}

				boolean success = false;
				for(int j = 0; j < ConstantHolder.itemsRecommendedPerTurn; ) {
					double key = sortedMap.lastKey();
					ArrayList<Long> itemKeys2 = sortedMap.remove(key);
					for(Long itemKey : itemKeys2) {
						Item item = contextualSetting.getItem(itemKey);
						if(currentOrder.getItems().contains(item)) success = true;
						if(debug) {
							System.out.println("   " + item.getProductId() + " with score of " + key);
							ConstantHolder.logWriter.println("   " + item.getProductId() + " with score of " + key);
						}
						j++;
						if(!(j < ConstantHolder.itemsRecommendedPerTurn)) break;
					}
				}
				
				cba.train(success);
				if(success) numberOfSuccess++;
				if(debug) {
					System.out.println("These are the actual items bought: ");	
					ConstantHolder.logWriter.println("These are the actual items bought: ");	
				}
				for(Item item : currentOrder.getItems()) {
					item.buy(currentOrder);
					if(debug) {
						System.out.println("   " + item.getProductId());
						ConstantHolder.logWriter.println("   " + item.getProductId());
					}
				}
				if(evaluate) {
					series.put(i - ConstantHolder.trainingSet, (((double) numberOfSuccess)/((double) i - ConstantHolder.trainingSet))*100 );
				}
			}
		}
		catch (Exception e) {
			System.out.println(debugString);
			ConstantHolder.logWriter.println(debugString);
			e.printStackTrace();
		}
		int numberOfRightGuesses = 0;
		ContextualBayesArm thisRunBestArm = null;
		for(ContextualBayesArm cba : cb.getAllArms()) {
			numberOfRightGuesses += cba.getNumberOfBuys();
			System.out.println("The arm " + cba.getArmNumber() + " has vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta() + ", percentage " + ((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries())*100);
			ConstantHolder.logWriter.println("The arm " + cba.getArmNumber() + " has vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta() + ", percentage " + ((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries())*100);
			if(thisRunBestArm == null) {
				thisRunBestArm = cba;
			}
			if(((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries()) > ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())) {
				thisRunBestArm = cba;
			}
		}
		
		System.out.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		ConstantHolder.logWriter.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		
		if(bestArm == null || ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries()) > ((double)bestArm.getNumberOfBuys()/(double)bestArm.getNumberOfTries())) {
			bestArm = thisRunBestArm;
		}
		System.out.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		
		System.out.println("The right guesses are: " + numberOfRightGuesses + " which means " + (((double) numberOfRightGuesses)/((double) ConstantHolder.numberOfTests)*100) + "% correct");
		System.out.println("Done");
		
		ConstantHolder.logWriter.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		
		ConstantHolder.logWriter.println("The right guesses are: " + numberOfRightGuesses + " which means " + (((double) numberOfRightGuesses)/((double) ConstantHolder.numberOfTests)*100) + "% correct");
		ConstantHolder.logWriter.println("Done");
		
		return series;
	}
	
	public static HashMap<Integer, Double> testVectorArm(String plotName, ContextualBandit cb, ContextualSetting contextualSetting, int TYPE) {
		String debugString = "";

		HashMap<Integer, Double> series = new HashMap<Integer, Double>();
		int numberOfSuccess = 0;
		try {
			for(int i = ConstantHolder.trainingSet; i < ConstantHolder.orderToEndAt; i++) {
				boolean debug = i % 1000 == 0;
				boolean evaluate = i % 100 == 0;
				TreeMap<Double, ArrayList<Long>> sortedMap = new TreeMap<Double, ArrayList<Long>>();
				Order currentOrder = contextualSetting.getOrderByDate(i);
				User u = currentOrder.getUser();
				ContextualBayesArm cba = cb.getArm();
				
				debugString = currentOrder.getOrderId() + "";
				
				Vector userContext;
				if((TYPE & ConstantHolder.TYPE_NORMAL) > 0) {
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
					if((TYPE & ConstantHolder.TYPE_NORMAL) > 0) {
						itemVec = contextualSetting.getItem(key).getContextVector(zipcode, currentOrder.getPlacedOrder());
					} else {
						// Timefocus only
						//itemVec = contextualSetting.getItem(key).getNumberOfBuysInLastMonth(currentOrder.getPlacedOrder());
						itemVec = contextualSetting.getItem(key).getNumberOfBuysInLastWeek(currentOrder.getPlacedOrder());
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
					
					ConstantHolder.logWriter.println("The current arm is " + cba.getArmNumber() + " with vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta());
					ConstantHolder.logWriter.println("The order information:");
					ConstantHolder.logWriter.println("Date: " + currentOrder.getPlacedOrder().toString());
					ConstantHolder.logWriter.println("It predicted these items for order " + currentOrder.getOrderId() + " :");
				}

				boolean success = false;
				for(int j = 0; j < ConstantHolder.itemsRecommendedPerTurn; ) {
					double key = sortedMap.lastKey();
					ArrayList<Long> itemKeys2 = sortedMap.remove(key);
					for(Long itemKey : itemKeys2) {
						Item item = contextualSetting.getItem(itemKey);
						if(currentOrder.getItems().contains(item)) success = true;
						if(debug) {
							System.out.println("   " + item.getProductId() + " with score of " + key);
							ConstantHolder.logWriter.println("   " + item.getProductId() + " with score of " + key);
						}
						j++;
						if(!(j < ConstantHolder.itemsRecommendedPerTurn)) break;
					}
				}
				
				cba.train(success);
				if(success) numberOfSuccess++;
				if(debug) {
					System.out.println("These are the actual items bought: ");	
					ConstantHolder.logWriter.println("These are the actual items bought: ");	
				}
				for(Item item : currentOrder.getItems()) {
					item.buy(currentOrder);
					if(debug) {
						System.out.println("   " + item.getProductId());
						ConstantHolder.logWriter.println("   " + item.getProductId());
					}
				}
				if(evaluate) {
					series.put(i - ConstantHolder.trainingSet, (((double) numberOfSuccess)/((double) i - ConstantHolder.trainingSet))*100 );
				}
			}
		}
		catch (Exception e) {
			System.out.println(debugString);
			ConstantHolder.logWriter.println(debugString);
			
			e.printStackTrace();
		}
		int numberOfRightGuesses = 0;
		ContextualBayesArm thisRunBestArm = null;
		for(ContextualBayesArm cba : cb.getAllArms()) {
			numberOfRightGuesses += cba.getNumberOfBuys();
			System.out.println("The arm " + cba.getArmNumber() + " has vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta() + ", percentage " + ((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries())*100);
			ConstantHolder.logWriter.println("The arm " + cba.getArmNumber() + " has vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta() + ", percentage " + ((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries())*100);
			if(thisRunBestArm == null) {
				thisRunBestArm = cba;
			}
			if(((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries()) > ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())) {
				thisRunBestArm = cba;
			}
		}
		
		System.out.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		ConstantHolder.logWriter.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		
		if(bestArm == null || ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries()) > ((double)bestArm.getNumberOfBuys()/(double)bestArm.getNumberOfTries())) {
			bestArm = thisRunBestArm;
		}
		
		System.out.println("The right guesses are: " + numberOfRightGuesses + " which means " + (((double) numberOfRightGuesses)/((double) ConstantHolder.numberOfTests)*100) + "% correct");
		System.out.println("Done");
		ConstantHolder.logWriter.println("The right guesses are: " + numberOfRightGuesses + " which means " + (((double) numberOfRightGuesses)/((double) ConstantHolder.numberOfTests)*100) + "% correct");
		ConstantHolder.logWriter.println("Done");
		
		return series;
	}

	public static void savePlotToText(ArrayList<HashMap<Integer, Double>> allPlots) {
		try {
			
			int j = 0;
			ConstantHolder.plotWriter.print("[");
			for(HashMap<Integer, Double> plot : allPlots) {
				ConstantHolder.plotWriter.print("[");
				int k = 0;
				for(Integer i : plot.keySet()) {
					ConstantHolder.plotWriter.print("(" + i + "," + plot.get(i) + ")");
					
					k++;
					if(k != plot.keySet().size()) {
						ConstantHolder.plotWriter.print(",");
					}
				}
				ConstantHolder.plotWriter.print("]");
				j++;
				if(j != allPlots.size()) {
					ConstantHolder.plotWriter.println(",");			
				}
			}
			ConstantHolder.plotWriter.println("]");
			ConstantHolder.plotWriter.flush();
			ConstantHolder.plotWriter.close();
		} catch(Exception e) {
			System.out.println("FAIL");
			e.printStackTrace();
		}
	}
	
	public static HashMap<Integer, Double> testJaccardSimilarity(String plotName, ContextualSetting contextualSetting) {
		String debugString = "";

		HashMap<Integer, Double> series = new HashMap<Integer, Double>();
		int numberOfSuccess = 0;
		int numberOfFails = 0;
		try {
			for(int i = ConstantHolder.trainingSet; i < ConstantHolder.orderToEndAt; i++) {
				boolean debug = i % 1000 == 0;
				boolean evaluate = i % 100 == 0;
				Order currentOrder = contextualSetting.getOrderByDate(i);
				User u = currentOrder.getUser();
				JaccardDistanceAlgorithm jda = new JaccardDistanceAlgorithm(u);
				
				for(User u2 : userBought) {
					jda.addNewUser(u2);
				}
				
				Map<Long, Double> recommendationMap = jda.getRecommendationMap();
				
				// Convert Map to List
				List<Map.Entry<Long, Double>> sortedList = 
					new LinkedList<Map.Entry<Long, Double>>(recommendationMap.entrySet());
		 
				
				if(sortedList.size() >= ConstantHolder.itemsRecommendedPerTurn) {
					// Sort list with comparator, to compare the Map values
					Collections.sort(sortedList, new Comparator<Map.Entry<Long, Double>>() {
						public int compare(Map.Entry<Long, Double> o1,
			                                           Map.Entry<Long, Double> o2) {
							return (o2.getValue()).compareTo(o1.getValue());
						}
					});

					if(debug) {
						System.out.println("The order information:");
						System.out.println("Date: " + currentOrder.getPlacedOrder().toString());
						System.out.println("It predicted these items for order " + currentOrder.getOrderId() + " :");
						
						ConstantHolder.logWriter.println("The order information:");
						ConstantHolder.logWriter.println("Date: " + currentOrder.getPlacedOrder().toString());
						ConstantHolder.logWriter.println("It predicted these items for order " + currentOrder.getOrderId() + " :");
					}

					boolean success = false;
					
					int offset = 0;
					for(int j = 0; j < ConstantHolder.itemsRecommendedPerTurn; j++) {
						if(sortedList.size() <= j + offset) {
							numberOfFails++;
							success = false;
							break;
						}
						Map.Entry<Long, Double> entry = sortedList.get(j + offset);
						Long itemKey = entry.getKey();
						Item item = contextualSetting.getItemFromIndex(itemKey);
						while(u.getItemList().contains(item)) {
							offset++;
							if(sortedList.size() <= j + offset) {
								numberOfFails++;
								success = false;
								break;
							}
							entry = sortedList.get(j + offset);
							itemKey = entry.getKey();
							item = contextualSetting.getItemFromIndex(itemKey);
						}
						if(currentOrder.getItems().contains(item)) success = true;
						if(debug) {
							System.out.println("   " + item.getProductId() + " with score of " + entry.getValue());
							ConstantHolder.logWriter.println("   " + item.getProductId() + " with score of " + entry.getValue());
						}
					}
					
					if(success) numberOfSuccess++;
					if(debug) {
						System.out.println("These are the actual items bought: ");	
						ConstantHolder.logWriter.println("These are the actual items bought: ");	
					}
					for(Item item : currentOrder.getItems()) {
						u.addBuy(item);
						if(debug) {
							System.out.println("   " + item.getProductId());
							ConstantHolder.logWriter.println("   " + item.getProductId());
						}
					}
					if(evaluate) {
						series.put(i - ConstantHolder.trainingSet, (((double) numberOfSuccess)/((double) (i - ConstantHolder.trainingSet - numberOfFails + 1)))*100 );
					}
				} else {
					numberOfFails++;
					if(evaluate) {
						series.put(i - ConstantHolder.trainingSet, (((double) numberOfSuccess)/((double) (i - ConstantHolder.trainingSet - numberOfFails + 1)))*100 );
					}
				}
				if(!userBought.contains(u)) userBought.add(u);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		//series.put(ConstantHolder.orderToEndAt, (((double) numberOfSuccess)/((double) (ConstantHolder.orderToEndAt - ConstantHolder.trainingSet - numberOfFails + 1)))*100 );
		System.out.println(numberOfSuccess);
		System.out.println(numberOfFails);
		System.out.println("Jaccard is over");
		
		return series;
	}	
}
