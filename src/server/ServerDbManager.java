package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerDbManager {

    private static Connection con;
    private static Statement st;
    private static ResultSet rs;
    private static PreparedStatement ps;

    public static boolean query(String query) {
        con = null;
        st = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cocochut", "root", "");
            st = con.createStatement();
            st.executeUpdate(query);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    public static String verifyUser(String username, String password) {
        String query = "select * from users where uName = '" + username + "' and pass = '" + password + "'";
        String query1 = "select * from users where uName = '" + username + "'";

        con = null;
        st = null;
        rs = null;
        

        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cocochut", "root", "");
            System.out.println(username + password);
            st = con.createStatement();
            rs = st.executeQuery(query1);
            if (rs.next()) {
                rs = st.executeQuery(query);
                if (rs.next()) {
                    return ("/y/" + "UserName and Password correct");
                } else {
                    return ("/x/" + "Password incorrect");
                }
            } else {
                return ("/z/" + "Invalid user");
            }
        } catch (SQLException e) {
            return ("Login failed");
        }
    }

    public static boolean registerUser(String fName, String lName, String uName, String pWord, String email, String phone, String gender,
            String county, String occupation, String birthDate, String status) {

        String addUser = "insert into users(fName,lName,uName,pass,email,phone,gender,county,occupation,dob,relationship)"
                + "VALUES('" + fName + "','" + lName + "','" + uName + "','" + pWord + "','" + email + "','" + phone + "','" + gender + "',"
                + "'" + county + "','" + occupation + "','" + birthDate + "','" + status + "')";
        

        boolean registered = query(addUser);
        if (registered) {
            return true;
        }
        return false;
        
    }

    public static void main(String[] args) {
        ServerDbManager db = new ServerDbManager();
        String user = "tina";
        boolean choice = false;
        //db.updatePic(user, choice);
       //db.retrieveUsers();
    }

    public static void updatePic(String username, boolean choice) {
        con = null;
        ps = null;
        String updateQuery = "UPDATE users SET userImg = ? WHERE uName = ?";
        String deleteQuery = "UPDATE users SET userImg = ? WHERE uName = ?";
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cocochut", "root", "");
            if (choice == true) {
                ps = con.prepareStatement(updateQuery);
                InputStream img = new FileInputStream(new File("H:\\my photos\\leni 4tos\\mtaani\\chai.jpg"));
                ps.setBlob(1, img);
                ps.setString(2, username);
                if (ps.executeUpdate() == 1) {
                    System.out.println("image saved");
                } else {
                    System.out.println("no image saved");
                }
            } else if (choice == false) {
                ps = con.prepareStatement(deleteQuery);
                InputStream img = new FileInputStream(new File("src\\images\\default.png"));
                ps.setBlob(1, img);
                ps.setString(2, username);
                if (ps.executeUpdate() == 1) {
                    System.out.println("image deleted");
                } else {
                    System.out.println("image non-existent");
                }
            }
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(ServerDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void retrieveUsers() {
        con = null;
        st = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cocochut", "root", "");
            st = con.createStatement();
            rs = st.executeQuery("select * from users;");
            System.out.println("=====UserNames--------");
            while (rs.next()) {
                System.out.println(rs.getString("uName"));
            }
            System.out.println("=======UserNames--------");
        } catch (SQLException ex) {
            Logger.getLogger(ServerDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void retrieveImage(){
        try {
            InputStream is = rs.getBinaryStream("userImg");
            // OutputStream os = new FileOutputStream();
        } catch (SQLException ex) {
            Logger.getLogger(ServerDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
