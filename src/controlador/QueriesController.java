/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controlador;

import java.io.IOException;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import controlador.QueryResultController;

public class QueriesController implements Initializable {

    // Declaración de componentes de la interfaz
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
    @FXML
    private ComboBox<String> cbxCondition2;
    
    // Variables de configuración y conexión
    private Connection connection;
    private String dataBaseName;
    private String previousTable2 = null;
    private String previousTable1 = null;
    private String select = "SELECT * ";
    private String from = "FROM ";
    private String join = "INNER JOIN ";
    private String on = "ON ";
    private String condition1 = "";
    private String condition2 = "";
    private String finalQuery = this.select + this.from + this.condition1 + this.condition2;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuración inicial del controlador
        ToggleGroup tg = new ToggleGroup();
        this.rbt1Table.setToggleGroup(tg);
        this.rbt2Table.setToggleGroup(tg);

        // Agregar operadores de comparación a los ComboBox de condiciones
        String[] operators = {"<", ">", "<=", ">=", "=", "<>", "LIKE", "NOT LIKE", "IS NULL", "IS NOT NULL"};
        for (String op : operators) {
            this.cbxOp1.getItems().add(op);
            this.cbxOp2.getItems().add(op);
        }
        
        // Desactivar los ComboBox de "LIKE" inicialmente
        this.cbxLike1.setDisable(true);
        this.cbxLike2.setDisable(true);
        this.updateFinalQuery();
    }
    
    // Método para actualizar la consulta "ON" en la unión
    @FXML
    private void doKey1(ActionEvent event) {
        this.doKey2(event);
    }

    @FXML
    private void doKey2(ActionEvent event) {
        this.on = "ON ";
        if (this.cbxKey1.getValue() != null) {
            this.on += this.cbxTable1.getValue() + "." + this.cbxKey1.getValue() + " = " + this.cbxTable2.getValue() + "." + this.cbxKey2.getValue();
        }
        this.updateFinalQuery();
    }

    // Métodos para manejar los operadores de las condiciones
    @FXML
    private void doOp1(ActionEvent event) {
        this.configureLikeOptions(this.cbxOp1, this.cbxLike1);
        this.createCondition(1);
    }

    @FXML
    private void doOp2(ActionEvent event) {
        this.configureLikeOptions(this.cbxOp2, this.cbxLike2);
        this.createCondition(2);
    }

    // Configura el ComboBox de opciones "LIKE"
    private void configureLikeOptions(ComboBox<String> opComboBox, ComboBox<String> likeComboBox) {
        likeComboBox.getItems().clear();
        if ("LIKE".equals(opComboBox.getValue()) || "NOT LIKE".equals(opComboBox.getValue())) {
            likeComboBox.setDisable(false);
            likeComboBox.getItems().addAll("Comienza en", "Contiene", "Finaliza en");
        } else {
            likeComboBox.setDisable(true);
        }
    }

    // Métodos para actualizar los campos de las tablas seleccionadas
    @FXML
    private void doTable2(ActionEvent event) {
        this.updateFieldsPane(this.cbxTable2, this.paneFields2, this.previousTable2, this.cbxCondition1, this.cbxCondition2);
        this.previousTable2 = this.cbxTable2.getValue();
    }

    @FXML
    private void doTable1(ActionEvent event) {
        this.updateFieldsPane(this.cbxTable1, this.paneFields1, this.previousTable1, this.cbxCondition1, this.cbxCondition2);
        this.previousTable1 = this.cbxTable1.getValue();
    }

    // Actualiza los campos del panel de una tabla seleccionada
    private void updateFieldsPane(ComboBox<String> tableComboBox, FlowPane fieldsPane, String previousTable, ComboBox<String>... conditionBoxes) {
        ObservableList<String> fields = FXCollections.observableArrayList();
        ObservableList<String> fieldsCondition = FXCollections.observableArrayList();

        // Remover campos de la tabla anterior en los ComboBox de condiciones
        if (previousTable != null) {
            this.removePreviousTableFields(previousTable, conditionBoxes);
        }

        // Consulta para describir la tabla seleccionada
        String query = "DESCRIBE " + this.dataBaseName + "." + tableComboBox.getValue();
        String tablePrefix = this.dataBaseName + "." + tableComboBox.getValue();
        if (tableComboBox == this.cbxTable1) {
            this.from = "FROM " + tablePrefix + " ";
        } else {
            this.join = "INNER JOIN " + tablePrefix + " ";
        }
        
        fieldsPane.getChildren().clear();
        try (Statement statement = this.connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                String fieldName = resultSet.getString(1);
                fields.add(fieldName);
                fieldsCondition.add(tableComboBox.getValue() + "." + fieldName);
                this.addCheckBoxToPane(fieldName, fieldsPane);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.updateComboBoxItems(tableComboBox, fields, fieldsCondition, conditionBoxes);
        this.updateFinalQuery();
    }

    // Elimina los campos de la tabla anterior en los ComboBox de condiciones
    private void removePreviousTableFields(String previousTable, ComboBox<String>[] conditionBoxes) {
        ObservableList<String> filter = conditionBoxes[0].getItems();
        filter.removeIf(item -> item.startsWith(previousTable + "."));
        conditionBoxes[1].setItems(filter);
    }

    // Agrega un CheckBox para cada campo en el FlowPane
    private void addCheckBoxToPane(String fieldName, FlowPane pane) {
        CheckBox checkBox = new CheckBox(fieldName);
        checkBox.setOnAction(event -> this.updateSelectQuery());
        pane.getChildren().add(checkBox);
    }

    // Actualiza los ComboBox de claves y condiciones con los campos disponibles
    private void updateComboBoxItems(ComboBox<String> tableComboBox, ObservableList<String> fields, ObservableList<String> fieldsCondition, ComboBox<String>... conditionBoxes) {
        ComboBox<String> keyComboBox = tableComboBox == this.cbxTable1 ? this.cbxKey1 : this.cbxKey2;
        keyComboBox.setItems(fields);

        for (String field : fieldsCondition) {
            for (ComboBox<String> conditionBox : conditionBoxes) {
                if (!conditionBox.getItems().contains(field)) {
                    conditionBox.getItems().add(field);
                }
            }
        }
    }

    // Crea una condición WHERE o AND según la opción
    private void createCondition(int option) {
        ComboBox<String> conditionBox = option == 1 ? this.cbxCondition1 : this.cbxCondition2;
        ComboBox<String> opBox = option == 1 ? this.cbxOp1 : this.cbxOp2;
        TextField conditionText = option == 1 ? this.txtCondition1 : this.txtCondition2;
        ComboBox<String> likeBox = option == 1 ? this.cbxLike1 : this.cbxLike2;

        if (conditionBox.getValue() != null && opBox.getValue() != null) {
            String condition = (option == 1 ? " WHERE " : " AND ") + this.buildCondition(conditionBox, opBox, conditionText, likeBox);
            if (option == 1) {
                this.condition1 = condition;
            } else {
                this.condition2 = condition;
            }
            this.updateFinalQuery();
        }
    }

    // Construye la condición para la consulta
    private String buildCondition(ComboBox<String> conditionBox, ComboBox<String> opBox, TextField conditionText, ComboBox<String> likeBox) {
        String conditionValue = conditionText.getText();
        if (!"LIKE".equals(opBox.getValue()) && !"NOT LIKE".equals(opBox.getValue())) {
            return conditionBox.getValue() + " " + opBox.getValue() + " " + conditionValue;
        } else if (likeBox.getValue() != null) {
            String likeCondition = "";
            if ("Comienza en".equals(likeBox.getValue())) {
                likeCondition = conditionBox.getValue() + " " + opBox.getValue() + " '" + conditionValue + "%'";
            } else if ("Finaliza en".equals(likeBox.getValue())) {
                likeCondition = conditionBox.getValue() + " " + opBox.getValue() + " '%" + conditionValue + "'";
            } else if ("Contiene".equals(likeBox.getValue())) {
                likeCondition = conditionBox.getValue() + " " + opBox.getValue() + " '%" + conditionValue + "%'";
            }
            return likeCondition;
        }
        return "";
    }

    // Actualiza la consulta de selección en la vista previa
    private void updateSelectQuery() {
        StringBuilder selectBuilder = new StringBuilder("SELECT ");
        this.buildSelectFields(selectBuilder, this.paneFields1, this.cbxTable1);
        this.buildSelectFields(selectBuilder, this.paneFields2, this.cbxTable2);
        
        this.select = selectBuilder.length() > 7 ? selectBuilder.substring(0, selectBuilder.length() - 2) : "SELECT *";
        this.updateFinalQuery();
    }

    // Construye los campos seleccionados para la consulta
    private void buildSelectFields(StringBuilder builder, FlowPane pane, ComboBox<String> tableBox) {
        for (javafx.scene.Node node : pane.getChildren()) {
            if (node instanceof CheckBox && ((CheckBox) node).isSelected()) {
                builder.append(tableBox.getValue()).append(".").append(((CheckBox) node).getText()).append(", ");
            }
        }
    }

    // Actualiza la consulta final y la muestra en el área de vista previa
    private void updateFinalQuery() {
        this.finalQuery = this.select + " " + this.from + (this.rbt2Table.isSelected() ? this.join + this.on : "") + this.condition1 + this.condition2;
        this.txtQueryPreview.setText(this.finalQuery);
    }

    // Configura el nombre de la base de datos
    public void setDBName(String DBName) {
        this.dataBaseName = DBName;
        this.txtDB.setText(this.dataBaseName);
    }

    // Establece la conexión de la base de datos
    public void setConnection(Connection connection) {
        this.connection = connection;
        System.out.println(this.connection);
    }

    // Obtiene la lista de tablas en la base de datos seleccionada
    public ObservableList<String> getTables(String dataBaseName, Connection connection) {
        ObservableList<String> tables = FXCollections.observableArrayList();
        String query = "SHOW TABLES FROM " + dataBaseName;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    // Configura los campos de la primera tabla seleccionada
    @FXML
    private void setFields1(ActionEvent event) {
        ObservableList<String> tables = this.getTables(this.dataBaseName, this.connection);
        this.cbxTable1.setItems(tables);
        this.cbxKey2.setDisable(true);
        this.cbxKey1.setDisable(true);
        this.cbxTable2.setDisable(true);     
    }

    // Configura los campos de la segunda tabla seleccionada
    @FXML
    private void setFields2(ActionEvent event) {
        ObservableList<String> tables = this.getTables(this.dataBaseName, this.connection);
        this.updateFinalQuery();
        this.cbxTable1.setItems(tables);
        this.cbxTable2.setItems(tables);
        this.cbxKey2.setDisable(false);
        this.cbxTable2.setDisable(false);   
        this.cbxKey1.setDisable(false);
    }

    // Métodos para manejar las condiciones y LIKE en la interfaz
    @FXML
    private void doLike1(ActionEvent event) {
        this.createCondition(1);
    }

    @FXML
    private void doLike2(ActionEvent event) {
        this.createCondition(2);
    }

    @FXML
    private void doCreateCondition1(KeyEvent event) {
        this.createCondition(1);
    }

    @FXML
    private void doCreateCondition2(KeyEvent event) {
        this.createCondition(2);
    }

    @FXML
    private void doCondition1(ActionEvent event) {
        this.createCondition(1);
    }

    @FXML
    private void doCondition2(ActionEvent event) {
        this.createCondition(2);
    }

    // Método para realizar la búsqueda
    @FXML
    private void doSearch(ActionEvent event) {
            try{
            FXMLLoader loader= new FXMLLoader(getClass().getResource("/vista/QueryResult.fxml"));
            Parent root=loader.load();
            Scene scene=new Scene(root);
            
            QueryResultController queryResult = loader.getController();
            queryResult.setQuery(this.finalQuery);
            queryResult.setConnection(this.connection);
                                                                   
            Stage stage=new Stage();
            stage.setScene(scene);
            //stage.setOnCloseRequest(even->{even.consume();});
            stage.setResizable(false);
            stage.setTitle("Resultado del Query");
        
            stage.show();
            
            Stage myStage=(Stage)this.btnSearch.getScene().getWindow();
            myStage.close();
            
        }
        catch(IOException ex){
            java.util.logging.Logger.getLogger(QueriesController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }        
        
    }
}
