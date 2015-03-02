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

public class User {

	private long userId;
	private List<DateTime> updated;
	private List<Integer> zipcode;
	private DateTime dateOfBirth;
	private int gender;
	
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

	DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-mm-dd HH:mm:ss");
	
	public User(long userId, DateTime updated, int zipcode, DateTime dateOfBirth, int gender){
		this.userId 		= userId;
		this.updated 		= new ArrayList<DateTime>();
		this.updated.add(updated);
		this.zipcode 		= new ArrayList<Integer>();
		this.zipcode.add(zipcode);
		this.dateOfBirth 	= dateOfBirth;
		this.gender 		= gender;
	}
	
	public User(String[] row) throws Exception{
		this.userId 		= Long.parseLong(row[0]);
		this.updated = new ArrayList<DateTime>();
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
	
	public Vector getContextVector(DateTime date) {
		Vector age = getAge(date);
		Vector gender = getGenderContext();
		double zipFocus = 1.0;
		return new DenseVector(new double[]{age.get(0),age.get(1), age.get(2), gender.get(0), gender.get(1), zipFocus});
	}
	
	public Vector getUserContextVector(Matrix dm, DateTime date) {
		return dm.times(this.getContextVector(date));
	}
}
