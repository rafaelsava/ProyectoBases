/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controlador;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void doConnect(ActionEvent event) {
        System.out.println("Conectando");
        System.out.println(txtIP.getText());
        System.out.println(txtPort.getText());
        System.out.println(txtUser.getText());
        System.out.println(txtPassword.getText());
        
        
    }
    
}
