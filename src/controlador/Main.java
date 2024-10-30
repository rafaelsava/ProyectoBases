/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application{

    public static void main(String [] args)
    {
        launch(args);
    }
    
    @Override
    public void start(Stage ventana) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/Login.fxml")); // Arma la ventana
        Scene scene = new Scene(root);
        ventana.setScene(scene);
        ventana.setTitle("VENTANA PRINCIPAL"); // titulo ventana
        ventana.setResizable(false); // no permite dimensionar ventana
        //ventana.setOnCloseRequest(event -> {event.consume();});
        ventana.show();
    }
    
}
 