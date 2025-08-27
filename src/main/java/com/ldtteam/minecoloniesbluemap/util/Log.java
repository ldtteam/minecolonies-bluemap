package com.ldtteam.minecoloniesbluemap.util;

import com.ldtteam.minecoloniesbluemap.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Logging utility class.
 */
public final class Log
{
    /**
     * Mod logger.
     */
    private static final Logger logger = LogManager.getLogger(Constants.MOD_ID);

    /**
     * Private constructor to hide the public one.
     */
    private Log()
    {
    }

    /**
     * Getter for the minecolonies Logger.
     *
     * @return the logger.
     */
    public static Logger getLogger()
    {
        return logger;
    }
}

