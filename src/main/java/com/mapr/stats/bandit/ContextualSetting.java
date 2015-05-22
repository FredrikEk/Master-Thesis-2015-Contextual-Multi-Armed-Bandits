package com.mapr.stats.bandit;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.mapr.objects.ConstantHolder;
import com.mapr.objects.Item;
import com.mapr.objects.Order;
import com.mapr.objects.User;

public class ContextualSetting {

	static int debugOutPrint = 100000;
	static String csvSplitBy = ",";
	
	HashMap<Long, Item> items = new HashMap<Long, Item>();
	HashMap<Long, User> users = new HashMap<Long, User>();
	HashMap<Long, Order> orders = new HashMap<Long, Order>();
	HashMap<Long, Item> itemIndexMap = new HashMap<Long, Item>();
	ArrayList<Order> ordersByDate = new ArrayList<Order>();
	int debug;
	
	public ContextualSetting(BufferedReader[] brs) {
		debug = 0;
		try {
			readItems(brs[0]);
			//List<Long> keyDomain = new ArrayList<Long>();
			//for(long i = 0; i < itemIndexMap.size(); i++) {
			//	keyDomain.add(i);
			//}
			//BanditHittepa2.KeyDomain = keyDomain;
			ConstantHolder.numberOfItems = itemIndexMap.size();
			readUsers(brs[1]);
			readOrders(brs[2]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public ContextualSetting(ContextualSetting cs) {
		Set<Long> itemKeys = cs.getItemKeySet();
		Set<Long> userKeys = cs.getUserKeySet();
		ArrayList<Order> orderedOrders = cs.getOrdersByDate();
		
		for(Long key : itemKeys) {
			Item i = new Item(cs.getItem(key));
			items.put(key, i);
			itemIndexMap.put(i.getIndex(), i);
		}
		for(Long key : userKeys) {
			users.put(key, new User(cs.getUser(key)));
		}
		for(Order orderCopy : orderedOrders) {
			
			Order o;
			if(!orders.containsKey(orderCopy.getOrderId())) {
				Long userId = orderCopy.getUser().getUserId();
				o = new Order(users.get(userId), orderCopy.getOrderId(), orderCopy.getPlacedOrder());
				this.ordersByDate.add(o);
				this.orders.put(orderCopy.getOrderId(), o);
			} else {
				o = this.orders.get(orderCopy.getOrderId());
			}
			
			for(Item i : orderCopy.getItems()) {
				o.addItem(items.get(i.getProductId()));
			}
		}
		
		
	}
	
	private void readItems(BufferedReader br) throws Exception {
		String line = "";
		while ((line = br.readLine()) != null) {
		    // use comma as separator
			debug++;
			if(debug > debugOutPrint) {
				System.out.println("Items: " + line);
				ConstantHolder.logWriter.println("Items: " + line);
				debug = 0;
			}
			String[] row = line.split(csvSplitBy);
			Item i = new Item(row);
			if(items.containsKey(i.getProductId())) {
				items.get(i.getProductId()).update(i);
			} else {
				int index = items.size();
				items.put(i.getProductId(), i);
				i.setIndex(index);
				itemIndexMap.put((long) index, i);
			}
		}
	}
	
	private void readUsers(BufferedReader br) throws Exception {
		String line = "";
		while ((line = br.readLine()) != null) {
		    // use comma as separator
			debug++;
			if(debug > debugOutPrint) {
				System.out.println("Users: " + line);
				ConstantHolder.logWriter.println("Users: " + line);
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
	}
	
	private void readOrders(BufferedReader br) throws Exception {
		String line = "";
		
		while ((line = br.readLine()) != null) {
		    // use comma as separator
			debug++;
			if(debug > debugOutPrint) {
				System.out.println("Orders: " + line);
				ConstantHolder.logWriter.println("Orders: " + line);
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
	}

	public Item getItem(Long key) {
		return items.get(key);
	}
	
	public User getUser(Long key) {
		return users.get(key);
	}
	
	public Order getOrder(Long key) {
		return orders.get(key);
	}
	
	public Order getOrderByDate(int index) {
		return ordersByDate.get(index);
	}
	
	public HashMap<Long, Item> getItems() {
		return items;
	}

	public void setItems(HashMap<Long, Item> items) {
		this.items = items;
	}

	public HashMap<Long, User> getUsers() {
		return users;
	}

	public void setUsers(HashMap<Long, User> users) {
		this.users = users;
	}

	public HashMap<Long, Order> getOrders() {
		return orders;
	}

	public void setOrders(HashMap<Long, Order> orders) {
		this.orders = orders;
	}

	public ArrayList<Order> getOrdersByDate() {
		return ordersByDate;
	}

	public void setOrdersByDate(ArrayList<Order> ordersByDate) {
		this.ordersByDate = ordersByDate;
	}
	
	public Set<Long> getItemKeySet() {
		return items.keySet();
	}
	
	public Set<Long> getUserKeySet() {
		return users.keySet();
	}
	
	public Set<Long> getOrderKeySet() {
		return orders.keySet();
	}
	
	public ContextualSetting copy() {
		return new ContextualSetting(this);
	}
	
	public Set<Long> getItemIndexKeySet() {
		return itemIndexMap.keySet();
	}
	
	public Item getItemFromIndex(Long index) {
		return itemIndexMap.get(index);
	}
}
