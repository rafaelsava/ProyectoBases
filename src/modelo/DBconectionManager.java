/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

/**
 *
 * @author Lenovo
 */
public class DBconectionManager {
    Connection conectar = null;
    String user;
    String password;
    String server;
    String port;

    public DBconectionManager(String user, String password, String server, String port) {
        this.user = user;
        this.password = password;
        this.server = server;
        this.port = port;
    }
   
    
    public Connection connection(){
        String url = "jdbc:mysql://"+server+":"+port;
        try{
            conectar = DriverManager.getConnection(url,user,password);
            JOptionPane.showMessageDialog(null, "La conexion se ha realizado con exito");
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(null, "Error de autentificación de usuario..."+e.toString());
        }
        return conectar;
    }
    
}
