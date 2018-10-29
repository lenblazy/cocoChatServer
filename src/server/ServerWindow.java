package server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class ServerWindow extends Application {

    Stage window;
    Scene server;
    ToggleButton start, stop;
    ToggleGroup tg;
    public static TableView<ServerClient> monitor, onClients;
    Text status;
    ServerThread stred;
    public static List<ServerClient> client = new ArrayList<>();
    public static ObservableList<ServerClient> userList;
    private static ListView onlineUsers;
    

    @Override
    public void start(Stage primaryWindow) throws Exception {
        window = primaryWindow;
        window.setTitle("Cocochut- SERVER 1.0");
        File iconImg = new File("src/images/icon.jpg");
        String iconLoc = iconImg.toURI().toURL().toString();
        window.getIcons().add(new Image(iconLoc));
        BorderPane root = new BorderPane();

        //username column
        TableColumn<ServerClient, String> nameColumn = new TableColumn<>("Username");
        nameColumn.setMinWidth(60);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));

        //port column
        TableColumn<ServerClient, String> portColumn = new TableColumn<>("Port");
        portColumn.setMinWidth(20);
        portColumn.setCellValueFactory(new PropertyValueFactory<>("port"));

        //Identity column
        TableColumn<ServerClient, String> idColumn = new TableColumn<>("User ID");
        idColumn.setMinWidth(30);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));

        //ipAddress column
        TableColumn<ServerClient, String> ipColumn = new TableColumn<>("Host Address");
        ipColumn.setMinWidth(100);
        ipColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        //Action column
        TableColumn<ServerClient, String> actionColumn = new TableColumn<>("Action");
        actionColumn.setMinWidth(90);
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));        

        userList = FXCollections.observableArrayList();
        monitor = new TableView<>();
        monitor.setItems(userList);
        monitor.getColumns().addAll(nameColumn, portColumn, idColumn, ipColumn, actionColumn);
        monitor.setMinWidth(340);

        TitledPane onLie = new TitledPane();
        onLie.setText("Online Clients");
        onLie.setId("child-style");
        
        //username column
        TableColumn<ServerClient, String> nameColumn1 = new TableColumn<>("Username");
        nameColumn.setMinWidth(60);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        
        onClients = new TableView<>();
        onClients.setItems(userList);
        onClients.getColumns().add(nameColumn1);
        onClients.setMaxWidth(60);

        TitledPane monTa = new TitledPane();
        monTa.setText("Monitor Clients");
        monTa.setContent(monitor);
        monTa.setId("child-style");

        onLie.setContent(onClients);

        HBox up = new HBox(10);
        up.getChildren().addAll(monTa, onLie);
        up.setPadding(new Insets(3));

        TitledPane steto = new TitledPane();
        steto.setText("Server Status");
        status = new Text("Server Offline...");
        status.setFont(Font.font("Serif", FontWeight.BOLD, 30));
        status.setFill(Color.RED);
        StackPane st = new StackPane(new Group(status));
        steto.setContent(st);
        steto.setId("child-style");

        tg = new ToggleGroup();
        start = new ToggleButton("Start");
        start.setToggleGroup(tg);
        
        stop = new ToggleButton("Stop");
        stop.setToggleGroup(tg);
        stop.setSelected(true);
        stop.setDisable(true);
        
        stop.setOnAction(e -> {
            //TO DO CODE TO STOP LISTENING FOR CONNECTIONS
            stred.stopServer();
            status.setText("Server Offline...");
            status.setFill(Color.RED);
            stop.setDisable(true);
            start.setDisable(false);
        });

        start.setOnAction(e -> {
            try {
                stred = new ServerThread();
            } catch (IOException ex) {
                Logger.getLogger(ServerWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
            status.setText("Server Now Online...");
            status.setFill(Color.GREEN);
            start.setDisable(true);
            stop.setDisable(false);
        });

        HBox butts = new HBox(30);
        butts.setAlignment(Pos.CENTER);
        butts.getChildren().addAll(start, stop);

        VBox down = new VBox(3);
        down.getChildren().addAll(steto, butts);

        root.setCenter(up);
        root.setBottom(down);
        root.setPadding(new Insets(5, 5, 5, 1));
        root.setId("root-style");

        server = new Scene(root, 500, 400);
        server.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        window.setOnCloseRequest(e -> {
            window.close();
        });
        window.setScene(server);
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static ObservableList<ServerClient> getUsers(String name, InetAddress inetAddress, int port, int id, String action, boolean connect) {
        if(connect == true){
            userList.add(new ServerClient(name, inetAddress, port, id, action));
            System.out.println(name + " Identifier: (" + id + ") connected!");
            monitor.refresh();
            onClients.refresh();
        }else if (connect == false){
            ServerClient c = null;
            for(int i = 0; i < client.size(); i++){
                if (client.get(i).getID() == id) {
                    c = client.get(i);
                    userList.add(new ServerClient(name, inetAddress, port, id, action));
                    monitor.refresh();
                    onClients.refresh();
                    break;
                }
            }
        }
        return userList;
    }

}
