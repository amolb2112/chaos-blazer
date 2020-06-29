package com.cb.util;

import com.cb.model.ChaosConfig;
import java.io.IOException;

public class ChaosUtil {

    private ChaosUtil() {}

    public static String trimLeadingSlash(final String appId) {
        if (appId != null && appId.startsWith("/")) {
            return appId.substring(1);
        } else {
            return appId;
        }
    }

    public static ChaosConfig getConfiguration(String env) throws IOException{
        ChaosConfig chaosConfig
                = YamlUtils.load("configuration-"+env+".yml", ChaosConfig.class);
        return chaosConfig;
    }
}
