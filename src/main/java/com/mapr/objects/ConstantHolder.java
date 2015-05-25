package com.mapr.objects;

import java.io.File;
import java.io.PrintWriter;

import org.joda.time.DateTime;

public class ConstantHolder {

	public static final boolean useCategories = true;
    public static final boolean useGender = true;
    public static final boolean useAge = true;
    public static final boolean useLocation = true;
    public static final boolean usePopularity = true;
    // 771338 - 777471, 1 week, 2012-05-01
    // 771338 - 788838, 1 month, 2012-05-01 This is not accurate, but approx. correct.
    // 771338 - 859594, 3 months, 2012-05-01
    // 771338 - 936163, 6 months, 2012-05-01
    // 771338 - 1091338, 1 year, 2012-05-01
 	public static final int TYPE_RANDOM_MATRIX = 1;
 	public static final int TYPE_NORMAL = 2;
 	public static final int TYPE_MOST_BUYS = 4;
 	public static final int TYPE_JACCARD_DISTANCE = 8;
 	// Should be set to 700 000
 	public static final int startPlace = 700000;
 	public static final int numberOfTraining = 1000; // 71338
 	public static final int trainingSet = startPlace + numberOfTraining;
 	public static final int numberOfTests = 2000;
 	public static final int orderToEndAt = trainingSet + numberOfTests;
 	public static final int numberOfArms = 50;
 	public static final int numberOfFeatures = 0 + (useAge	 		? 3 : 0) + 
 												   (useGender 		? 2 : 0) + 
												   (useLocation 	? 1 : 0) + 
												   (usePopularity 	? 2 : 0) + 
												   (useCategories 	? Category.numberOfCategories : 0);
 	public static final int extraFeatures = 10;
 	public static final int debugOutPrint = 100000;
 	public static final int itemsRecommendedPerTurn = 10;
	public static final boolean baseLine = true;
	public static PrintWriter logWriter = null;
	public static PrintWriter plotWriter = null;
	
	
	public static int numberOfItems;
	
	public static String logFileName;
	public static String plotFileName;
	public static int TYPE = TYPE_NORMAL | TYPE_MOST_BUYS | TYPE_JACCARD_DISTANCE;		
 	
	public static void initialize() {
		
		logFileName = "logs/logfile" + DateTime.now().toString().replaceAll("[^a-zA-Z0-9.-]", "_");
	 	plotFileName = "textPlot/logfile" + DateTime.now().toString().replaceAll("[^a-zA-Z0-9.-]", "_");
		
		try {
			logWriter = new PrintWriter(new File(logFileName + ".log"));
			plotWriter = new PrintWriter(new File(plotFileName + ".log"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}
