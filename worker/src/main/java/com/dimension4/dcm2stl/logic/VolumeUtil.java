package com.dimension4.dcm2stl.logic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import toxi.geom.Vec3D;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;
import toxi.geom.mesh.Vertex;

public final class VolumeUtil {

    private static final Logger LOGGER = Logger.getLogger(VolumeUtil.class.getName());

    private static final boolean DEBUG = false;

    // volume treshold
    private static final float VOLUME_TRESHOLD = 50f;

    public static TriangleMesh filterNoise(TriangleMesh mesh) {
        // Fix floating point errors
        fixFLoatingPointErrors(mesh);

        // map edges to the index of buckets
        HashMap<Edge, BucketNode> edgeMap = new HashMap<>();
        // keep a list of buckets to refer to via the map entry index
        HashSet<BucketNode> roots = new HashSet<>();
        List<Face> triangles = mesh.getFaces();
        //LOGGER.log(Level.INFO, "Iterating over {0} faces", triangles.size());

        for (int index = 0; index < triangles.size(); index++) {
            // get the current face from the list
            Face triangle = triangles.get(index);

            // get the edges for the current face
            Edge e0 = new Edge(triangle.a, triangle.b);
            Edge e1 = new Edge(triangle.b, triangle.c);
            Edge e2 = new Edge(triangle.c, triangle.a);

            // Get the BucketNodes corresponding with the edges (if they exist)
            BucketNode b0 = edgeMap.get(e0);
            BucketNode b1 = edgeMap.get(e1);
            BucketNode b2 = edgeMap.get(e2);

            // Create new Nodes when necessary
            if(b0 == null){
                b0 = new BucketNode(e0);
                edgeMap.put(e0, b0);
            }
            if(b1 == null){
                b1 = new BucketNode(e1);
                edgeMap.put(e1, b1);
            }
            if(b2 == null){
                b2 = new BucketNode(e2);
                edgeMap.put(e2, b2);
            }

            // Get the root of each tree
            BucketNode r0 = b0.getRoot();
            BucketNode r1 = b1.getRoot();
            BucketNode r2 = b2.getRoot();

            // join all these root nodes into one new root node
            BucketNode bNew = join(r0, r1, r2, triangle);

            // remove old roots and add new root
            roots.remove(r0);
            roots.remove(r1);
            roots.remove(r2);
            roots.add(bNew);
        }
        // prepare a new mesh to copy the passing faces into
        TriangleMesh newMesh = new TriangleMesh();
        // some metrics for debugging
        int countRemoved = 0;
        int countRemovedVolumes = 0;
        int countFaces = 0;

        for(BucketNode b:roots) {
            List<Face> faces = b.getTreeFaces();
            countFaces += faces.size();

            // calculate the volume of the group
            float volume = calcVolume(faces);

            LOGGER.log(Level.INFO, "Bucket b {0} has {1} faces and volume {2}", new Object[]{b.hashCode(), faces.size(), volume});

            if (volume < VOLUME_TRESHOLD) {
                countRemoved += faces.size();
                countRemovedVolumes++;
            } else {
                for (Face f : faces) {
                    newMesh.addFace(f.a, f.b, f.c);
                }
            }
        }

        LOGGER.log(Level.INFO, "{0} Buckets counted a total of {1} faces", new Object[]{roots.size(), countFaces});
        LOGGER.log(Level.INFO, "Removed {0} total faces ({1} sub-volumes)", new Object[]{countRemoved, countRemovedVolumes});
        LOGGER.log(Level.INFO, "Done \n\n");
        mesh = newMesh; // returning is not necessary
        return newMesh;
    }

    private static float calcVolume(Collection<Face> triangles) {
        float sum = 0f;
        for (Face triangle : triangles) {
            sum += signedVolumeOfTriangle(triangle.a, triangle.b, triangle.c);
        }
        sum = Math.abs(sum);
        return sum;
    }

    private static float signedVolumeOfTriangle(Vec3D p1, Vec3D p2, Vec3D p3) {
        float v321 = p3.x * p2.y * p1.z;
        float v231 = p2.x * p3.y * p1.z;
        float v312 = p3.x * p1.y * p2.z;
        float v132 = p1.x * p3.y * p2.z;
        float v213 = p2.x * p1.y * p3.z;
        float v123 = p1.x * p2.y * p3.z;
        return (1.0f / 6.0f) * (-v321 + v231 + v312 - v132 - v213 + v123);
    }

    private static void fixFLoatingPointErrors(TriangleMesh mesh){
        LOGGER.log(Level.INFO, "Correcting floating point inaccuracies");
        float threshold = 0.001f;
        TreeSet<Float> numberTreeSet = new TreeSet<>();
        List<Face> triangles = mesh.getFaces();
        for (Face triangle : triangles) {
            for (Vertex v : triangle.getVertices(new Vertex[3])) {
                float[] newVals = new float[3];
                float[] vertexArray = v.toArray();
                for (int i = 0; i < vertexArray.length; i++) {
                    float number = vertexArray[i];

                    Float fetchFloor = numberTreeSet.floor(number);
                    Float fetchCeil = numberTreeSet.ceiling(number);

                    if (fetchFloor == null) {
                        fetchFloor = -Float.MAX_VALUE;
                    }
                    if (fetchCeil == null) {
                        fetchCeil = Float.MAX_VALUE;
                    }

                    float dFloor = number - fetchFloor;
                    float dCeil = fetchCeil - number;

                    if (dFloor < threshold || dCeil < threshold) {
                        if (dFloor > dCeil) {
                            number = fetchCeil;
                        } else {
                            number = fetchFloor;
                        }
                    }
                    numberTreeSet.add(number);
                    newVals[i] = number;
                }

                v.set(newVals[0], newVals[1], newVals[2]);
            }
        }
        LOGGER.log(Level.INFO, "Done \n\n");
    }

    private static BucketNode join(BucketNode b0, BucketNode b1, BucketNode b2, Face face) {
        if(b0.equals(b1) && b1.equals(b2)){
            // All nodes are the same, no need to merge
            b0.getFaces().add(face);
            return b0;
        }
        else{
            BucketNode bNew = new BucketNode(face);
            if(b0.equals(b1)){
                // merge b0 and b2
                bNew.getChildren().add(b0);
                bNew.getChildren().add(b2);
                b0.setParent(bNew);
                b2.setParent(bNew);
            }
            else if(b0.equals(b2) || b1.equals(b2)){
                // merge b0 and b1
                bNew.getChildren().add(b0);
                bNew.getChildren().add(b1);
                b0.setParent(bNew);
                b1.setParent(bNew);
            }
            else {
                // merge b0, b1 and b2
                bNew.getChildren().add(b0);
                bNew.getChildren().add(b1);
                bNew.getChildren().add(b2);
                b0.setParent(bNew);
                b1.setParent(bNew);
                b2.setParent(bNew);
            }
            return bNew;
        }
    }
}
