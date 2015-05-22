package JaccardSimilarity;

import java.util.HashMap;

import org.apache.mahout.math.DenseVector;

import com.mapr.objects.Item;
import com.mapr.objects.SparseVector;
import com.mapr.objects.User;

public class JaccardDistanceAlgorithm {

	private HashMap<Long, Double> itemRecommendation = new HashMap<Long, Double>();
	private User baseUser;
	private SparseVector baseVector;
	
	public JaccardDistanceAlgorithm(User u) {
		baseUser = u;
		baseVector = u.getItemVector();
	}
	
	public void addNewUser(User u2) {
		double jaccardDistance = calculateJaccardDistance(u2);
		
		if(jaccardDistance > 0) {
			for(Item i : u2.getItemList()) {
				Long index = (long) i.getIndex();
				if(itemRecommendation.containsKey(index)) {
					itemRecommendation.put(index, itemRecommendation.get(index) + jaccardDistance);
				} else {
					itemRecommendation.put(index, jaccardDistance);
				}
			}
		}
	
	}
	
	private double calculateJaccardDistance(User u) {
		
		double intersection = baseVector.dot(u.getItemVector());
		
		SparseVector msp = baseVector.clone();
		
		msp.plus(u.getItemVector());
		double union = msp.nnz();
		if(intersection == 0.0 || union == 0.0) {
			return 0.0;
		} else {
			return intersection/union;
		}
	}
	
	public HashMap<Long, Double> getRecommendationMap() {
		return itemRecommendation;
	}
}
