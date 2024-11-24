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
    @FXML
    private Button btnEditarResgistro;

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
                            nextPrimaryKey = resultSet.getInt("maxKey") + 1;
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
    }

    @FXML
    private void doShowAll(ActionEvent event) {
        String selectedDatabase = this.cbxDB.getValue();
        String selectedTable = this.cbxTable.getValue();
        if (selectedDatabase != null && selectedTable != null) {
            loadTableData(selectedDatabase, selectedTable, connection, "searchAll");
        }
    }

    @FXML

    private void doEditar(ActionEvent event) {
        ObservableList<String> seleccionado = tblStructure.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            ObservableList<TableColumn<ObservableList<String>, ?>> columnas = tblStructure.getColumns();
            ArrayList<String> nombresColumnas = new ArrayList<>();
            for (TableColumn<ObservableList<String>, ?> columna : columnas) {
                nombresColumnas.add(columna.getText());
            }

            // Obtener las claves primarias de la tabla seleccionada
            String selectedDatabase = this.cbxDB.getValue();
            String selectedTable = this.cbxTable.getValue();
            ArrayList<String> clavesPrimarias = getPrimaryKeys(selectedDatabase, selectedTable);

            // Mostrar cuadro de diálogo para elegir la columna a editar
            String columnaSeleccionada = (String) JOptionPane.showInputDialog(
                    null,
                    "Seleccione la columna que desea editar:",
                    "Editar Columna",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    nombresColumnas.toArray(),
                    nombresColumnas.get(0)
            );

            if (columnaSeleccionada != null) {
                if (clavesPrimarias.contains(columnaSeleccionada)) {
                    JOptionPane.showMessageDialog(null, "No se puede editar la columna clave primaria: " + columnaSeleccionada);
                    return;
                }

                int indiceColumna = nombresColumnas.indexOf(columnaSeleccionada);
                String valorActual = seleccionado.get(indiceColumna);
                String nuevoValor = JOptionPane.showInputDialog(
                        "Editar valor para la columna: " + columnaSeleccionada,
                        valorActual
                );

                if (nuevoValor != null && !nuevoValor.isEmpty()) {
                    // Actualizar en la tabla
                    seleccionado.set(indiceColumna, nuevoValor);
                    tblStructure.refresh();

                    // Actualizar en la base de datos
                    try (Statement statement = connection.createStatement()) {
                        String clavePrimaria = tblStructure.getColumns().get(0).getText();
                        String valorClavePrimaria = seleccionado.get(0); // Obtener el valor de la clave primaria

                        String query = String.format(
                                "UPDATE %s.%s SET %s = '%s' WHERE %s = '%s'",
                                selectedDatabase,
                                selectedTable,
                                columnaSeleccionada,
                                nuevoValor,
                                clavePrimaria,
                                valorClavePrimaria
                        );

                        int rowsAffected = statement.executeUpdate(query);
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(null, "Registro actualizado con éxito en la base de datos.");
                        } else {
                            JOptionPane.showMessageDialog(null, "No se pudo actualizar el registro.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error al actualizar el registro en la base de datos.");
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Por favor, selecciona un registro para editar.");
        }
    }

    /**
     * Obtiene las claves primarias de una tabla específica.
     *
     * @param databaseName El nombre de la base de datos.
     * @param tableName El nombre de la tabla.
     * @return Una lista con los nombres de las columnas que son claves
     * primarias.
     */
    private ArrayList<String> getPrimaryKeys(String databaseName, String tableName) {
        ArrayList<String> primaryKeys = new ArrayList<>();
        String query = String.format("SHOW KEYS FROM %s.%s WHERE Key_name = 'PRIMARY'", databaseName, tableName);

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                primaryKeys.add(resultSet.getString("Column_name")); // Nombre de la columna clave primaria
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return primaryKeys;
    }

}
