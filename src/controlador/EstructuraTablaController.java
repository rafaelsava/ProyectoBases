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
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

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

        if (selectedDatabase != null && selectedTable != null) {
            try {
                // Obtener las columnas de la tabla seleccionada
                String describeQuery = String.format("DESCRIBE %s.%s", selectedDatabase, selectedTable);
                ArrayList<String> columnNames = new ArrayList<>();
                ArrayList<Boolean> isNullable = new ArrayList<>();
                ArrayList<String> columnTypes = new ArrayList<>();
                ArrayList<String> foreignKeys = new ArrayList<>();
                String primaryKeyColumn = null;

                try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(describeQuery)) {
                    while (resultSet.next()) {
                        String columnName = resultSet.getString("Field");
                        columnNames.add(columnName);
                        isNullable.add("YES".equalsIgnoreCase(resultSet.getString("Null")));
                        columnTypes.add(resultSet.getString("Type"));

                        // Verificar si la columna es una clave primaria
                        if ("PRI".equalsIgnoreCase(resultSet.getString("Key"))) {
                            primaryKeyColumn = columnName;
                        }

                        // Verificar si la columna es una clave foránea
                        if (isForeignKey(columnName, selectedTable, selectedDatabase)) {
                            foreignKeys.add(columnName);
                        }
                    }
                }

                // Determinar el próximo valor para la clave primaria
                int nextPrimaryKey = 1;
                if (primaryKeyColumn != null) {
                    String maxPrimaryKeyQuery = String.format("SELECT MAX(%s) AS maxKey FROM %s.%s", primaryKeyColumn, selectedDatabase, selectedTable);
                    try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(maxPrimaryKeyQuery)) {
                        if (resultSet.next()) {
                            String maxKey = resultSet.getString("maxKey");
                            if (maxKey != null && maxKey.matches("\\d+")) { // Verifica si es numérico
                                nextPrimaryKey = Integer.parseInt(maxKey) + 1;
                            } else {
                                nextPrimaryKey = 1; // Valor predeterminado si no hay registros
                            }
                        }
                    }
                }

                // Solicitar al usuario los valores de las columnas
                ArrayList<String> values = new ArrayList<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    String columnName = columnNames.get(i);
                    String columnType = columnTypes.get(i);
                    boolean nullable = isNullable.get(i);

                    String value = null;

                    // Si es la clave primaria, asignar automáticamente el próximo valor
                    if (columnName.equals(primaryKeyColumn)) {
                        values.add(String.valueOf(nextPrimaryKey));
                        continue;
                    }

                    // Si es una clave foránea, mostrar valores válidos
                    if (foreignKeys.contains(columnName)) {
                        ArrayList<String> validValues = getForeignKeyValues(columnName, selectedTable, selectedDatabase);
                        Object selectedValue = JOptionPane.showInputDialog(
                                null,
                                "Seleccione un valor para " + columnName + " (" + columnType + "):",
                                "Agregar Registro",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                validValues.toArray(),
                                validValues.get(0)
                        );
                        if (selectedValue != null) {
                            value = selectedValue.toString();
                        }
                    } else {
                        // Solicitar entrada normal para otras columnas
                        String prompt = "Ingrese el valor para " + columnName + " (" + columnType + ")";
                        if (!nullable) {
                            prompt += " (Obligatorio)";
                        }
                        do {
                            value = JOptionPane.showInputDialog(null, prompt, "Agregar Registro", JOptionPane.PLAIN_MESSAGE);
                            if (value == null && !nullable) {
                                JOptionPane.showMessageDialog(null, "Este campo es obligatorio.");
                            }
                        } while (value == null && !nullable);
                    }

                    values.add(value);
                }

                // Construir la consulta SQL para insertar el registro
                StringBuilder columns = new StringBuilder();
                StringBuilder placeholders = new StringBuilder();
                for (String columnName : columnNames) {
                    if (columns.length() > 0) {
                        columns.append(", ");
                        placeholders.append(", ");
                    }
                    columns.append(columnName);
                    placeholders.append("?");
                }

                String insertQuery = String.format(
                        "INSERT INTO %s.%s (%s) VALUES (%s)",
                        selectedDatabase,
                        selectedTable,
                        columns,
                        placeholders
                );

                // Ejecutar la consulta
                try (java.sql.PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    for (int i = 0; i < values.size(); i++) {
                        preparedStatement.setString(i + 1, values.get(i));
                    }
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Registro agregado con éxito.");
                    } else {
                        JOptionPane.showMessageDialog(null, "No se pudo agregar el registro.");
                    }
                }

                // Actualizar la tabla en la interfaz
                loadTableData(selectedDatabase, selectedTable, connection, "searchAll");

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al agregar el registro: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Por favor, selecciona una base de datos y una tabla antes de agregar un registro.");
        }
    }

    /**
     * Verifica si una columna es una clave foránea.
     */
    private boolean isForeignKey(String columnName, String tableName, String databaseName) {
        String query = String.format(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE "
                + "WHERE TABLE_NAME = '%s' AND TABLE_SCHEMA = '%s' AND COLUMN_NAME = '%s' AND REFERENCED_TABLE_NAME IS NOT NULL",
                tableName, databaseName, columnName
        );

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtiene los valores válidos para una clave foránea.
     */
    private ArrayList<String> getForeignKeyValues(String columnName, String tableName, String databaseName) {
        ArrayList<String> values = new ArrayList<>();
        String query = String.format(
                "SELECT REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME "
                + "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE "
                + "WHERE TABLE_NAME = '%s' AND TABLE_SCHEMA = '%s' AND COLUMN_NAME = '%s' AND REFERENCED_TABLE_NAME IS NOT NULL",
                tableName, databaseName, columnName
        );

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                String referencedTable = resultSet.getString("REFERENCED_TABLE_NAME");
                String referencedColumn = resultSet.getString("REFERENCED_COLUMN_NAME");
                String valueQuery = String.format("SELECT %s FROM %s.%s", referencedColumn, databaseName, referencedTable);

                try (Statement valueStatement = connection.createStatement(); ResultSet valueResultSet = valueStatement.executeQuery(valueQuery)) {
                    while (valueResultSet.next()) {
                        values.add(valueResultSet.getString(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return values;
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
