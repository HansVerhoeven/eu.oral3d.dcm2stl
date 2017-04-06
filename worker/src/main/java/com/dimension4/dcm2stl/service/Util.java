package com.dimension4.dcm2stl.service;

import java.util.UUID;

/**
 *
 * @author Brecht
 */
public final class Util {
    public static String generateId() {
        return UUID.randomUUID().toString();
    }
}
