/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controlador;

import java.sql.Connection;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn; //Aqui pueede haber un error
import javafx.scene.control.cell.PropertyValueFactory;
import javax.swing.JOptionPane;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.sql.ResultSetMetaData;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author rafae
 */
public class EstructuraTablaController implements Initializable {

    @FXML
    private ComboBox<String> cbxDB;
    @FXML
    private ComboBox<String> cbxTable;
    @FXML
    private TableView<ObservableList<String>> tblStructure;
    @FXML
    private Button btnAddRegisters;
    @FXML
    private Button btnSearch;
    private Connection connection;
    @FXML
    private Button btnQuery;
    /**
     * Initializes the controller class.
     */
    public void setConnection (Connection connection){
        this.connection = connection;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void doDB(ActionEvent event) {
        String selectedDatabase = this.cbxDB.getValue();
        if(selectedDatabase != null && !selectedDatabase.isEmpty()){
            ObservableList<String> tables = getTables(selectedDatabase, connection);
            this.cbxTable.setItems(tables);
        }
    }

    @FXML
    private void doTable(ActionEvent event) {
    }

    @FXML
    private void doAddRegisters(ActionEvent event) {
    }

    @FXML
    private void doSearch(ActionEvent event) {
        String selectedDatabase = this.cbxDB.getValue();
        String selectedTable = this.cbxTable.getValue();
        if (selectedDatabase != null && selectedTable != null) {
            loadTableData(selectedDatabase, selectedTable, connection);
        }
    }
    
    //Gestion temporal de llenado de los combo box y la tabla.
    
    
    public void fillcombo(Connection connection){
        ArrayList<String> dbList = new ArrayList<String>();
        ObservableList<String> databases = FXCollections.observableArrayList();
        try(Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("SHOW DATABASES")){
             while (resultSet.next()) {
                databases.add(resultSet.getString(1));
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        this.cbxDB.setItems(databases);
    }
    
    //Llenar el combo box de las tablas correspondientes a la base de datos.
    
    public ObservableList<String> getTables(String dataBaseName, Connection connection){
        ObservableList<String> tables = FXCollections.observableArrayList();
        String query = "SHOW TABLES FROM " + dataBaseName;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
            tables.add(resultSet.getString(1));  // Obtener el nombre de la tabla
        }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }
    
    //Hacer columnas dinamicas y consulta sql para llenar el table view de la interfaz
    
      public void loadTableData(String databaseName, String tableName, Connection connection) {
        this.tblStructure.getItems().clear();
        this.tblStructure.getColumns().clear(); 
        
        String query = "DESCRIBE " + databaseName + "." + tableName;
        
         try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            for (int i = 1; i <= columnCount; i++) {
                final int columnIndex = i;
                String columnName = metaData.getColumnName(i);

                // Crear columna en el TableView
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnName);
                column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(columnIndex - 1)));

                tblStructure.getColumns().add(column);
            }
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getString(i));
                }
                data.add(row);
            }

            tblStructure.setItems(data);
         }catch (SQLException e) {
            e.printStackTrace();
        }
      }

    @FXML
    private void doQuery(ActionEvent event) {
    }

}
