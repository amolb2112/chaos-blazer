package com.cb.model;

/**
 *
 * Created by amolb2112 on 5/17/17.
 */
public class InputParams {
    private String appId;
    private StrategyType strategy;
    private String environment;

    // Kill Strategy params
    private float frequencyOfKillAttempts; //Kill attempts per day
    private float probabilityOfKillSuccess;
    private float fractionOfTasksToKill;

    //Network strategy params

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public float getFrequencyOfKillAttempts() {
        return frequencyOfKillAttempts;
    }

    public void setFrequencyOfKillAttempts(float frequencyOfKillAttempts) {
        this.frequencyOfKillAttempts = frequencyOfKillAttempts;
    }

    public float getProbabilityOfKillSuccess() {
        return probabilityOfKillSuccess;
    }

    public void setProbabilityOfKillSuccess(float probabilityOfKillSuccess) {
        this.probabilityOfKillSuccess = probabilityOfKillSuccess;
    }

    public float getFractionOfTasksToKill() {
        return fractionOfTasksToKill;
    }

    public void setFractionOfTasksToKill(float fractionOfTasksToKill) {
        this.fractionOfTasksToKill = fractionOfTasksToKill;
    }

    public StrategyType getStrategy() {
        return strategy;
    }

    public void setStrategy(StrategyType strategy) {
        this.strategy = strategy;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
