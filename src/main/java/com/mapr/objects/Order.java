package com.mapr.objects;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Order {
	
	private User user;
	private long orderId;
	private List<Item> items;
	private DateTime placedOrder;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public DateTime getPlacedOrder() {
		return placedOrder;
	}

	public void setPlacedOrder(DateTime placedOrder) {
		this.placedOrder = placedOrder;
	}

	public DateTimeFormatter getDf() {
		return df;
	}

	public void setDf(DateTimeFormatter df) {
		this.df = df;
	}

	public DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

	
	public Order(User user, long orderId, String date) throws Exception{
		this.user 			= user;
		this.orderId 		= orderId;
		this.items 			= new ArrayList<>();
		this.placedOrder 	= df.parseDateTime(date.substring(0,19));
	}
	
	public Order(User user, long orderId, DateTime date) {
		this.user 			= user;
		this.orderId 		= orderId;
		this.items 			= new ArrayList<>();
		this.placedOrder 	= new DateTime(date);
	}
	
	public void addItem(Item i){
		items.add(i);
	}
}
