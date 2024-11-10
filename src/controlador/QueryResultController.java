/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controlador;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;


/**
 * FXML Controller class
 *
 * @author rafae
 */
public class QueryResultController implements Initializable {

    @FXML
    private TableView<ObservableList<String>> tblQueryResult;
    
    Connection connection;
    
    String query;
    @FXML
    private Button btnBack;
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
    
    public void setConnection(Connection connection) {
        this.connection = connection;
        this.loadTableData();
        
    }
    
    public void setQuery(String query){
        this.query = query;
    }
    
    public void loadTableData() {
        this.tblQueryResult.getItems().clear();
        this.tblQueryResult.getColumns().clear(); 
        
        
         try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(this.query)) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            for (int i = 1; i <= columnCount; i++) {
                final int columnIndex = i;
                String columnName = metaData.getColumnName(i);

                // Crear columna en el TableView
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnName);
                column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(columnIndex - 1)));

                this.tblQueryResult.getColumns().add(column);
            }
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getString(i));
                }
                data.add(row);
            }

            this.tblQueryResult.setItems(data);
         }catch (SQLException e) {
            e.printStackTrace();
        }
      }

    @FXML
    private void doBack(ActionEvent event) {
            try{
            FXMLLoader loader= new FXMLLoader(getClass().getResource("/vista/Queries.fxml"));
            Parent root=loader.load();
            Scene scene=new Scene(root);
            
                                                                   
            Stage stage=new Stage();
            stage.setScene(scene);
            //stage.setOnCloseRequest(even->{even.consume();});
            stage.setResizable(false);
            stage.setTitle("Queries");
        
            stage.show();
            
            Stage myStage=(Stage)this.btnBack.getScene().getWindow();
            myStage.close();
            
        }
        catch(IOException ex){
            java.util.logging.Logger.getLogger(QueriesController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }        
        
    
    }
    
}
