package com.mapr.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mapr.bandit.BanditHittepa;
import com.mapr.bandit.BanditHittepa2;

public class Item {

	private long productId;
	private DateTime productCreated;
	private DateTime productModified;
	private boolean stock;
	private int gender;
	private int bought;
	private int youngBuys;
	private int middleBuys;
	private int oldBuys;
	private HashMap<Integer, Integer> zipcodes;
	private List<User> users;
	private List<Order> orders;
	private List<Integer> category;
	
	public static List<Item> mostPopularMonthly = new ArrayList<Item>();
	public static List<Item> mostPopularWeekly = new ArrayList<Item>();
	
	public DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

	public Item(long productId, int gender, DateTime productCreated, DateTime productModified, int stock, String byteCode){
		this.productId 			= productId;
		this.gender 			= gender;
		this.productCreated 	= productCreated;
		this.productModified	= productModified;
		this.stock				= stock == 0 ? false : true;
		this.youngBuys 			= 0;
		this.middleBuys			= 0;
		this.oldBuys			= 0;
		zipcodes 				= new HashMap<Integer, Integer>();
		bought 					= 0;
		users					= new ArrayList<User>();
		orders 					= new ArrayList<Order>();
		List<Category.Categories> subCategories = Category.GetByByteArray(byteCode);
		category 				= new ArrayList<Integer>();
		for(Category.Categories subCat : subCategories) {
			category.add(Category.GetMainCategoryByCategory(subCat).GetID() - 1);
		}
	}
	
	public Item(String[] row) throws Exception{
		this.productId		 	= Long.parseLong(row[0]);
		this.gender 			= Integer.parseInt(row[1]);
		this.productCreated 	= df.parseDateTime(row[2].substring(0,19));
		this.productModified 	= df.parseDateTime(row[3].substring(0,19));
		this.stock				= Integer.parseInt(row[4]) == 0 ? false : true;
		this.youngBuys 			= 0;
		this.middleBuys			= 0;
		this.oldBuys			= 0;
		zipcodes 				= new HashMap<Integer, Integer>();
		bought 					= 0;
		users 					= new ArrayList<User>();
		orders 					= new ArrayList<Order>();
		List<Category.Categories> subCategories = Category.GetByByteArray(row[5]);
		category 				= new ArrayList<Integer>();
		for(Category.Categories subCat : subCategories) {
			category.add(Category.GetMainCategoryByCategory(subCat).GetID() - 1);
		}
	}
	
	public Item(Item i) {
		this.productId			= i.productId;
		this.gender				= i.gender;
		this.productCreated		= new DateTime(i.productCreated);
		this.productModified	= new DateTime(i.productModified);
		this.stock				= i.stock;
		this.youngBuys			= 0;
		this.middleBuys			= 0;
		this.oldBuys			= 0;
		this.zipcodes			= new HashMap<Integer, Integer>();
		this.bought				= 0;
		this.users				= new ArrayList<User>();
		this.orders				= new ArrayList<Order>();
		this.category			= new ArrayList<Integer>();
		for(Integer subCat : i.getCategories()) {
			this.category.add(subCat);
		}
	}
	
	public void buy(Order o) {
		User u = o.getUser();
		DateTime d = o.getPlacedOrder();
		int zip = u.getZipCode(d);
		if(zipcodes.containsKey(zip)) {
			zipcodes.put(zip, zipcodes.get(zip) + 1);
		} else {
			zipcodes.put(zip, 1);
		}
		Vector v = u.getAge(d);
		if(v.get(0) > 0.5) {
			this.youngBuys++;
		} else if(v.get(1) > 0.5) {
			this.middleBuys++;
		} else {
			this.oldBuys++;
		}
		bought++;
		users.add(u);
		orders.add(o);
		
		Comparator<Item> compLastMonth = (e1, e2) -> Integer.compare(
	            e2.getBuysInLastMonth(d), e1.getBuysInLastMonth(d));
	
		Comparator<Item> compLastWeek = (e1, e2) -> Integer.compare(
	            e2.getBuysInLastWeek(d), e1.getBuysInLastWeek(d));
		
		
		if(mostPopularMonthly.size() < 10 && !mostPopularMonthly.contains(this)) {
			mostPopularMonthly.add(this);
		}
		else if(!mostPopularMonthly.contains(this)) {
			if(mostPopularMonthly.get(9).getBuysInLastMonth(d) < this.getBuysInLastMonth(d)) { 
				mostPopularMonthly.set(9, this);
			}
		}
		
		if(mostPopularWeekly.size() < 10 && !mostPopularWeekly.contains(this)){
			mostPopularWeekly.add(this);
		}
		else if(!mostPopularWeekly.contains(this)) {
			if(mostPopularWeekly.get(9).getBuysInLastWeek(d) < this.getBuysInLastWeek(d)) { 
				mostPopularWeekly.set(9, this);
			}
		}
		
		Collections.sort(mostPopularMonthly, compLastMonth);
		Collections.sort(mostPopularWeekly, compLastWeek);
		
	}
	
	public Vector getAgeVector() {
		double youngVec = 0.0, middleVec = 0.0, oldVec = 0.0;
		if(bought != 0) {
			youngVec = (double) youngBuys / (double)bought;
			middleVec = (double) middleBuys / (double)bought;
			oldVec = (double) oldBuys / (double)bought;
		}
		return new DenseVector(new double[]{youngVec, middleVec, oldVec});
	}
	
	public double getZipContext(int zip) {
		if(zipcodes.containsKey(zip)) {
			return (double) zipcodes.get(zip) / (double) bought;
		} else {
			return 0.0;
		}
	}
	
	public Vector getGenderContext() {
		if(this.gender == 1) {
			return new DenseVector(new double[]{1.0, 0.0});
		} else if(this.gender == 2) {
			return new DenseVector(new double[]{0.0, 1.0});
		}
		return new DenseVector(new double[]{1.0, 1.0});
	}
	
	public Vector getPopularityContext(DateTime sale) {
		double weekPopularity = 0.0, monthPopularity = 0.0;
		if(bought != 0) {
			DateTime lastMonth = sale.minusMonths(1);
			DateTime lastWeek = sale.minusWeeks(1);
			for(int i = 0; i < orders.size(); i++) {
				if(lastWeek.isBefore(orders.get(i).getPlacedOrder())) {
					weekPopularity++;
					monthPopularity++;
				}
				else if(lastMonth.isBefore(orders.get(i).getPlacedOrder())) {
					monthPopularity++;
				}
			}
		}
		return new DenseVector(new double[]{weekPopularity, monthPopularity});
	}
	
	public double[] getCategoriesVector() {
		double[] categories = new double[Category.numberOfCategories];
		for(int i = 1; i <= Category.numberOfCategories; i++) {
			if(category.contains(i-1)) {
				categories[i-1] = 1.0; 
			} else {
				categories[i-1] = 0.0;
			}
		}
		return categories;
	}
	
	public Vector getContextVector(int zipcode, DateTime sale) {
		if(sale.isAfter(this.productCreated) && (stock || sale.isBefore(this.productModified))) {
			ArrayList<Vector> contextVector = new ArrayList<Vector>();
			if(BanditHittepa2.useAge) contextVector.add(getAgeVector());
			if(BanditHittepa2.useGender) contextVector.add(getGenderContext());
			if(BanditHittepa2.useLocation) contextVector.add(new DenseVector(new double[] {getZipContext(zipcode)}));
			if(BanditHittepa2.usePopularity) contextVector.add(getPopularityContext(sale));
			double[] contextuality = new double[0 + (BanditHittepa2.useAge ? 3 : 0) + (BanditHittepa2.useGender ? 2 : 0) + (BanditHittepa2.useLocation ? 1 : 0)
			                                          + (BanditHittepa2.usePopularity ? 2 : 0) + (BanditHittepa2.useCategories ? Category.numberOfCategories : 0)];
			int pointer = 0;
			for(Vector preContext : contextVector) {
				for(int i = 0; i < preContext.size(); i++) {
					contextuality[pointer] = preContext.get(i);
					pointer++;
				}
			}
			
			if(BanditHittepa.useCategories) {
				double[] categories = getCategoriesVector();
				
				for(int i = 0 ; i < categories.length; i++) {
					contextuality[i + pointer] = categories[i];
				}
			}
			
			return new DenseVector(contextuality);
			
		} else {
			double[] contextuality = new double[BanditHittepa.numberOfFeatures];
			for(int i = 0; i < contextuality.length; i++) {
				contextuality[i] = 0.0;
			}
			return new DenseVector(contextuality);
		}
	}
	
	public Vector getUserContextVector(Matrix dm, DateTime date) {
		double[] contextuality = new double[BanditHittepa.extraFeatures];
		for(int i = 0; i < contextuality.length; i++) {
			contextuality[i] = 0.0;
		}
		
		Vector vec = new DenseVector(contextuality);
		
		for(User u : users) {
			vec = vec.plus(u.getUserContextVector(dm, date).divide(users.size()));
		}
		
		return vec;
	}
	
	public long getProductId() {
		return productId;
	}

	public void setProductDetailId(long productId) {
		this.productId = productId;
	}

	public DateTime getProductCreated() {
		return productCreated;
	}

	public void setProductCreated(DateTime productCreated) {
		this.productCreated = productCreated;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getBought() {
		return bought;
	}

	public void setBought(int bought) {
		this.bought = bought;
	}

	public int getYoungBuys() {
		return youngBuys;
	}

	public void setYoungBuys(int youngBuys) {
		this.youngBuys = youngBuys;
	}

	public int getMiddleBuys() {
		return middleBuys;
	}

	public void setMiddleBuys(int middleBuys) {
		this.middleBuys = middleBuys;
	}

	public int getOldBuys() {
		return oldBuys;
	}

	public void setOldBuys(int oldBuys) {
		this.oldBuys = oldBuys;
	}

	public HashMap<Integer, Integer> getZipcodes() {
		return zipcodes;
	}

	public void setZipcodes(HashMap<Integer, Integer> zipcodes) {
		this.zipcodes = zipcodes;
	}

	public DateTimeFormatter getDf() {
		return df;
	}

	public void setDf(DateTimeFormatter df) {
		this.df = df;
	}
	
	public boolean getStock() {
		return stock;
	}
	
	public DateTime getProductModified() {
		return productModified;
	}
	
	public void update(Item i) {
		if(i.getProductCreated().isBefore(this.productCreated)) {
			this.productCreated = new DateTime(i.getProductCreated());
		}
		if(!this.stock) {
			this.stock = i.getStock();
		}
		if(i.getProductModified().isAfter(this.productModified)) {
			this.productModified = new DateTime(i.getProductModified());
		}
	}
	
	public Vector getNumberOfBuysInLastMonth(DateTime sale) {
		if(sale.isAfter(this.productCreated) && (stock || sale.isBefore(productModified))) {
			int buys = 0;
			DateTime lastMonth = sale.minusMonths(1);
			for(int i = 0; i < orders.size(); i++) {
				if(lastMonth.isBefore(orders.get(i).getPlacedOrder())) {
					buys++;
				}
			}
			return new DenseVector(new double[]{(double)buys});
		} else {
			return new DenseVector(new double[]{0.0});
		}
	}
	
	public int getBuysInLastMonth(DateTime sale) {
		if(sale.isAfter(this.productCreated) && (stock || sale.isBefore(productModified))) {
			int buys = 0;
			DateTime lastMonth = sale.minusMonths(1);
			for(int i = 0; i < orders.size(); i++) {
				if(lastMonth.isBefore(orders.get(i).getPlacedOrder())) {
					buys++;
				}
			}
			return buys;
		} else {
			return 0;
		}
	}
	
	public int getBuysInLastWeek(DateTime sale) {
		if(sale.isAfter(this.productCreated) && (stock || sale.isBefore(productModified))) {
			int buys = 0;
			DateTime lastWeek = sale.minusWeeks(1);
			for(int i = 0; i < orders.size(); i++) {
				if(lastWeek.isBefore(orders.get(i).getPlacedOrder())) {
					buys++;
				}
			}
			return buys;
		} else {
			return 0;
		}
	}
	
	public List<Integer> getCategories() {
		return category;
	}
	
	public Item copy() {
		return new Item(this);
	}
}
