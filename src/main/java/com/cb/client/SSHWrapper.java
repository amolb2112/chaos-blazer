package com.cb.client;

import com.cb.Application;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SSHWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final JSch jsch;

    public static final String DOCKER_EXEC_CMD_PREFIX = "docker exec -i ";
    public static final String DOCKER_EXEC_CMD_NETWORK_FAILURE_RESET = " /usr/bin/sab reset";
    public static final String DOCKER_EXEC_CMD_NETWORK_FAILURE_CMD = " /usr/bin/sab add --fault_type NETWORK_FAILURE --to_port 8090 --direction IN";

    static {
        jsch = new JSch();
        try {
            jsch.addIdentity("/Users/amolb2112/workspace/tools/dcos-vagrant/.vagrant/dcos/private_key_vagrant");
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    public Session getSession(String host,String username) throws Exception {
        Session session;

        session = jsch.getSession(username,host);
        session.setConfig("StrictHostKeyChecking", "no");

        System.out.println("Establishing connection to the docker container host...");
        session.connect();
        System.out.println("Connection established to the docker container host.");

        return session;
    }

    /**
     * Execute a command on the ssh channel
     *
     * @param session
     * @param command
     * @throws IOException
     * @throws JSchException
     */
    public List<String> executeCommand(Session session, String command) throws IOException, JSchException {
        Channel channel=session.openChannel("exec");
        ((ChannelExec)channel).setCommand(command);

        channel.setInputStream(null);

        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut);
        ((ChannelExec)channel).setErrStream(new PrintStream(pipeOut));

        List<String> outputStrings = new ArrayList();
        List<String> errorStrings = new ArrayList();
        String line;

        InputStream inputStream = channel.getInputStream();

        channel.connect();
        BufferedReader bufferedOutputReader = new BufferedReader(new InputStreamReader(inputStream));
        BufferedReader bufferedErrorReader = new BufferedReader(new InputStreamReader(pipeIn));

        while(true){
            while ((line = bufferedOutputReader.readLine()) != null){
                outputStrings.add(line);
            }
            while ((line = bufferedErrorReader.readLine()) != null){
                errorStrings.add(line);
            }

            if(channel.isClosed()){
                if(inputStream.available()>0) continue;
                System.out.println("exit-status: "+channel.getExitStatus());
                break;
            }
            try{Thread.sleep(1000);}catch(Exception ee){}
        }

        channel.disconnect();

        return outputStrings;
    }

    /**
     * Fail the network of a host
     *
     * @param host
     * @param userName
     * @param imageName
     * @throws Exception
     */
    public void failNetwork(String host, String userName, String imageName) throws Exception {
        Session session = this.getSession(host,userName);

        System.out.println(
                this.executeCommand(session,
                        DOCKER_EXEC_CMD_PREFIX + getContainerName(session,imageName) + DOCKER_EXEC_CMD_NETWORK_FAILURE_CMD));
    }

    private String getContainerName(Session session, String imageName) throws IOException, JSchException {
        List<String> outputStrings = this.executeCommand(session,"docker ps");

        for (int i=1; i<outputStrings.size();i++){
            String[] partsStr = outputStrings.get(i).split("\\s+");
            String imageNameFromJob = partsStr[1];
            if(imageName.equals(imageNameFromJob)){
                return partsStr[11];
            }
        }
        return null;
    }

    public void resetNetwork(String host, String userName, String imageName) throws Exception {
        Session session = this.getSession(host,userName);

        System.out.println(this.executeCommand(session, DOCKER_EXEC_CMD_PREFIX + getContainerName(session,imageName) + DOCKER_EXEC_CMD_NETWORK_FAILURE_RESET));

    }
    public static void main(String[] args) throws Exception {
        SSHWrapper sshWrapper = new SSHWrapper();
        sshWrapper.resetNetwork("192.168.65.111","vagrant","amolb2112/bookstore-service");
    }
}