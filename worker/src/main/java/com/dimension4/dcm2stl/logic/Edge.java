package com.dimension4.dcm2stl.logic;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import toxi.geom.mesh.Vertex;

public class Edge {

    private static final Logger LOGGER = Logger.getLogger(Edge.class.getName());

    public Vertex a;
    public Vertex b;

    private final boolean debug = false;

    public Edge(Vertex a, Vertex b) {
        // sort the vertices at the start
        // that way we don't have to bother with checking interchangability during equals & hashcode
        int compVal = compareVerteces(a, b);
        if (debug) {
            LOGGER.log(Level.INFO, "compVal {0}", compVal);
        }
        // this means that a is larger then b, so we swap
        if (compVal > 0) {
            Vertex tmpA = a;
            a = b;
            b = tmpA;
        }
        this.a = a;
        this.b = b;
        if (debug) {
            LOGGER.log(Level.INFO, "a is {0}", a);
            LOGGER.log(Level.INFO, "b is {0}", b);
        }
    }

//    @Override
//    public boolean equals(Object obj) {
//        // a -> b or b -> a has to be the same
//        if(debug) {
//            LOGGER.log(Level.INFO, "Equals called");
//        }
//        if (obj instanceof Edge) {
//            Edge other = (Edge) obj;
//            boolean one = this.a.equals(other.b) || this.a.equals(other.a);
//            boolean two = this.b.equals(other.a) || this.b.equals(other.b);
//            return one && two;
//        } else {
//            return false;
//        }
//    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Edge other = (Edge) obj;
        if (!Objects.equals(this.a, other.a)) {
            return false;
        }
        if (!Objects.equals(this.b, other.b)) {
            return false;
        }
        return true;
    }

//    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 79 * hash + Objects.hashCode(this.a);
//        hash = 79 * hash + Objects.hashCode(this.b);
//        if(debug) {
//            LOGGER.log(Level.INFO, "Hashcode is {0}", hash);
//        }
//        return hash;
//    }
//    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 53 * hash + (int)(this.a.x + this.a.y + this.a.z + this.b.x + this.b.y + this.b.z);        
//        return hash;
//    }
    // vertex error, coordinate the same equals error
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(a.x);
        builder.append(a.y);
        builder.append(a.z);

        builder.append(b.x);
        builder.append(b.y);
        builder.append(b.z);

        return builder.toHashCode();
    }

    private int compareVerteces(Vertex a, Vertex b) {
        if (a.x < b.x) {
            return -1;
        } else if (a.x > b.x) {
            return 1;
        }
        if (a.y < b.y) {
            return -1;
        } else if (a.y > b.y) {
            return 1;
        }
        if (a.z < b.z) {
            return -1;
        } else if (a.z > b.z) {
            return 1;
        }
        return 0;
    }
//    private int compareVerteces(Vertex a, Vertex b) {
////        if (a.x == b.x && a.y == b.y && a.z == b.z) {
////            return 0;
////        }
//        int distA = (int)(a.x * a.x + a.y * a.y + a.z * a.z);
//        int distB = (int)(b.x * b.x + b.y * b.y + b.z * b.z);
//        return (distA - distB);
//    }

}
