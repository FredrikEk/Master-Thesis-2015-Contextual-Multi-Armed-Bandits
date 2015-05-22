package com.mapr.objects;

import java.util.HashMap;

public class SparseVector {
    private final int N;
    private HashMap<Long, Double> st;  

    public SparseVector(int N) {
        this.N  = N;
        this.st = new HashMap<Long, Double>();
    }
    
    public SparseVector(SparseVector sp) {
    	this.N = sp.N;
    	this.st = new HashMap<Long, Double>(sp.st);
    }

    public void put(long i, double value) {
        if (i < 0 || i >= N) throw new RuntimeException("Illegal index");
        if (value == 0.0) st.remove(i);
        else              st.put(i, value);
    }

    public double get(long i) {
        if (i < 0 || i >= N) throw new RuntimeException("Illegal index");
        if (st.containsKey(i)) return st.get(i);
        else                return 0.0;
    }

    public int nnz() {
        return st.size();
    }

    public int size() {
        return N;
    }

    public double dot(SparseVector b) {
        SparseVector a = this;
        if (a.N != b.N) throw new RuntimeException("Vector lengths disagree");
        double sum = 0.0;

        // iterate over the vector with the fewest nonzeros
        if (a.st.size() <= b.st.size()) {
            for (long i : a.st.keySet())
                if (b.st.containsKey(i)) sum += a.get(i) * b.get(i);
        }
        else  {
            for (long i : b.st.keySet())
                if (a.st.containsKey(i)) sum += a.get(i) * b.get(i);
        }
        return sum;
    }

    // return the 2-norm
    public double norm() {
        SparseVector a = this;
        return Math.sqrt(a.dot(a));
    }

    // return alpha * a
    public SparseVector scale(double alpha) {
        SparseVector a = this;
        SparseVector c = new SparseVector(N);
        for (long i : a.st.keySet()) c.put(i, alpha * a.get(i));
        return c;
    }

    // return a + b
    public SparseVector plus(SparseVector b) {
        SparseVector a = this;
        if (a.N != b.N) throw new RuntimeException("Vector lengths disagree");
        SparseVector c = new SparseVector(N);
        for (long i : a.st.keySet()) c.put(i, a.get(i));                // c = a
        for (long i : b.st.keySet()) c.put(i, b.get(i) + c.get(i));     // c = c + b
        return c;
    }

    // return a string representation
    public String toString() {
        String s = "";
        for (long i : st.keySet()) {
            s += "(" + i + ", " + st.get(i) + ") ";
        }
        return s;
    }
    
    public SparseVector clone() {
    	return new SparseVector(this);
    }

}


