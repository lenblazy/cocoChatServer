package server;

import interactions.Message;
import interactions.MessageType;
import static interactions.MessageType.*;
import interactions.Status;
import interactions.User;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread implements Runnable {

    static String info;
    public static List<ServerClient> clients = new ArrayList<>();
    private List<Integer> clientResponse = new ArrayList<>();
    static DatagramSocket udpSocket;
    public ServerSocket tcpServerSocket;
    private Socket tcpSocket;
    private static Thread run, receive, manage;
    private static boolean running = false;
    private static String fName, lName, uName, pWord, email, phone, gender, county, occupation, birthDate, status;
    private static String loginUser, loginPass;
    private static final int maxClientsCount = 20;
    private static final clientThread[] threads = new clientThread[maxClientsCount];
    private final int MAX_ATTEMPTS = 5;
    private int ID;
    ServerClient c = null;
    private static final HashMap<String, User> names = new HashMap<>();
    private static HashSet<ObjectOutputStream> writers = new HashSet<>();
    private static ArrayList<User> users = new ArrayList<>();

    ServerThread() throws IOException {
        tcpServerSocket = new ServerSocket(1357);
        System.out.println("Waiting for tcpconnections");
        udpSocket = new DatagramSocket(3579);
        System.out.println("UDP started on port: 3579");
        run = new Thread(this, "Server");
        run.start();
    }

    @Override
    public void run() {
        running = true;
        manageClients();
        receive();
    }

    public void stopServer() {
        udpSocket.close();
        try {
            running = false;
            tcpServerSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void manageClients() {
        manage = new Thread("Manage") {
            @Override
            public void run() {
                while (running) {
                    try {
                        tcpSocket = tcpServerSocket.accept();
                        System.out.println("Accepted connection");
                        int i;
                        for (i = 0; i < maxClientsCount; i++) {
                            if (threads[i] == null) {
                                (threads[i] = new clientThread(tcpSocket, threads)).start();
                                break;
                            }
                        }
                        if (i == maxClientsCount) {
                            PrintStream os = new PrintStream(tcpSocket.getOutputStream());
                            os.println("Server too busy");
                            os.close();
                            tcpSocket.close();
                        }
                        // managing
                    } catch (IOException ex) {
                        Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        manage.start();
    }

    private void receive() {
        receive = new Thread("Receive") {
            @Override
            public void run() {
                while (running) {
                    // datagram protocol
                    byte[] data = new byte[1024];
                    DatagramPacket udpPacket = new DatagramPacket(data, data.length);
                    try {
                        udpSocket.receive(udpPacket);
                    } catch (IOException ex) {
                    }
                    process(udpPacket);
                }
            }
        };
        receive.start();
    }

    private static void process(DatagramPacket udpPacket) {
        String string = new String(udpPacket.getData());
        System.out.println(string);
        //use this to send audion and video
    }

    private static boolean saveUser() {
        boolean saved = ServerDbManager.registerUser(fName, lName, uName, pWord, email, phone, gender, county, occupation, birthDate, status);
        return saved;
    }

    private class clientThread extends Thread {

        private String name;
        private User user;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private Socket clientSocket;
        private final clientThread[] threads;
        private int maxClientsCount;
        String clientName = null;

        public clientThread(Socket clientSocket, clientThread[] threads) {
            this.clientSocket = clientSocket;
            this.threads = threads;
            maxClientsCount = threads.length;
        }

        @Override
        public void run() {
            
                int maxClientsCount = this.maxClientsCount;
                clientThread[] threads = this.threads;
                serverCommands();
                
                
                try {
                output = new ObjectOutputStream(clientSocket.getOutputStream());
                writers.add(output);
                input = new ObjectInputStream(clientSocket.getInputStream());

                while (clientSocket.isConnected()) {
                    Message incoming = (Message) input.readObject();
                    switch (incoming.getType()) {
                        case CONNECTED:
                            connected(incoming);
                            break;
                        case UPDATE:
                            write(incoming);
                            break;
                        case STATUS:
                            changeStatus(incoming);
                            break;
                        case LOGIN: //done
                            login(incoming);
                            break;
                        case REGISTER: //done
                            register(incoming);
                            break;
                        case REGISTERED:
                            System.out.println(incoming.getUsername() + incoming.getMsg());
                            break;
                        case PRIVATEMESSAGE:
                            send(incoming);
                            break;
                        case GROUPMESSAGE:
                            grpSend(incoming);
                            break;
                        case PING:
                            String thedata = incoming.getMsg();
                            clientResponse.add(Integer.parseInt(thedata.split("/i/|/e/")[1]));
                            break;
                        case DISCONNECTED:
                            String data = incoming.getMsg();
                            if (data.startsWith("/d/")) {
                                input.close();
                            } else if (data.startsWith("/l/")) {
                                int id = Integer.parseInt(data.split("/l/|/e/")[1]);
                                boolean left = disconnect(id, true);
                                if (left) {
                                    input.close();
                                }
                            }
                    }
                }
            } catch (IOException ex) {
                synchronized (this) {
                    /*Clean up. Set the current thread variable to null so that a new client could be accepted by the server. */
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] == this) {
                            threads[i] = null;
                            System.out.println("one thread released");
                        }
                    }
                }
                try {
                    output.close();
                    input.close();
                    clientSocket.close();
                    System.out.println("streams and sockets closed");
                } catch (IOException exe) {}
            } catch (ClassNotFoundException ex) {}
            
        }// end run method

        private boolean disconnect(int id, boolean status) {
            ServerClient c = null;
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).getID() == id) {
                    c = clients.get(i);
                    clients.remove(i);
                    break;
                }
            }
            String message;
            boolean connect = false;
            if (status) {
                message = "Client " + c.userName + " (" + c.getID() + ") @"
                        + c.address.toString() + ":" + c.port + " disconnected.";
                ServerWindow.getUsers(c.userName, c.address, c.port, c.getID(), " logged out ", connect);
                
            } else {
                message = "Client " + c.userName + " (" + c.getID() + ") @ "
                        + c.address.toString() + ":" + c.port + " timed out.";
                ServerWindow.getUsers(c.userName, c.address, c.port, c.getID(), " timed out ", connect);
            }
            System.out.println(message);
            names.remove(name);
            users.remove(user);
            writers.remove(output);
            Message msg = new Message();
            msg.setMsg("has left the chat.");
            msg.setUsername("SERVER");
            msg.setType(UPDATE);
            try {
                write(msg);
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }

        private void sendStatus() {
            int maxClientsCount = this.maxClientsCount;
            clientThread[] threads = this.threads;

            String users = "/u/";
            if (clients.size() <= 0) {
                return;
            }
            for (int i = 0; i < clients.size() - 1; i++) {
                users += clients.get(i).userName + "/n/";
            }
            users += clients.get(clients.size() - 1).userName + "/e/";

        }

        private void serverCommands() {
            Thread serverActs = new Thread("Server Commands") {
                @Override
                public void run() {
                    Scanner scanner = new Scanner(System.in);
                    while (running) {
                        String text = scanner.nextLine();
                        if (text.startsWith("/clients")) {
                            System.out.println("=========");
                            for (int i = 0; i < clients.size(); i++) {
                                ServerClient c = clients.get(i);
                                System.out.println(loginUser + " (" + c.getID() + "): " + c.address.toString()
                                        + ": " + c.port);
                            }
                            System.out.println("=========");
                        } else if (text.startsWith("kick")) {
                            String name = text.split(" ")[1];
                            int id = -1;
                            boolean num = true;
                            try {
                                id = Integer.parseInt(name);
                            } catch (NumberFormatException e) {
                                num = false;
                            }
                            if (num) {
                                boolean exists = false;
                                for (int i = 0; i < clients.size(); i++) {
                                    if (clients.get(i).getID() == id) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (exists) {
                                    disconnect(id, true);
                                } else {
                                    System.out.println("Clients " + id + "Does not exist");
                                }
                            } else {
                                for (int i = 0; i < clients.size(); i++) {
                                    ServerClient c = clients.get(i);
                                    if (name.equals(c.userName)) {
                                        disconnect(c.getID(), true);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            };
            serverActs.start();
        }

        private void sendToAll(Message pinger) {
            Thread sendPings = new Thread("SendPings") {
                @Override
                public void run() {
                    while (true) {
                        for (int i = 0; i < maxClientsCount; i++) {

                            if (threads[i] != null) {
                                //    threads[i].os.println(pinger);
                                System.out.println(pinger);
                                try {
                                    Thread.sleep(2000);
                                    for (int j = 0; j < clients.size(); j++) {
                                        ServerClient c = clients.get(j);
                                        if (!clientResponse.contains(c.getID())) {
                                            if (c.attempt >= MAX_ATTEMPTS) {
                                                disconnect(c.getID(), false);
                                            } else {
                                                c.attempt++;
                                            }
                                        } else {
                                            clientResponse.remove(new Integer(c.getID()));
                                            c.attempt = 0;
                                        }
                                    }
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
            };
            sendPings.start();
        }

        private Message connected(Message data) throws IOException {
            Message addUser = new Message();
            String thedata = data.getMsg();
            int id = UniqueIdentifier.getIdentifier();
            String uname = thedata.split("/c/|/e/")[1];

            ID = id;
            String action = "log in";
            boolean connect = true;

            clients.add(new ServerClient(uname, clientSocket.getInetAddress(),
                    clientSocket.getPort(), id, action));
            ServerWindow.getUsers(uname, clientSocket.getInetAddress(),
                    clientSocket.getPort(), id, action, connect);
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] == this) {
                        clientName = "@" + uname;
                        addUser.setMsg("/c/" + id);
                        addUser.setType(MessageType.CONNECTED);
                        addUser.setUsername("SERVER");
                        
                        if (!names.containsKey(data.getUsername())) {
                            this.name = data.getUsername();
                            user = new User();
                            user.setName(data.getUsername());
                            user.setStatus(Status.ONLINE);

                            users.add(user);
                            names.put(name, user);
                        } else {}
                        
                        output.writeObject(addUser);
                        output.flush();
                        break;
                    }
                }
            }
            return addUser;
        }
        
        private Message sendNotification(Message firstMessage) throws IOException {
            Message msg = new Message();
            msg.setMsg("has joined the chat.");
            msg.setType(MessageType.NOTIFICATION);
            msg.setUsername(firstMessage.getUsername());
            // msg.setPicture(firstMessage.getPicture());
            write(msg);
            return msg;
        }

        private void write(Message incoming) throws IOException {
            for (ObjectOutputStream writer : writers) {
                incoming.setUserlist(names);
                incoming.setUsers(users);
                incoming.setOnlineCount(names.size());
                writer.writeObject(incoming);
                writer.reset();
            }
        }

        private void changeStatus(Message incoming) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private void login(Message incoming) throws IOException {
            String thedata = incoming.getMsg();
            if (thedata.startsWith("/u/")) {
                synchronized (this) {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i] == this) {
                            loginUser = thedata.substring(3, thedata.length());
                            break;
                        }
                    }
                }
            } else if (thedata.startsWith("/p/")) {
                loginPass = thedata.substring(3, thedata.length());
                String valid = ServerDbManager.verifyUser(loginUser, loginPass);

                Message reply = new Message();
                if (valid.startsWith("/y/")) {
                    reply.setMsg(valid);
                    reply.setType(LOGIN);
                    output.writeObject(reply);
                    output.flush();
                } else {
                    reply.setMsg(valid);
                    reply.setType(LOGIN);
                    output.writeObject(reply);
                    output.flush();
                }
            }
        }//end login method

        private void register(Message incoming) throws IOException {
            String thedata = incoming.getMsg();
            Message reply = new Message();
            if (thedata.startsWith("/fn/")) {
                fName = thedata.substring(4, thedata.length());
            } else if (thedata.startsWith("/ln/")) {
                lName = thedata.substring(4, thedata.length());
            } else if (thedata.startsWith("/un/")) {
                uName = thedata.substring(4, thedata.length());
            } else if (thedata.startsWith("/ps/")) {
                pWord = thedata.substring(4, thedata.length());
            } else if (thedata.startsWith("/em/")) {
                email = thedata.substring(4, thedata.length());
            } else if (thedata.startsWith("/ph/")) {
                phone = thedata.substring(4, thedata.length());
            } else if (thedata.startsWith("/xx/")) {
                gender = thedata.substring(4, thedata.length());
            } else if (thedata.startsWith("/co/")) {
                county = thedata.substring(4, thedata.length());
            } else if (thedata.startsWith("/oc/")) {
                occupation = thedata.substring(4, thedata.length());
            } else if (thedata.startsWith("/dob/")) {
                birthDate = thedata.substring(5, thedata.length());
            } else if (thedata.startsWith("/rn/")) {
                status = thedata.substring(4, thedata.length());
                boolean saved = saveUser();
                if (saved) {
                    reply.setMsg("/z/saved");
                    reply.setType(REGISTER);
                    output.writeObject(reply);
                    output.flush();
                } else {
                    reply.setMsg("/x/error");
                    reply.setType(REGISTER);
                    output.writeObject(reply);
                    output.flush();
                }
            }
        }//end register method

        private void send(Message incoming) throws IOException {
            int maxClientsCount = this.maxClientsCount;
            clientThread[] threads = this.threads;

            String line = incoming.getMsg();
            if (line.startsWith("@")) {
                String[] words = line.split("\\s", 2);
                if (words.length > 1 && words[1] != null) {
                    words[1] = words[1].trim();
                    if (!words[1].isEmpty()) {
                        synchronized (this) {
                            for (int i = 0; i < maxClientsCount; i++) {
                                if (threads[i] != null && threads[i] != this
                                        && threads[i].clientName != null
                                        && threads[i].clientName.equals(words[0])) {
                                    Message reply = new Message();
                                    reply.setMsg("/m/" + words[1]);
                                    reply.setType(PRIVATEMESSAGE);
                                    reply.setUsername(incoming.getUsername());
                                    threads[i].output.writeObject(reply);
                                    output.flush();
                                    
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }//end send method

        private void grpSend(Message incoming) throws IOException {
            int maxClientsCount = this.maxClientsCount;
            clientThread[] threads = this.threads;

            String thedata = incoming.getMsg();
            Message grup = new Message();
            if (thedata.startsWith("/g1/")) {
                grup.setMsg("/g/" + "Welcome to group chat: " + clientName);
                grup.setType(GROUPMESSAGE);
                grup.setUsername("SERVER");
                output.writeObject(grup);
                synchronized (this) {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i] != this) {
                            Message toMembers = new Message();
                            toMembers.setUsername("SERVER");
                            toMembers.setMsg("/g/" + "*** A new user " + clientName
                                    + " entered the chat room !!! ***");
                            toMembers.setType(GROUPMESSAGE);
                            threads[i].output.writeObject(toMembers);
                            threads[i].output.flush();
                        }
                    }
                }
            } else if (thedata.startsWith("/g/")) {
                synchronized (this) {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
                            Message toAll = new Message();
                            toAll.setMsg(incoming.getMsg());
                            toAll.setUsername(incoming.getUsername());
                            toAll.setType(GROUPMESSAGE);
                            threads[i].output.writeObject(toAll);
                            threads[i].output.flush();
                        }
                    }
                }
            }
        }//end method grpSend

    }// end inner class

}
