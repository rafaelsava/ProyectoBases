/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controlador;

import java.awt.Checkbox;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;

/**
 * FXML Controller class
 *
 * @author rafae
 */
public class QueriesController implements Initializable {

    @FXML
    private TextField txtDB;
    @FXML
    private ComboBox<String> cbxKey1;
    @FXML
    private ComboBox<String> cbxKey2;
    @FXML
    private FlowPane paneFields1;
    @FXML
    private FlowPane paneFields2;
    @FXML
    private ComboBox<String> cbxCondition1;
    @FXML
    private ComboBox<String> cbxOp1;
    @FXML
    private ComboBox<String> cbxLike1;
    @FXML
    private TextField txtCondition1;
    @FXML
    private ComboBox<String> cbxCondition2;
    @FXML
    private ComboBox<String> cbxOp2;
    @FXML
    private ComboBox<String> cbxLike2;
    @FXML
    private TextField txtCondition2;
    @FXML
    private TextArea txtQueryPreview;
    @FXML
    private Button btnSearch;
    @FXML
    private RadioButton rbt1Table;
    @FXML
    private RadioButton rbt2Table;
    @FXML
    private ComboBox<String> cbxTable2;
    @FXML
    private ComboBox<String> cbxTable1;
    
    private Connection connection;
    
    private String dataBaseName;
    
    private String previousTable2 = null;
    private String previousTable1 = null;
    
    private String select = "select * ";
    private String from ="from ";
    private String join ="inner join ";
    private String on ="on ";
    private String condition1="";
    private String condition2="";
    private String finalQuery = this.select + this.from + this.condition1 + this.condition2;

 

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        ToggleGroup tg = new ToggleGroup();
        this.rbt1Table.setToggleGroup(tg);
        this.rbt2Table.setToggleGroup(tg);
        
        String[] operadores = {"<", ">", "<=", ">=","=", "<>", "LIKE", "NOT LIKE", "IS NULL", "IS NOT NULL"};
        
        for (String operador : operadores){
            this.cbxOp1.getItems().add(operador);
            this.cbxOp2.getItems().add(operador);
        }
        
        this.cbxLike1.setDisable(true);
        this.cbxLike2.setDisable(true);
        this.txtQueryPreview.setText(this.finalQuery);


    }    
   
    @FXML
    private void doKey1(ActionEvent event) {
        this.doKey2(event);
    }

    @FXML
    private void doKey2(ActionEvent event) {
        
        this.on = "on ";
        finalQuery = this.select + this.from + this.join + this.on + this.condition1 + this.condition2;        
        this.txtQueryPreview.setText(this.finalQuery);  
        
        if(this.cbxKey1.getValue()!= null){
            this.on += this.cbxTable1.getValue() + "." + this.cbxKey1.getValue() + " = " + this.cbxTable2.getValue() + "." + this.cbxKey2.getValue()+ " ";
            finalQuery = this.select + this.from + this.join + this.on + this.condition1 + this.condition2;        
            this.txtQueryPreview.setText(this.finalQuery);             
        }
    }

    @FXML
    private void doCondition1(ActionEvent event) {
        this.createCondition(1);
        
    }

    @FXML
    private void doOp1(ActionEvent event) {
        this.cbxLike1.getItems().clear();
        if(this.cbxOp1.getValue()=="LIKE" || this.cbxOp1.getValue()=="NOT LIKE"){
            this.cbxLike1.setDisable(false);
            this.cbxLike1.getItems().add("Comienza en");
            this.cbxLike1.getItems().add("Contiene");
            this.cbxLike1.getItems().add("Finaliza en");                      
        }
        else{
            this.cbxLike1.setDisable(true);
        }        
        

    }

    @FXML
    private void doCondition2(ActionEvent event) {
        this.createCondition(2);
    }

    @FXML
    private void doOp2(ActionEvent event) {
        this.cbxLike1.getItems().clear();
        if(this.cbxOp2.getValue()=="LIKE" || this.cbxOp2.getValue()=="NOT LIKE"){
            this.cbxLike2.setDisable(false);
            this.cbxLike2.getItems().add("Comienza en");
            this.cbxLike2.getItems().add("Contiene");
            this.cbxLike2.getItems().add("Finaliza en");                      
        }   
        else{
            this.cbxLike2.setDisable(true);
        }
    }

    @FXML
    private void doLike2(ActionEvent event) {
        this.createCondition(2);
    }

    @FXML
    private void doSearch(ActionEvent event) {
    }

    @FXML
    private void doTable2(ActionEvent event) {
        ObservableList<String> fields = FXCollections.observableArrayList();
        ObservableList<String> fieldsCondition = FXCollections.observableArrayList();
        if (this.previousTable2 != null) {
            ObservableList<String> filter = this.cbxCondition1.getItems();
            filter.removeIf(item -> item.startsWith(previousTable2 + "."));
            this.cbxCondition2.setItems(filter);
        } 

        String query = "DESCRIBE " + this.dataBaseName + "." + this.cbxTable2.getValue();
        this.join = "inner join ";
        this.join += this.dataBaseName + "." +this.cbxTable2.getValue()+" ";
        finalQuery = this.select + this.from + this.join + this.condition1 + this.condition2;        
        this.txtQueryPreview.setText(this.finalQuery);        
        this.previousTable2 = this.cbxTable2.getValue();
        paneFields2.getChildren().clear();
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                String fieldName = resultSet.getString(1);  // Obtener el nombre del campo
                fields.add(fieldName);
                fieldsCondition.add(this.cbxTable2.getValue()+"."+fieldName);                

                // Crear un CheckBox para cada campo y agregarlo al FlowPane paneFields2
                CheckBox checkBox = new CheckBox(fieldName);
                checkBox.setOnAction(cBEvent -> updateSelectQuery());
                paneFields2.getChildren().add(checkBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.cbxKey2.setItems(fields);
        this.cbxCondition1.getItems().addAll(fieldsCondition);
        this.cbxCondition2.getItems().addAll(fieldsCondition);         
       
    }
    @FXML
    private void doTable1(ActionEvent event) {
        ObservableList<String> fields = FXCollections.observableArrayList();
        ObservableList<String> fieldsCondition = FXCollections.observableArrayList();
        if (this.previousTable1 != null) {
            ObservableList<String> filter = this.cbxCondition1.getItems();
            filter.removeIf(item -> item.startsWith(previousTable1 + "."));
            this.cbxCondition2.setItems(filter);
        } 

        String query = "DESCRIBE " + this.dataBaseName + "." + this.cbxTable1.getValue();
        this.from ="from ";
        this.from += this.dataBaseName + "." + this.cbxTable1.getValue()+" ";
        finalQuery = this.select + this.from + this.condition1 + this.condition2;        
        this.txtQueryPreview.setText(this.finalQuery);
        this.previousTable1 = this.cbxTable1.getValue();
        paneFields1.getChildren().clear();
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                String fieldName = resultSet.getString(1);  // Obtener el nombre del campo
                fields.add(fieldName);
                fieldsCondition.add(this.cbxTable1.getValue()+"."+fieldName);                

                CheckBox checkBox = new CheckBox(fieldName);
                checkBox.setOnAction(cBEvent -> updateSelectQuery());
                paneFields1.getChildren().add(checkBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(fieldsCondition);
        this.cbxKey1.setItems(fields);
        this.cbxCondition1.getItems().addAll(fieldsCondition);
        this.cbxCondition2.getItems().addAll(fieldsCondition);        

    }
    
    
    public void setDBName(String DBName){
        this.dataBaseName = DBName;
        this.txtDB.setText(this.dataBaseName);
    }
    
    public void setConnection (Connection connection){
        this.connection = connection;
    }
    
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
    
    @FXML
    private void setFields1(ActionEvent event) {
        ObservableList<String> tables = getTables(this.dataBaseName,this.connection);
        this.cbxTable1.setItems(tables);
        this.cbxKey2.setDisable(true);
        this.cbxKey1.setDisable(true);
        this.cbxTable2.setDisable(true);     
    }

    @FXML
    private void setFields2(ActionEvent event) {
        ObservableList<String> tables = getTables(this.dataBaseName,this.connection);
        finalQuery = this.select + this.from + this.join + this.condition1 + this.condition2;        
        this.txtQueryPreview.setText(this.finalQuery);        
        this.cbxTable1.setItems(tables);
        this.cbxTable2.setItems(tables);
        this.cbxKey2.setDisable(false);
        this.cbxTable2.setDisable(false);   
        this.cbxKey1.setDisable(false);


    }

    private void updateSelectQuery() {
        StringBuilder selectBuilder = new StringBuilder("SELECT ");

        // Para la primera tabla (paneFields1)
        for (javafx.scene.Node node : paneFields1.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) node;
                if (checkBox.isSelected()) {
                    selectBuilder.append(cbxTable1.getValue()).append(".").append(checkBox.getText()).append(", ");
                }
            }
        }

    // Para la segunda tabla (paneFields2)
    for (javafx.scene.Node node : paneFields2.getChildren()) {
        if (node instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) node;
            if (checkBox.isSelected()) {
                selectBuilder.append(cbxTable2.getValue()).append(".").append(checkBox.getText()).append(", ");
            }
        }
    }

    // Eliminar la última coma y espacio si hay alguna columna seleccionada
    if (selectBuilder.length() > 7) {
        selectBuilder.setLength(selectBuilder.length() - 2);
    } else {
        selectBuilder.append("*");
    }
    
    this.select = selectBuilder.toString();

    // Establecer el texto actualizado en txtQueryPreview
    if(this.rbt2Table.isSelected()){
        finalQuery = this.select + " " + this.from + this.join + this.on+this.condition1 + this.condition2;
    }
    else{
        finalQuery = this.select + " " + this.from + this.condition1 + this.condition2;                    
    }   
    this.txtQueryPreview.setText(this.finalQuery);
}

    @FXML
    private void doLike1(ActionEvent event) {
        this.createCondition(1);

    }


    @FXML
    private void doCreateCondition1(KeyEvent event) {
        this.createCondition(1);

    }
    
    private void createCondition(Integer option){
        if (option ==1){
            if(this.cbxCondition1.getValue() != null && this.cbxOp1.getValue()!=null){
                this.condition1 = "where ";
                if(!"LIKE".equals(this.cbxOp1.getValue()) && !"NOT LIKE".equals(this.cbxOp1.getValue())){
                    this.condition1 += this.cbxCondition1.getValue() + " " + this.cbxOp1.getValue()+ " "+this.txtCondition1.getText()+" ";               
                } 
                else {
                    if(this.cbxLike1 != null){
                        switch (this.cbxLike1.getValue()){
                            case "Comienza en" -> this.condition1 += this.cbxCondition1.getValue() + " "+ this.cbxOp1.getValue() + " '"+this.txtCondition1.getText() + "%' ";
                            case "Finaliza en" -> this.condition1 += this.cbxCondition1.getValue() + " "+ this.cbxOp1.getValue() + " '%"+ this.txtCondition1.getText() +"' ";
                            case "Contiene" -> this.condition1 += this.cbxCondition1.getValue() + " "+ this.cbxOp1.getValue() + " '%"+ this.txtCondition1.getText()+"%' " ;

                        }

                    }
                }
                if(this.rbt2Table.isSelected()){
                    finalQuery = this.select + " " + this.from + this.join + this.on+this.condition1 + this.condition2;
                }
                else{
                    finalQuery = this.select + " " + this.from + this.condition1 + this.condition2;                    
                }
                this.txtQueryPreview.setText(finalQuery);

            }
            
        }
        
        if(option==2){
            if(this.cbxCondition2.getValue() != null && this.cbxOp2.getValue()!=null){
                this.condition2 = "and ";
                if(!"LIKE".equals(this.cbxOp2.getValue()) && !"NOT LIKE".equals(this.cbxOp2.getValue())){
                    this.condition2 += this.cbxCondition2.getValue() + " " + this.cbxOp2.getValue()+ " "+this.txtCondition2.getText()+" ";               
                } 
                else {
                    if(this.cbxLike2 != null){
                        switch (this.cbxLike2.getValue()){
                            case "Comienza en" -> this.condition2 += this.cbxCondition2.getValue() + " "+ this.cbxOp2.getValue() + " '"+this.txtCondition2.getText() + "%' ";
                            case "Finaliza en" -> this.condition2 += this.cbxCondition2.getValue() + " "+ this.cbxOp2.getValue() + " '%"+ this.txtCondition2.getText() +"' ";
                            case "Contiene" -> this.condition2 += this.cbxCondition2.getValue() + " "+ this.cbxOp2.getValue() + " '%"+ this.txtCondition2.getText()+"%' " ;

                        }

                    }
                }
                if(this.rbt2Table.isSelected()){
                    finalQuery = this.select + " " + this.from + this.join + this.on+this.condition1 + this.condition2;
                }
                else{
                    finalQuery = this.select + " " + this.from + this.condition1 + this.condition2;                    
                }              
                this.txtQueryPreview.setText(finalQuery);

            }            
        }
        
    }

    @FXML
    private void doCreateCondition2(KeyEvent event) {
        this.createCondition(2);
    }
    
}