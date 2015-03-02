package com.mapr.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
	
	public DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-mm-dd HH:mm:ss");

	public Item(long productId, int gender, DateTime productCreated, DateTime productModified, int stock){
		this.productId 			= productId;
		this.gender 			= gender;
		this.productCreated 	= productCreated;
		this.productModified	= productModified;
		this.stock				= stock == 0 ? false : true;
		this.youngBuys 			= 0;
		this.middleBuys			= 0;
		this.oldBuys			= 0;
		zipcodes = new HashMap<Integer, Integer>();
		bought = 0;
		users = new ArrayList<User>();
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
		zipcodes = new HashMap<Integer, Integer>();
		bought = 0;
		users = new ArrayList<User>();
	}
	
	public void buy(User u, DateTime d) {
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
	
	public Vector getContextVector(int zipcode, DateTime sales) {
		if(sales.isAfter(this.productCreated) && (stock || sales.isBefore(productModified))) {
			Vector age = getAgeVector();
			Vector gender = getGenderContext();
			double zip = getZipContext(zipcode);
			return new DenseVector(new double[] {age.get(0), age.get(1), age.get(2), gender.get(0), gender.get(1), zip});
		} else {
			return new DenseVector(new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
		}
	}
	
	public Vector getUserContextVector(Matrix dm, DateTime date) {
		Vector vec = new DenseVector(new double[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f});
		
		for(User u : users) {
			vec = vec.plus(dm.times(u.getContextVector(date)).divide(users.size()));
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
}
