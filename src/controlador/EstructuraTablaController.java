/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controlador;

import java.io.IOException;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import controlador.QueriesController;
import java.util.HashSet;

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
    @FXML
    private Button btnShowAll;

    /**
     * Initializes the controller class.
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void doDB(ActionEvent event) {
        String selectedDatabase = this.cbxDB.getValue();
        if (selectedDatabase != null && !selectedDatabase.isEmpty()) {
            ObservableList<String> tables = getTables(selectedDatabase, connection);
            this.cbxTable.setItems(tables);
        }
    }

    @FXML
    private void doTable(ActionEvent event) {
    }

    @FXML
    private void doAddRegisters(ActionEvent event) {
        String selectedDatabase = this.cbxDB.getValue();
        String selectedTable = this.cbxTable.getValue();

        if (selectedDatabase == null || selectedTable == null) {
            showMessage("Debe seleccionar una base de datos y una tabla.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtén la estructura de la tabla para saber los tipos de datos.
        ArrayList<String[]> columnsWithTypes = getColumnsWithTypes(selectedDatabase, selectedTable);

        ArrayList<String> values = new ArrayList<>();

        // Solicita al usuario valores para cada columna.
        for (String[] column : columnsWithTypes) {
            String columnName = column[0];
            String columnType = column[1]; // Tipo de dato
            String inputMessage = "Ingrese el valor para '" + columnName + "' (Tipo: " + columnType + "):";

            String value = JOptionPane.showInputDialog(null, inputMessage, "Agregar Registro", JOptionPane.QUESTION_MESSAGE);

            if (value == null) { // Si el usuario cancela.
                showMessage("Operación cancelada.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Si está vacío, se usa NULL.
            values.add(value.trim().isEmpty() ? "NULL" : "'" + value + "'");
        }

        // Construye y ejecuta la consulta SQL INSERT INTO.
        insertIntoTable(selectedDatabase, selectedTable, columnsWithTypes, values);
    }

    private ArrayList<String[]> getColumnsWithTypes(String databaseName, String tableName) {
        ArrayList<String[]> columnsWithTypes = new ArrayList<>();
        String query = "DESCRIBE " + databaseName + "." + tableName;

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("Field");
                String columnType = resultSet.getString("Type");
                columnsWithTypes.add(new String[]{columnName, columnType});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return columnsWithTypes;
    }

    private void insertIntoTable(String databaseName, String tableName, ArrayList<String[]> columnsWithTypes, ArrayList<String> values) {
        // Construye la consulta INSERT INTO.
        StringBuilder query = new StringBuilder("INSERT INTO " + databaseName + "." + tableName + " (");

        for (int i = 0; i < columnsWithTypes.size(); i++) {
            query.append(columnsWithTypes.get(i)[0]);
            if (i < columnsWithTypes.size() - 1) {
                query.append(", ");
            }
        }
        query.append(") VALUES (");
        for (int i = 0; i < values.size(); i++) {
            query.append(values.get(i));
            if (i < values.size() - 1) {
                query.append(", ");
            }
        }
        query.append(")");

        // Ejecuta la consulta.
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query.toString());
            showMessage("Registro agregado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            loadTableData(databaseName, tableName, connection, "searchAll");
        } catch (SQLException e) {
            showMessage("Error al agregar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    private void doSearch(ActionEvent event) {
        String selectedDatabase = this.cbxDB.getValue();
        String selectedTable = this.cbxTable.getValue();
        if (selectedDatabase != null && selectedTable != null) {
            loadTableData(selectedDatabase, selectedTable, connection, "search");
        }
    }

    //Gestion temporal de llenado de los combo box y la tabla.
    public void fillcombo(Connection connection) {
        ArrayList<String> dbList = new ArrayList<String>();
        ObservableList<String> databases = FXCollections.observableArrayList();
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("SHOW DATABASES")) {
            while (resultSet.next()) {
                databases.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.cbxDB.setItems(databases);
    }

    //Llenar el combo box de las tablas correspondientes a la base de datos.
    public ObservableList<String> getTables(String dataBaseName, Connection connection) {
        ObservableList<String> tables = FXCollections.observableArrayList();
        String query = "SHOW TABLES FROM " + dataBaseName;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));  // Obtener el nombre de la tabla
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    //Hacer columnas dinamicas y consulta sql para llenar el table view de la interfaz
    public void loadTableData(String databaseName, String tableName, Connection connection, String typeQuery) {
        this.tblStructure.getItems().clear();
        this.tblStructure.getColumns().clear();

        String query = typeQuery.equals("search") ? "DESCRIBE " + databaseName + "." + tableName : "SELECT * FROM " + databaseName + "." + tableName;

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void doQuery(ActionEvent event) {

        if (this.cbxDB.getValue() != null) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Queries.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                QueriesController query = loader.getController();
                query.setDBName(this.cbxDB.getValue());
                query.setConnection(this.connection);

                Stage stage = new Stage();
                stage.setScene(scene);
                //stage.setOnCloseRequest(even->{even.consume();});
                stage.setResizable(false);
                stage.setTitle("Manejo de clientes");

                stage.show();

                Stage myStage = (Stage) this.btnQuery.getScene().getWindow();
                myStage.close();

            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(QueriesController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

        } else {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una base de datos");
        }

    }

    @FXML
    private void doShowAll(ActionEvent event) {
        String selectedDatabase = this.cbxDB.getValue();
        String selectedTable = this.cbxTable.getValue();
        if (selectedDatabase != null && selectedTable != null) {
            loadTableData(selectedDatabase, selectedTable, connection, "searchAll");
        }
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(null, message, title, messageType);
    }

}
