package com.cb.client;

import com.cb.util.ChaosUtil;
import com.cb.model.ChaosConfig;
import com.cb.model.InputParams;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.GetAppTasksResponse;
import mesosphere.marathon.client.model.v2.Task;
import mesosphere.marathon.client.utils.MarathonException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.Collection;

/**
 * Created on 5/17/17.
 */
public class MarathonWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonWrapper.class);

    InputParams inputParams;
    ChaosConfig chaosConfig;

    public MarathonWrapper(InputParams inputParams,ChaosConfig chaosConfig) {
        this.inputParams = inputParams;
        this.chaosConfig = chaosConfig;
    }

    protected boolean appExists(Marathon marathon, String appId) throws Exception {
        try {
            marathon.getApp(ChaosUtil.trimLeadingSlash(appId));
            return true;
        } catch (MarathonException getAppException) {
            if (getAppException.getStatus() == 404) {
                return false;
            } else {
                throw new Exception("Failed to check if an app " + appId + " exists",
                        getAppException);
            }
        } catch (Exception e) {
            throw new Exception("Failed to check if an app " + appId + " exists", e);
        }
    }

    public Marathon getMarathonClient() {

        if (!chaosConfig.isSecuredSSL()) {
            hackSSL();
        }
        if (StringUtils.isNotEmpty(chaosConfig.getMarathonUser()) && StringUtils.isNotEmpty(chaosConfig.getMarathonPassword())) {
            return mesosphere.marathon.client.MarathonClient.getInstanceWithBasicAuth(
                    chaosConfig.getMarathonHost(),
                    chaosConfig.getMarathonUser(),
                    chaosConfig.getMarathonPassword());
        } else {
            return mesosphere.marathon.client.MarathonClient.getInstance(chaosConfig.getMarathonHost());
        }
    }

    private void hackSSL() {

        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        final HostnameVerifier trustAllHosts = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHosts);
        } catch (Exception e) {
        }
    }

    /**
     * Delete App Instance
     *
     * @throws Exception
     */
    public void deleteAppTask(String appId, String taskId) throws Exception {
        Marathon marathon = getMarathonClient();
        LOGGER.info("Deleting Marathon instance for " + appId);

        if (appExists(marathon, appId)) {

            try {
                marathon.deleteAppTask(appId,taskId,"1");
            } catch (Exception deleteAppException) {
                throw new Exception("Failed to delete Marathon instance "
                        + chaosConfig.getMarathonHost(), deleteAppException);
            }
        } else {
            LOGGER.warn(appId + " does not exist - nothing to delete");
        }
    }

    /**
     * Get App
     *
     * @throws Exception
     */
    public GetAppResponse getApp(String appId) throws Exception {
        final Marathon marathon = getMarathonClient();

        if(appId == null) {
            appId = inputParams.getAppId();
        }

        LOGGER.info("Looking up application in Marathon instance for AppId = " + appId);

        GetAppResponse getAppResponse = null;
        try {
            if (appExists(marathon, appId)) {
                getAppResponse = marathon.getApp(appId);
            } else {
                LOGGER.warn(appId + " does not exist");
            }
        } catch (final Exception e) {
            LOGGER.error("Problem communicating with Marathon", e);
        }
        return getAppResponse;
    }

    /**
     * Get App Tasks
     *
     * @throws Exception
     */
    public Collection<Task> getAppTasks(String appId) throws Exception {
        final Marathon marathon = getMarathonClient();

        if(appId == null) {
            appId = inputParams.getAppId();
        }

        LOGGER.info("Looking up tasks in Marathon instance for AppId = " + appId);

        GetAppTasksResponse getAppTasksResponse = null;
        try {
            if (appExists(marathon, appId)) {
                LOGGER.info(appId + " exists - getting app tasks..");
                try {
                    getAppTasksResponse = marathon.getAppTasks(appId);
                } catch (Exception deleteAppException) {
                    throw new Exception("Failed to get tasks for Marathon instance "
                            + chaosConfig.getMarathonHost(), deleteAppException);
                }
            } else {
                LOGGER.warn(appId + " does not exist");
            }
        } catch (final Exception e) {
            LOGGER.error("Problem communicating with Marathon", e);
        }
        return getAppTasksResponse.getTasks();
    }

}
