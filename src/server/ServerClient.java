package server;

import java.net.*;

public class ServerClient {
    
    public String userName;
    public InetAddress address;
    public int port;
    private final int ID;
    public int attempt = 0;
    public String action;
    
    public ServerClient(String userName, InetAddress address, int port, final int ID, String action){
        this.userName = userName;
        this.address = address;
        this.port = port ;
        this.ID = ID;
        this.action = action;
    }
    
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }
    
    public int getID(){
        return ID;
    }
    
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    
}
