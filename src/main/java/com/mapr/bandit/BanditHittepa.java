package com.mapr.bandit;

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

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;

import com.mapr.objects.Category;
import com.mapr.objects.Item;
import com.mapr.objects.Order;
import com.mapr.objects.User;
import com.mapr.stats.bandit.ContextualBandit;
import com.mapr.stats.bandit.ContextualBayesArm;

public class BanditHittepa {

    private static Random gen = RandomUtils.getRandom();
    
    public final static boolean useCategories = false;
    
 	public final static int TYPE_MATRIX_VECTOR = 0;
 	public final static int TYPE_VECTOR_VECTOR = 1;
 	public final static int TYPE_MOST_BUYS = 2;
 	
    public final static int startPlace = 600000;
	public final static int trainingSet = startPlace + 1000;
	public final static int numberOfTests = 5000;
	public final static int orderToEndAt = trainingSet + numberOfTests;
	public final static int numberOfArms = 50;
	public final static int numberOfFeatures = useCategories ? 8 + Category.numberOfCategories: 8;
	public final static int extraFeatures = 10;
	public final static int debugOutPrint = 100000;
	public final static int itemsRecommendedPerTurn = 10;
	
	
    
	static String[] csvFiles = {"data/JunkyardItems.csv", "data/JunkyardUser.csv", "data/JunkyardOrders.csv" };
	static String csvSplitBy = ",";
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// Type defines what 
	 	int TYPE = TYPE_VECTOR_VECTOR;
		
		final HashMap<Long, Item> items = new HashMap<Long, Item>();
		final HashMap<Long, User> users = new HashMap<Long, User>();
		final HashMap<Long, Order> orders = new HashMap<Long, Order>();
		
		String debugString = "";
		String line = "";
		
		ArrayList<Order> ordersByDate = new ArrayList<Order>();
		ContextualBandit cb;
		if(TYPE == TYPE_MATRIX_VECTOR) {
			cb = new ContextualBandit(numberOfArms, extraFeatures);
		} else if(TYPE == TYPE_VECTOR_VECTOR){
			cb = new ContextualBandit(numberOfArms, numberOfFeatures);		
		} else {
			cb = new ContextualBandit(1, 1);
		}
		
		double[][] randomizedMatrix = new double[extraFeatures][numberOfFeatures];
		for(int i = 0; i < extraFeatures; i++) {
			for (int j = 0; j < numberOfFeatures; j++) {
				randomizedMatrix[i][j] = gen.nextDouble();
			}
		}
		
		Matrix characterMatrix = new DenseMatrix(randomizedMatrix);
		System.out.println(characterMatrix.asFormatString());
		
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
				} else {
					items.put(i.getProductId(), i);
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
				}
				else {
					User u2 = users.get(u.getUserId());
					u2.addZipCode(u.getUpdated().get(0), u.getZipcode().get(0));
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
				Order o;
				if(!orders.containsKey(orderId)) {
					Long userId = Long.parseLong(row[0]);
					o = new Order(users.get(userId), orderId, row[3]);
					orders.put(orderId, o);
					ordersByDate.add(o);
				} else {
					o = orders.get(orderId);
				}
				o.addItem(items.get(itemId));
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
		}
		
		try {
			for(int i = trainingSet; i < orderToEndAt; i++) {
				boolean debug = i % 1000 == 0;
				TreeMap<Double, ArrayList<Long>> sortedMap = new TreeMap<Double, ArrayList<Long>>();
				Order currentOrder = ordersByDate.get(i);
				User u = currentOrder.getUser();
				ContextualBayesArm cba = cb.getArm();
				
				debugString = currentOrder.getOrderId() + "";
				
				Vector userContext;
				if(TYPE == TYPE_MATRIX_VECTOR) {
					userContext = cba.getContext().times(u.getUserContextVector(characterMatrix, currentOrder.getPlacedOrder()));
				} else if(TYPE == TYPE_VECTOR_VECTOR) {
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
					if(TYPE == TYPE_MATRIX_VECTOR) {
						itemVec = items.get(key).getUserContextVector(characterMatrix, currentOrder.getPlacedOrder());
					} else if(TYPE == TYPE_VECTOR_VECTOR) {
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
				if(debug) System.out.println("These are the actual items bought: ");	
				for(Item item : currentOrder.getItems()) {
					item.buy(currentOrder);
					if(debug) System.out.println("   " + item.getProductId());
				}
			}
		}
		catch (Exception e) {
			System.out.println(debugString);
			e.printStackTrace();
		}
		int numberOfRightGuesses = 0;
		ContextualBayesArm bestArm = null;
		for(ContextualBayesArm cba : cb.getAllArms()) {
			numberOfRightGuesses += cba.getNumberOfBuys();
			System.out.println("The arm " + cba.getArmNumber() + " has vector " + cba.getContext().toString() + " with alpha " + cba.getAlpha() + " and beta " + cba.getBeta() + ", percentage " + ((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries())*100);
			if(bestArm == null) {
				bestArm = cba;
			}
			if(((double)cba.getNumberOfBuys()/(double)cba.getNumberOfTries()) > ((double)bestArm.getNumberOfBuys()/(double)bestArm.getNumberOfTries())) {
				bestArm = cba;
			}
		}
		
		System.out.println("The best arm is " + bestArm.getArmNumber() + " with vector " + bestArm.getContext().toString() + " with alpha " + bestArm.getAlpha() + " and beta " + bestArm.getBeta() + ", percentage " + ((double)bestArm.getNumberOfBuys()/(double)bestArm.getNumberOfTries())*100);
		
		System.out.println("The right guesses are: " + numberOfRightGuesses + " which means " + (((double) numberOfRightGuesses)/((double) numberOfTests)*100) + "% correct");
		System.out.println("Done");
	  }
}
