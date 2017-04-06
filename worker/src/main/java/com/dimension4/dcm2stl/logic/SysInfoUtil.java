package com.dimension4.dcm2stl.logic;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class SysInfoUtil {
    
    private static final Logger LOGGER = Logger.getLogger(SysInfoUtil.class.getName());
        
    public static void memInfo() {
        final int MB = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();

        LOGGER.log(Level.INFO, "Used memory {0}", (runtime.totalMemory() - runtime.freeMemory()) / MB);
        LOGGER.log(Level.INFO, "Free memory {0}", (runtime.freeMemory()) / MB);
        LOGGER.log(Level.INFO, "Total memory {0}", (runtime.totalMemory()) / MB);
        LOGGER.log(Level.INFO, "Max memory {0}", (runtime.maxMemory()) / MB);
    }
}
