/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controlador;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import javafx.fxml.FXMLLoader;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author rafae
 */
public class LoginController implements Initializable {

    @FXML
    private TextField txtIP;
    @FXML
    private TextField txtPort;
    @FXML
    private TextField txtUser;
    @FXML
    private Button btnConnect;
    @FXML
    private TextField txtPassword;
    Connection conectar = null;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void doConnect(ActionEvent event) {  
        String user = this.txtUser.getText();
        String password = this.txtPassword.getText();
        String server = this.txtIP.getText();
        String port = this.txtPort.getText();
       
        try{
            this.connection(user, password, server, port);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/EstructuraTabla.fxml"));
            Parent root = loader.load();
            EstructuraTablaController tabla = loader.getController(); 
            tabla.setConnection(conectar);
            tabla.fillcombo(conectar);
            Scene scene=new Scene(root);
                        
            Stage stage=new Stage();
            stage.setScene(scene);
            //stage.setOnCloseRequest(even->{even.consume();});
            stage.setResizable(false);
            stage.setTitle("Estructura de una Tabla");
            
            Stage myStage=(Stage)this.btnConnect.getScene().getWindow();
            myStage.close();            
        
            stage.show();
            
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(null, "Error de carga... "+e.toString());
        }
        
        
    }
    public void changeStage (){
        try{
            FXMLLoader loader= new FXMLLoader(getClass().getResource("vista/EstructuraTabla.fxml"));
            Parent root=loader.load();
            Scene scene=new Scene(root);
                        
            Stage stage=new Stage();
            stage.setScene(scene);
            //stage.setOnCloseRequest(even->{even.consume();});
            stage.setResizable(false);
            stage.setTitle("Estructura de una Tabla");
            
            Stage myStage=(Stage)this.btnConnect.getScene().getWindow();
            myStage.close();            
        
            stage.show();
            
            
        }
        catch(IOException ex){
            java.util.logging.Logger.getLogger(EstructuraTablaController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }  
    }
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
