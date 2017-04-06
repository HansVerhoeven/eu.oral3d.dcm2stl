package com.dimension4.dcm2stl.logic;

import toxi.geom.mesh.Face;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BucketNode {
    private Edge edge;
    private List<Face> faces;
    private BucketNode parent;
    private List<BucketNode> children;
    private ShortcutNode shortCut;

    public BucketNode(Face face) {
        this.faces = new ArrayList<>();
        this.children = new ArrayList<>();
        this.faces.add(face);
        this.shortCut = null;
    }

    public BucketNode(Edge edge) {
        this.edge = edge;
        this.faces = new ArrayList<>();
        this.children = new ArrayList<>();
        this.shortCut = null;
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public BucketNode getParent() {
        return parent;
    }

    public void setParent(BucketNode parent) {
        this.parent = parent;
    }

    public List<BucketNode> getChildren() {
        return children;
    }

    public void setChildren(List<BucketNode> children) {
        this.children = children;
    }

    public BucketNode getRoot(){
        BucketNode curr = this;
        BucketNode next;
        ShortcutNode shortcutNode = new ShortcutNode();
        while(true){
            if(curr.parent == null){
                shortcutNode.setTarget(curr);
                return curr;
            }
            else if(curr.shortCut != null){
                next = curr.shortCut.getTarget();
                curr.shortCut = shortcutNode;
            }
            else {
                next = curr.parent;
                curr.shortCut = shortcutNode;
            }
            curr = next;
        }
    }

    public List<Face> getTreeFaces(){
        List<Face> faces = new ArrayList<>();
        Queue<BucketNode> nodesToVisit =new LinkedList<>();
        nodesToVisit.add(this);

        BucketNode currentnode;
        while(!nodesToVisit.isEmpty()) {
            currentnode = nodesToVisit.poll();
            nodesToVisit.addAll(currentnode.children);
            faces.addAll(currentnode.faces);
        }
        return faces;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public void setFaces(List<Face> faces) {
        this.faces = faces;
    }
}
