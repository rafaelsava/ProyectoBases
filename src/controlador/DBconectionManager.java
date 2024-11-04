/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

/**
 *
 * @author Lenovo
 */
public class DBconectionManager {
    Connection conectar = null;
    public Connection connection(String user, String password, String server, String port){
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
