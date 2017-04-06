package com.dimension4.dcm2stl.logic;

import java.util.HashSet;

public class Bucket {    
    private HashSet<Integer> indices;

    public Bucket() {
        this.indices = new HashSet<>();
    }

    public HashSet<Integer> getIndices() {
        return indices;
    }
    
    public boolean addIndex(int index) {
        return this.indices.add(index);
    }
    
    public void merge(Bucket otherBucket) {
        if(otherBucket != null) {
            this.indices.addAll(otherBucket.getIndices());
        }
    }

}
