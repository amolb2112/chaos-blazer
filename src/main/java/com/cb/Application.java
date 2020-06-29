package com.cb;


import com.cb.util.ChaosUtil;
import com.cb.client.MarathonWrapper;
import com.cb.client.SSHWrapper;
import com.cb.model.InputParams;
import com.cb.model.StrategyType;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.Task;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by amolb2112 on 5/17/17.
 */
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private InputParams inputParams;

    public static void main(String[] args) throws Exception {
        Application application = new Application();

        // init
        application.inputParams = getConfiguration(args);
        MarathonWrapper marathonWrapper
                = new MarathonWrapper(application.inputParams,
                                        ChaosUtil.getConfiguration(application.inputParams.getEnvironment()));

        // get app and tasks information
        GetAppResponse app =  marathonWrapper.getApp(application.inputParams.getAppId());
        Collection<Task> tasks =  marathonWrapper.getAppTasks(application.inputParams.getAppId());
        LOGGER.info("Number of tasks - {}, List - {}",tasks.size(),tasks.toString());

        // execute chaos strategy
        switch (application.inputParams.getStrategy()){
            case KILLINSTANCE:
                killInstances(application.inputParams,marathonWrapper, tasks);
                break;
            case KILLNETWORK:
                killNetwork(app.getApp().getContainer().getDocker().getImage(),tasks);
                break;
        }
    }

    private static InputParams getConfiguration(String[] args) throws Exception {
        Options options = new Options();

        options.addOption("e", true, "environment");
        options.addOption("a", true, "application identifier");
        options.addOption("f", true, "fraction of tasks to kill");
        options.addOption("s", true, "strategy for attack");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        InputParams inputParams = new InputParams();
        inputParams.setEnvironment(cmd.getOptionValue("e"));
        inputParams.setAppId(cmd.getOptionValue("a"));
        String fOptionValue = cmd.getOptionValue("f");
        if(fOptionValue !=null) {
            inputParams.setFractionOfTasksToKill(Float.valueOf(fOptionValue));
        }
        inputParams.setStrategy(StrategyType.valueOf(cmd.getOptionValue("s")));

        return inputParams;
    }

    private static void killNetwork(String appDockerImage,Collection<Task> tasks) throws Exception {
        for(Task task:tasks){
            SSHWrapper sshWrapper = new SSHWrapper();
            sshWrapper.failNetwork(task.getHost(),"vagrant",appDockerImage);
        }
    }

    private static void killInstances(InputParams inputParams, MarathonWrapper marathonWrapper, Collection<Task> tasks) throws Exception {
        //TODO - FEATURE - Pick random tasks?
        //TODO - FEATURE - probability of kill
        //TODO - FEATURE - check if good time to proceed further

        int numOfTasksToKill = Math.round(tasks.size()* inputParams.getFractionOfTasksToKill());
        LOGGER.info("Number of tasks to kill as per fraction - {}",numOfTasksToKill);

        if(numOfTasksToKill > 0) {
            int i = 0;
            for (Task task : tasks) {
                if(i++ >= numOfTasksToKill) {
                    break;
                }

                marathonWrapper.deleteAppTask(inputParams.getAppId(),task.getId());
                LOGGER.info("Deleted Application, appId = {}, taskId = {}", inputParams.getAppId(),task.getId());
            }
        }
    }
}
