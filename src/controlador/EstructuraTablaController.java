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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author rafae
 */
public class EstructuraTablaController implements Initializable {

    @FXML
    private ComboBox<?> cbxDB;
    @FXML
    private ComboBox<?> cbxTable;
    @FXML
    private TableView<?> tblStructure;
    @FXML
    private Button btnAddRegisters;
    @FXML
    private Button btnSearch;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void doDB(ActionEvent event) {
    }

    @FXML
    private void doTable(ActionEvent event) {
    }

    @FXML
    private void doAddRegisters(ActionEvent event) {
    }

    @FXML
    private void doSearch(ActionEvent event) {
    }
    
}
