package com.cb.model;

/**
 * Created by amolb2112 on 6/15/17.
 */
public class ChaosConfig {
    private boolean securedSSL;
    private String marathonHost;
    private String marathonUser;
    private String marathonPassword;

    public boolean isSecuredSSL() {
        return securedSSL;
    }

    public void setSecuredSSL(boolean securedSSL) {
        this.securedSSL = securedSSL;
    }

    public String getMarathonHost() {
        return marathonHost;
    }

    public void setMarathonHost(String marathonHost) {
        this.marathonHost = marathonHost;
    }

    public String getMarathonUser() {
        return marathonUser;
    }

    public void setMarathonUser(String marathonUser) {
        this.marathonUser = marathonUser;
    }

    public String getMarathonPassword() {
        return marathonPassword;
    }

    public void setMarathonPassword(String marathonPassword) {
        this.marathonPassword = marathonPassword;
    }
}
