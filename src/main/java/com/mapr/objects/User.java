package com.mapr.objects;

import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mapr.bandit.BanditHittepa;
import com.mapr.bandit.BanditHittepa2;

public class User {

	private long userId;
	private List<DateTime> updated;
	private List<Integer> zipcode;
	private DateTime dateOfBirth;
	private int gender;
	
	private int[] categories;
	private int buys;
	
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public List<DateTime> getUpdated() {
		return updated;
	}

	public void setUpdated(List<DateTime> updated) {
		this.updated = updated;
	}

	public List<Integer> getZipcode() {
		return zipcode;
	}

	public void setZipcode(List<Integer> zipcode) {
		this.zipcode = zipcode;
	}

	public DateTime getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(DateTime dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public DateTimeFormatter getDf() {
		return df;
	}

	public void setDf(DateTimeFormatter df) {
		this.df = df;
	}

	DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	
	public User(long userId, DateTime updated, int zipcode, DateTime dateOfBirth, int gender){
		this.userId 		= userId;
		this.updated 		= new ArrayList<DateTime>();
		this.updated.add(updated);
		this.zipcode 		= new ArrayList<Integer>();
		this.zipcode.add(zipcode);
		this.dateOfBirth 	= dateOfBirth;
		this.gender 		= gender;
		this.categories		= new int[Category.numberOfCategories];
		for(int i = 0; i < categories.length; i++) {
			categories[i] = 0;
		}
		this.buys			= 0;
	}
	
	public User(String[] row) throws Exception{
		this.userId 		= Long.parseLong(row[0]);
		this.updated 		= new ArrayList<DateTime>();
		if(!row[1].equals("")) {
			this.updated.add(df.parseDateTime(row[1].substring(0,19)));
		}
		String integerParse = row[2].replaceAll("[^0-9]", "");
		this.zipcode = new ArrayList<Integer>();
		if(!integerParse.equals("")) {
			if(integerParse.length() > 2) {
				this.zipcode.add(Integer.parseInt(integerParse.substring(0, 2)));
			} else {
				this.zipcode.add(Integer.parseInt(integerParse));
			}
		} else {
			this.zipcode.add(00);
		}
		if(!row[3].equals("")) {
			this.dateOfBirth = df.parseDateTime(row[3].substring(0,19));
		} else {
			this.dateOfBirth = new DateTime();
		}
		this.gender 		= Integer.parseInt(row[4]);
		this.categories		= new int[Category.numberOfCategories];
		for(int i = 0; i < categories.length; i++) {
			categories[i] 	= 0;
		}
		this.buys 			= 0;
	}
	
	public User(User u) {
		this.userId				= u.userId;
		this.updated			= new ArrayList<DateTime>();
		for(DateTime dt : u.updated) {
			this.updated.add(new DateTime(dt));
		}
		this.zipcode = new ArrayList<Integer>();
		for(Integer i : u.zipcode) {
			this.zipcode.add(i);
		}
		this.dateOfBirth		= new DateTime(u.dateOfBirth);
		this.gender				= u.gender;
		this.categories			= new int[Category.numberOfCategories];
		for(int i = 0; i < categories.length; i++) {
			categories[i] = 0;
		}
		this.buys 				= 0;
	}
	
	public Vector getAge(DateTime saleMoment) {
		double y = Days.daysBetween(saleMoment, dateOfBirth).getDays() < 6600 ? 1.0 : 0.0;
		double m = Days.daysBetween(saleMoment, dateOfBirth).getDays() > 6600 && Days.daysBetween(saleMoment, dateOfBirth).getDays() < 12000 ? 1.0 : 0.0;
		double a = Days.daysBetween(saleMoment, dateOfBirth).getDays() > 12000 ? 1.0 : 0.0;
		return new DenseVector(new double[]{y, m ,a});
	}
	
	public void addZipCode(DateTime date, int zipcode) {
		this.updated.add(date);
		this.zipcode.add(zipcode);
	}
	
	public int getZipCode(DateTime date) {
		for(int i = 0; i < zipcode.size(); i++) {
			if(updated.get(i).isAfter(date)) {
				if(i == 0) {
					return zipcode.get(0);
				} else {
					return zipcode.get(i-1);
				}
			}
		}
		return zipcode.get(zipcode.size() - 1);
	}
	
	public Vector getGenderContext() {
		if(this.gender == 1) {
			return new DenseVector(new double[]{1.0, 0.0});
		} else if(this.gender == 2) {
			return new DenseVector(new double[]{0.0, 1.0});
		}
		return new DenseVector(new double[]{1.0, 1.0});
	}
	
	public void addBuy(Item i) {
		for(int cat : i.getCategories()) {
			categories[cat]++;
		}
	}
	
	public double[] categoryVector() {
		double[] categoryVec = new double[categories.length];
		for(int i = 0; i < categories.length; i++) {
			if(buys > 0) {
				categoryVec[i] = ((double)categories[i]) / (double) buys;
			} else {
				categoryVec[i] = 0.0;
			}
		}
		return categoryVec;
	}
	
	public Vector getContextVector(DateTime date) {
		double popularityFocus = 1.0;
		ArrayList<Vector> contextVector = new ArrayList<Vector>();
		if(BanditHittepa2.useAge) contextVector.add(getAge(date));
		if(BanditHittepa2.useGender) contextVector.add(getGenderContext());
		if(BanditHittepa2.useLocation) contextVector.add(new DenseVector(new double[] {1.0}));
		if(BanditHittepa2.usePopularity) contextVector.add(new DenseVector(new double[] {popularityFocus, popularityFocus}));
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
			double[] category = categoryVector();
			
			for(int i = 0 ; i < categories.length; i++) {
				contextuality[i + pointer] = category[i];
			}
		}
		
		return new DenseVector(contextuality);
	}
	
	public Vector getUserContextVector(Matrix dm, DateTime date) {
		return dm.times(this.getContextVector(date));
	}
	
	public User copy() {
		return new User(this);
	}
}
