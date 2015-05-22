package com.mapr.bandit;

import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.mapr.objects.Category;
import com.mapr.objects.Item;
import com.mapr.objects.Order;
import com.mapr.objects.User;
import com.mapr.stats.bandit.ContextualBandit;
import com.mapr.stats.bandit.ContextualBayesArm;

public class BanditHittepa {

    private static Random gen = RandomUtils.getRandom();
    
    public final static boolean useCategories = true;
    
 	public final static int TYPE_MATRIX_VECTOR = 0;
 	public final static int TYPE_VECTOR_VECTOR = 1;
 	public final static int TYPE_MOST_BUYS = 2;
 	
    public final static int startPlace = 100000;
	public final static int trainingSet = startPlace + 1;
	public final static int numberOfTests = 10000;
	public final static int orderToEndAt = trainingSet + numberOfTests;
	public final static int numberOfArms = 50;
	public final static int numberOfFeatures = useCategories ? 8 + Category.numberOfCategories : 8;
	public final static int extraFeatures = 10;
	public final static int debugOutPrint = 100000;
	public final static int itemsRecommendedPerTurn = 10;
	
	public static ContextualBayesArm bestArm = null;
    
	static String[] csvFiles = {"data/JunkyardItems.csv", "data/JunkyardUser.csv", "data/JunkyardOrders.csv" };
	static String csvSplitBy = ",";
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// Type defines what 
		int TYPE = TYPE_VECTOR_VECTOR;
		
	 	long start1 = System.currentTimeMillis();
	 	
	 	final HashMap<Long, Item> items = new HashMap<Long, Item>();
		final HashMap<Long, User> users = new HashMap<Long, User>();
		final HashMap<Long, Order> orders = new HashMap<Long, Order>();
		final HashMap<Long, Item> itemsEvaluation = new HashMap<Long, Item>();
		final HashMap<Long, User> usersEvaluation = new HashMap<Long, User>();
		final HashMap<Long, Order> ordersEvaluation = new HashMap<Long, Order>();
		
		String debugString = "";
		String line = "";
		
		ArrayList<Order> ordersByDate = new ArrayList<Order>();
		ArrayList<Order> ordersByDateEvaluation = new ArrayList<Order>();
		
		try {
	 
			BufferedReader[] br = {new BufferedReader(new FileReader(csvFiles[0])), 
								   new BufferedReader(new FileReader(csvFiles[1])), 
								   new BufferedReader(new FileReader(csvFiles[2]))};
			int debug = 0;
			
			while ((line = br[0].readLine()) != null) {
			    // use comma as separator
				debug++;
				if(debug > debugOutPrint) {
					System.out.println("Items: " + line);
					debug = 0;
				}
				String[] row = line.split(csvSplitBy);
				Item i = new Item(row);
				if(items.containsKey(i.getProductId())) {
					items.get(i.getProductId()).update(i);
					itemsEvaluation.get(i.getProductId()).update(i.copy());
				} else {
					items.put(i.getProductId(), i);
					itemsEvaluation.put(i.getProductId(), i.copy());
				}
				
			}
			
			while ((line = br[1].readLine()) != null) {
			    // use comma as separator
				debug++;
				if(debug > debugOutPrint) {
					System.out.println("Users: " + line);
					debug = 0;
				}
				User u = new User(line.split(csvSplitBy));
				if(!users.containsKey(u.getUserId())) {
					users.put(u.getUserId(), u); 
					usersEvaluation.put(u.getUserId(), u.copy());
				}
				else {
					User u2 = users.get(u.getUserId());
					u2.addZipCode(u.getUpdated().get(0), u.getZipcode().get(0));
					User uEval = usersEvaluation.get(u.getUserId());
					uEval.addZipCode(u.getUpdated().get(0), u.getZipcode().get(0));
				}
			}
			
			while ((line = br[2].readLine()) != null) {
			    // use comma as separator
				debug++;
				if(debug > debugOutPrint) {
					System.out.println("Orders: " + line);
					debug = 0;
				}
				String[] row = line.split(csvSplitBy);
				
				Long orderId = Long.parseLong(row[1]);
				Long itemId = Long.parseLong(row[2]);
				Order o, oEvaluation;
				if(!orders.containsKey(orderId)) {
					Long userId = Long.parseLong(row[0]);
					o = new Order(users.get(userId), orderId, row[3]);
					oEvaluation = new Order(usersEvaluation.get(userId), orderId, row[3]);
					orders.put(orderId, o);
					ordersEvaluation.put(orderId, oEvaluation);
					ordersByDate.add(o);
					ordersByDateEvaluation.add(oEvaluation);
				} else {
					o = orders.get(orderId);
					oEvaluation = ordersEvaluation.get(orderId);
				}
				o.addItem(items.get(itemId));
				oEvaluation.addItem(itemsEvaluation.get(itemId));
			}
			
			
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
		for(int i = startPlace; i < trainingSet; i++) {
			Order o = ordersByDate.get(i);
			List<Item> itemList = o.getItems();
			for(Item item : itemList) {
				item.buy(o);
			}
			Order oEval = ordersByDateEvaluation.get(i);
			List<Item> itemListEval = oEval.getItems();
			for(Item item : itemListEval) {
				item.buy(oEval);
			}
		}
		double[][] randomizedMatrix = new double[extraFeatures][numberOfFeatures];
		
		if(TYPE == TYPE_MATRIX_VECTOR) {
			for(int i = 0; i < extraFeatures; i++) {
				for (int j = 0; j < numberOfFeatures; j++) {
					randomizedMatrix[i][j] = gen.nextDouble();
				}
			}
			Matrix characterMatrix = new DenseMatrix(randomizedMatrix);
			
			ContextualBandit cb = new ContextualBandit(numberOfArms, extraFeatures);
			
			XYSeries seriesAll = testMatrixArm("All arms", cb, items, users, orders, ordersByDate, characterMatrix);
			
			ArrayList<ContextualBayesArm> bestArms = new ArrayList<ContextualBayesArm>();
			bestArms.add(bestArm.copy());
			ContextualBandit cbEvaluation = new ContextualBandit(bestArms);
			
			XYSeries seriesReference = testMatrixArm("Best arm", cbEvaluation, itemsEvaluation, usersEvaluation, ordersEvaluation, ordersByDateEvaluation, characterMatrix);

			plot(seriesAll, seriesReference);
		} else {
			ContextualBandit cb;
			
			if(TYPE == TYPE_VECTOR_VECTOR){
				cb = new ContextualBandit(numberOfArms, numberOfFeatures);		
			} else {
				cb = new ContextualBandit(1, 1);
			}
			
			XYSeries seriesAll = testVectorArm("All arms", cb, items, users, orders, ordersByDate, TYPE);
			
			ArrayList<ContextualBayesArm> bestArms = new ArrayList<ContextualBayesArm>();
			bestArms.add(bestArm.copy());
			ContextualBandit cbEvaluation = new ContextualBandit(bestArms);
			
			XYSeries seriesReference = testVectorArm("Best arm", cbEvaluation, itemsEvaluation, usersEvaluation, ordersEvaluation, ordersByDateEvaluation, TYPE);
			
			plot(seriesAll, seriesReference);
		}
		
		System.out.println((System.currentTimeMillis() - start1) / 1000);
	  }
	
	public double computeJaccardDistance(String stringOne, String stringTwo) {
		   return (double) intersect(stringOne, stringTwo).length() /
		          (double) union(stringOne, stringTwo).length();
		 }
	
	/** Returns the union of the two strings, case insensitive. 
	Takes O( (|S1| + |S2|) ^2 ) time. */
	public static String union(String s1, String s2){
	    String s = (s1 + s2).toLowerCase(); //start with entire contents of both strings
	    int i = 0;
	    while(i < s.length()){
	        char c = s.charAt(i);
	        if(i != s.lastIndexOf(c)) //If c occurs multiple times in s, remove first one
	            s = s.substring(0, i) + s.substring(i+1, s.length());
	        else i++; //otherwise move pointer forward
	    }
	    return s;
	}
	
	/** Returns the intersection of the two strings, case insensitive. 
 	Takes O( |S1| * |S2| ) time. */
	public static String intersect(String s1, String s2){
	    String s = "";
	    s2 = s2.toLowerCase();
	    for(char c : s1.toLowerCase().toCharArray()){
	        if(s2.indexOf(c) != -1 && s.indexOf(c) == -1)
	            s += c;
	    }
	    return s;
	}
	
	private static void plot(XYSeries series, XYSeries series2) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        dataset.addSeries(series2);
        NumberAxis domain = new NumberAxis("Number of tries");
        NumberAxis range = new NumberAxis("Correct predictions (%)");
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
                return new Dimension(1024, 800);
            }
        };
        
        JFrame frame = new JFrame("Bandit Evaluation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chartPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
	
	public static XYSeries testMatrixArm(String plotName, ContextualBandit cb, HashMap<Long, Item> items, HashMap<Long, User> users, HashMap<Long, Order> orders, List<Order> ordersByDate, Matrix characterMatrix) {
		
		String debugString = "";

		XYSeries series = new XYSeries(plotName);
		int numberOfSuccess = 0;
		try {
			for(int i = trainingSet; i < orderToEndAt; i++) {
				boolean debug = i % 1000 == 0;
				boolean evaluate = i % 100 == 0;
				TreeMap<Double, ArrayList<Long>> sortedMap = new TreeMap<Double, ArrayList<Long>>();
				Order currentOrder = ordersByDate.get(i);
				User u = currentOrder.getUser();
				ContextualBayesArm cba = cb.getArm();
				
				debugString = currentOrder.getOrderId() + "";
				
				Vector userContext;
				userContext = cba.getContext().times(u.getUserContextVector(characterMatrix, currentOrder.getPlacedOrder()));
				
				Set<Long> itemKeys = items.keySet();
				int zipcode = u.getZipCode(currentOrder.getPlacedOrder());
				for(Long key : itemKeys) {
					double result = 0;
					Vector itemVec;
					itemVec = items.get(key).getUserContextVector(characterMatrix, currentOrder.getPlacedOrder());
					
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
					
				}

				boolean success = false;
				for(int j = 0; j < itemsRecommendedPerTurn; ) {
					double key = sortedMap.lastKey();
					ArrayList<Long> itemKeys2 = sortedMap.remove(key);
					for(Long itemKey : itemKeys2) {
						Item item = items.get(itemKey);
						if(currentOrder.getItems().contains(item)) success = true;
						if(debug) System.out.println("   " + item.getProductId() + " with score of " + key);
						j++;
						if(!(j < itemsRecommendedPerTurn)) break;
					}
				}
				
				cba.train(success);
				if(success) numberOfSuccess++;
				if(debug) System.out.println("These are the actual items bought: ");	
				for(Item item : currentOrder.getItems()) {
					item.buy(currentOrder);
					if(debug) System.out.println("   " + item.getProductId());
				}
				if(evaluate) {
					series.add(i - trainingSet, (((double) numberOfSuccess)/((double) i - trainingSet))*100 );
				}
			}
		}
		catch (Exception e) {
			System.out.println(debugString);
			e.printStackTrace();
		}
		int numberOfRightGuesses = 0;
		ContextualBayesArm thisRunBestArm = null;
		for(ContextualBayesArm cba : cb.getAllArms()) {
			numberOfRightGuesses += cba.getNumberOfBuys();
			System.out.println("The arm " + cba.getArmNumber() + " has vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta() + ", percentage " + ((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries())*100);
			if(thisRunBestArm == null) {
				thisRunBestArm = cba;
			}
			if(((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries()) > ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())) {
				thisRunBestArm = cba;
			}
		}
		
		System.out.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		
		if(bestArm == null || ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries()) > ((double)bestArm.getNumberOfBuys()/(double)bestArm.getNumberOfTries())) {
			bestArm = thisRunBestArm;
		}
		
		System.out.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		
		System.out.println("The right guesses are: " + numberOfRightGuesses + " which means " + (((double) numberOfRightGuesses)/((double) numberOfTests)*100) + "% correct");
		System.out.println("Done");
		
		return series;
	}
	
	public static XYSeries testVectorArm(String plotName, ContextualBandit cb, HashMap<Long, Item> items, HashMap<Long, User> users, HashMap<Long, Order> orders, List<Order> ordersByDate, int TYPE) {
		String debugString = "";

		XYSeries series = new XYSeries(plotName);
		int numberOfSuccess = 0;
		try {
			for(int i = trainingSet; i < orderToEndAt; i++) {
				boolean debug = i % 1000 == 0;
				boolean evaluate = i % 100 == 0;
				TreeMap<Double, ArrayList<Long>> sortedMap = new TreeMap<Double, ArrayList<Long>>();
				Order currentOrder = ordersByDate.get(i);
				User u = currentOrder.getUser();
				ContextualBayesArm cba = cb.getArm();
				
				debugString = currentOrder.getOrderId() + "";
				
				Vector userContext;
				if(TYPE == TYPE_VECTOR_VECTOR) {
					userContext = cba.getContext().times(u.getContextVector(currentOrder.getPlacedOrder()));
				} else {
					// Timefocus only
					userContext = new DenseVector(new double[]{1.0});
				}
				Set<Long> itemKeys = items.keySet();
				int zipcode = u.getZipCode(currentOrder.getPlacedOrder());
				for(Long key : itemKeys) {
					double result = 0;
					Vector itemVec;
					if(TYPE == TYPE_VECTOR_VECTOR) {
						itemVec = items.get(key).getContextVector(zipcode, currentOrder.getPlacedOrder());
					} else {
						// Timefocus only
						itemVec = items.get(key).getNumberOfBuysInLastMonth(currentOrder.getPlacedOrder());
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
					
				}

				boolean success = false;
				for(int j = 0; j < itemsRecommendedPerTurn; ) {
					double key = sortedMap.lastKey();
					ArrayList<Long> itemKeys2 = sortedMap.remove(key);
					for(Long itemKey : itemKeys2) {
						Item item = items.get(itemKey);
						if(currentOrder.getItems().contains(item)) success = true;
						if(debug) System.out.println("   " + item.getProductId() + " with score of " + key);
						j++;
						if(!(j < itemsRecommendedPerTurn)) break;
					}
				}
				
				cba.train(success);
				if(success) numberOfSuccess++;
				if(debug) System.out.println("These are the actual items bought: ");	
				for(Item item : currentOrder.getItems()) {
					item.buy(currentOrder);
					if(debug) System.out.println("   " + item.getProductId());
				}
				if(evaluate) {
					series.add(i - trainingSet, (((double) numberOfSuccess)/((double) i - trainingSet))*100 );
				}
			}
		}
		catch (Exception e) {
			System.out.println(debugString);
			e.printStackTrace();
		}
		int numberOfRightGuesses = 0;
		ContextualBayesArm thisRunBestArm = null;
		for(ContextualBayesArm cba : cb.getAllArms()) {
			numberOfRightGuesses += cba.getNumberOfBuys();
			System.out.println("The arm " + cba.getArmNumber() + " has vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta() + ", percentage " + ((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries())*100);
			if(thisRunBestArm == null) {
				thisRunBestArm = cba;
			}
			if(((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries()) > ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())) {
				thisRunBestArm = cba;
			}
		}
		
		System.out.println("The best arm is " + thisRunBestArm.getArmNumber() + " with vector " + thisRunBestArm.getContext().toString() + " with alpha " + thisRunBestArm.getAlpha() + " and beta " + thisRunBestArm.getBeta() + ", percentage " + ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries())*100);
		
		if(bestArm == null || ((double)thisRunBestArm.getNumberOfBuys()/(double)thisRunBestArm.getNumberOfTries()) > ((double)bestArm.getNumberOfBuys()/(double)bestArm.getNumberOfTries())) {
			bestArm = thisRunBestArm;
		}
		
		System.out.println("The right guesses are: " + numberOfRightGuesses + " which means " + (((double) numberOfRightGuesses)/((double) numberOfTests)*100) + "% correct");
		System.out.println("Done");
		
		return series;
	}
	
}
