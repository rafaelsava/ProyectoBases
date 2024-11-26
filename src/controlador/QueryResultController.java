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
import controlador.QueriesController;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TextInputDialog;
import javax.swing.JOptionPane;

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

    private String DBName;
    @FXML
    private Button btnVIista;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.tblQueryResult.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        this.loadTableData();

    }

    public void setDBName(String nombre) {
        this.DBName = nombre;

    }

    public void setQuery(String query) {
        this.query = query;
        this.btnEditar.setDisable(this.isMultiTableQuery(this.query));
        this.btnEliminar.setDisable(this.isMultiTableQuery(this.query));

    }

    public void loadTableData() {
        // Limpia los datos existentes en el TableView para evitar conflictos con nuevas consultas.
        this.tblQueryResult.getItems().clear(); // Elimina todas las filas actuales.
        this.tblQueryResult.getColumns().clear(); // Elimina todas las columnas actuales.

        // Usa un bloque try-with-resources para manejar automáticamente los recursos como Statement y ResultSet.
        try (Statement statement = connection.createStatement(); // Crea un Statement para ejecutar la consulta.
                 ResultSet resultSet = statement.executeQuery(this.query)) { // Ejecuta la consulta y obtiene el resultado en un ResultSet.

            // Obtiene la metadata del ResultSet, que contiene información sobre las columnas devueltas.
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount(); // Obtiene el número de columnas en el resultado.

            // Itera sobre cada columna para crear columnas en el TableView.
            for (int i = 1; i <= columnCount; i++) {
                final int columnIndex = i; // Almacena el índice de la columna actual.
                String columnName = metaData.getColumnName(i); // Obtiene el nombre de la columna desde la metadata.

                // Crea una nueva columna para el TableView.
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnName);

                // Configura cómo se mostrará cada celda de la columna.
                column.setCellValueFactory(data
                        -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(columnIndex - 1)));

                // Agrega la columna al TableView.
                this.tblQueryResult.getColumns().add(column);
            }

            // Crea una lista para almacenar los datos que se mostrarán en el TableView.
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

            // Recorre cada fila del ResultSet para extraer los datos.
            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList(); // Crea una lista para almacenar una fila.

                // Itera sobre cada columna de la fila actual y agrega los valores a la lista.
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getString(i)); // Obtiene el valor de la columna y lo agrega a la fila.
                }

                // Agrega la fila completa a la lista de datos.
                data.add(row);
            }

            // Establece los datos en el TableView para que sean mostrados al usuario.
            this.tblQueryResult.setItems(data);
        } catch (SQLException e) {
            // Muestra el error en caso de que ocurra un problema al ejecutar la consulta.
            e.printStackTrace();
        }
    }

    @FXML
    private void doBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Queries.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setScene(scene);
            //stage.setOnCloseRequest(even->{even.consume();});
            stage.setResizable(false);
            stage.setTitle("Queries");

            QueriesController queryConfig = loader.getController();
            queryConfig.setConnection(this.connection);
            queryConfig.setDBName(this.DBName);

            stage.show();

            Stage myStage = (Stage) this.btnBack.getScene().getWindow();
            myStage.close();

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(QueriesController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void doVista(ActionEvent event) {
        // Mostrar un JOptionPane de confirmación
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Crear Vista");
        dialog.setHeaderText("Ingrese el nombre de la vista:");
        dialog.setContentText("Nombre:");

        // Recoger la entrada del usuario
        String viewName = dialog.showAndWait().orElse(null);

        // Validar si se ingresó un nombre
        if (viewName == null || viewName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe ingresar un nombre válido para la vista.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Construir el query con el nombre de la base de datos y la vista
        String createViewQuery = "CREATE VIEW " + this.DBName + "." + viewName + " AS " + this.query;

        try (Statement statement = connection.createStatement()) {
            // Crear la vista
            statement.executeUpdate(createViewQuery);
            JOptionPane.showMessageDialog(null, "Vista creada exitosamente: " + viewName, "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al crear la vista: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Detectar si la consulta involucra múltiples tablas
    private boolean isMultiTableQuery(String query) {
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.contains("from")) {
            int fromIndex = lowerQuery.indexOf("from") + 5;
            String fromClause = lowerQuery.substring(fromIndex).split("where")[0].trim();

            // Verificar si hay múltiples tablas separadas por coma
            return fromClause.contains(",");
        }
        return false;
    }

    // Extraer el nombre de la tabla base (para consultas simples)
    private String getTableNameFromQuery(String query) {
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.startsWith("select") && lowerQuery.contains("from")) {
            int fromIndex = lowerQuery.indexOf("from") + 5;
            String fromClause = lowerQuery.substring(fromIndex).split("where")[0].trim();

            // Retornar la primera tabla (para consultas simples)
            return fromClause.split(",")[0].trim();
        }
        throw new IllegalArgumentException("No se pudo derivar el nombre de la tabla de la consulta: " + query);
    }

    private String buildWhereClause(ObservableList<TableColumn<ObservableList<String>, ?>> columns, ObservableList<String> row) {
        // Crea un objeto StringBuilder para construir dinámicamente la cláusula WHERE.
        StringBuilder whereClause = new StringBuilder(" WHERE ");

        // Itera sobre la lista de columnas y la fila seleccionada.
        for (int i = 0; i < columns.size(); i++) {
            // Obtiene el nombre de la columna.
            String columnName = columns.get(i).getText();

            // Obtiene el valor correspondiente en la fila para la columna actual.
            String value = row.get(i);

            // Verifica si el valor es nulo.
            if (value == null) {
                // Si el valor es nulo, añade "IS NULL" a la cláusula WHERE para esta columna.
                whereClause.append(columnName).append(" IS NULL");
            } else {
                // Si el valor no es nulo, añade "columnName = 'value'" a la cláusula WHERE.
                whereClause.append(columnName).append(" = '").append(value).append("'");
            }

            // Si no es la última columna, añade un operador "AND" para concatenar condiciones.
            if (i < columns.size() - 1) {
                whereClause.append(" AND ");
            }
        }

        // Convierte el StringBuilder en un String y lo retorna.
        return whereClause.toString();
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(null, message, title, messageType);
    }

    @FXML
    private void doEditar(ActionEvent event) {
        // Obtiene las filas seleccionadas en el TableView.
        ObservableList<ObservableList<String>> selectedRows = tblQueryResult.getSelectionModel().getSelectedItems();

        // Verifica que solo se haya seleccionado exactamente una fila.
        if (selectedRows.size() != 1) {
            showMessage("Debe seleccionar exactamente un registro para editar.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Sale del método si no hay exactamente una fila seleccionada.
        }

        // Obtiene la fila seleccionada.
        ObservableList<String> selectedRow = selectedRows.get(0);

        // Obtiene las columnas del TableView y sus tipos de datos asociados.
        ObservableList<TableColumn<ObservableList<String>, ?>> columns = tblQueryResult.getColumns();
        List<String[]> columnNamesWithTypes = getColumnNamesAndTypes(); // Lista con nombres de columnas y tipos de datos.

        // Prepara una lista para mostrar los nombres de las columnas con sus tipos.
        List<String> columnNames = new ArrayList<>();
        for (String[] column : columnNamesWithTypes) {
            columnNames.add(column[0] + " (Tipo: " + column[1] + ")"); // Agrega "Nombre (Tipo: TipoDato)".
        }

        // Muestra un cuadro de diálogo para seleccionar el campo a editar.
        String selectedField = (String) JOptionPane.showInputDialog(
                null, "Seleccione el campo a editar:", "Modificar Campo",
                JOptionPane.QUESTION_MESSAGE, null, columnNames.toArray(), columnNames.get(0)
        );

        // Verifica que se haya seleccionado un campo válido.
        if (selectedField == null || selectedField.trim().isEmpty()) {
            showMessage("Debe seleccionar un campo válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Sale del método si no se selecciona un campo válido.
        }

        // Obtiene el índice de la columna seleccionada y el nombre del campo.
        int columnIndex = columnNames.indexOf(selectedField); // Índice de la columna seleccionada.
        String fieldName = columnNamesWithTypes.get(columnIndex)[0]; // Nombre del campo seleccionado.

        // Obtiene el nombre de la tabla desde la consulta original.
        String tableName = getTableNameFromQuery(this.query); // Nombre de la tabla con posible prefijo.
        String tableNameOnly = tableName.substring(tableName.indexOf('.') + 1); // Remueve el prefijo si existe.

        // Declara la variable para almacenar el nuevo valor del campo.
        String newValue;

        // Verifica si el campo es una clave foránea.
        if (this.isForeignKey(this.DBName, tableNameOnly, fieldName)) {
            // Obtiene los valores válidos para la clave foránea.
            ArrayList<String> foreignKeyValues = getForeignKeyValues(this.DBName, tableNameOnly, fieldName);
            if (foreignKeyValues.isEmpty()) {
                showMessage("No hay valores válidos para la clave foránea '" + fieldName + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                return; // Sale del método si no hay valores válidos para la clave foránea.
            }

            // Muestra un cuadro de diálogo para seleccionar un valor de clave foránea.
            Object selectedValue = JOptionPane.showInputDialog(
                    null,
                    "Seleccione un valor para " + fieldName + " :",
                    "Agregar Registro",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    foreignKeyValues.toArray(),
                    foreignKeyValues.get(0) // Valor predeterminado.
            );

            // Verifica que se haya seleccionado un valor válido.
            if (selectedValue == null) {
                showMessage("Debe seleccionar un valor válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return; // Sale del método si no se selecciona un valor.
            }

            // Convierte el valor seleccionado a String.
            newValue = selectedValue.toString();
        } else {
            // Muestra un cuadro de diálogo para ingresar el nuevo valor del campo.
            newValue = JOptionPane.showInputDialog(
                    null,
                    "Ingrese el nuevo valor para el campo '" + fieldName + "' (Tipo: " + columnNamesWithTypes.get(columnIndex)[1] + "):",
                    selectedRow.get(columnIndex) // Valor actual como predeterminado.
            );

            // Verifica que se haya ingresado un valor.
            if (newValue == null) {
                showMessage("Debe ingresar un nuevo valor para el campo.", "Error", JOptionPane.ERROR_MESSAGE);
                return; // Sale del método si no se ingresa un valor.
            }
        }

        // Intenta ejecutar la actualización en la base de datos.
        try (Statement statement = connection.createStatement()) {
            // Construye la cláusula WHERE basada en la fila seleccionada.
            String whereClause = buildWhereClause(columns, selectedRow);

            // Prepara el nuevo valor para la consulta SQL (NULL si está vacío).
            String updateValue = newValue.trim().isEmpty() ? "NULL" : "'" + newValue + "'";

            // Construye la consulta SQL de actualización.
            String updateQuery = "UPDATE " + tableName + " SET " + fieldName + " = " + updateValue + whereClause;

            // Ejecuta la consulta de actualización.
            statement.executeUpdate(updateQuery);

            // Muestra un mensaje de éxito y recarga los datos del TableView.
            showMessage("Registro actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            loadTableData(); // Recarga los datos actualizados en el TableView.
        } catch (SQLException e) {
            // Muestra un mensaje de error si ocurre algún problema con la actualización.
            showMessage("Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    private void doEliminar(ActionEvent event) {
        // Obtiene las filas seleccionadas en el TableView.
        ObservableList<ObservableList<String>> selectedRows = tblQueryResult.getSelectionModel().getSelectedItems();

        // Verifica si se seleccionó al menos una fila para eliminar.
        if (selectedRows.isEmpty()) {
            showMessage("Debe seleccionar al menos un registro para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Sale del método si no hay filas seleccionadas.
        }

        // Muestra un cuadro de confirmación para verificar si el usuario desea proceder con la eliminación.
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "¿Está seguro de que desea eliminar los registros seleccionados?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        // Si el usuario selecciona "No", se cancela la operación.
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Inicia un bloque try-with-resources para manejar el recurso Statement.
        try (Statement statement = connection.createStatement()) {
            // Obtiene las columnas del TableView.
            ObservableList<TableColumn<ObservableList<String>, ?>> columns = tblQueryResult.getColumns();

            // Obtiene el nombre de la tabla desde la consulta original.
            String tableName = getTableNameFromQuery(this.query);

            // Itera sobre cada fila seleccionada para construir y ejecutar una consulta DELETE.
            for (ObservableList<String> row : selectedRows) {
                // Construye la cláusula WHERE para identificar la fila específica que se debe eliminar.
                String whereClause = buildWhereClause(columns, row);

                // Construye la consulta DELETE completa.
                String deleteQuery = "DELETE FROM " + tableName + whereClause;

                // Ejecuta la consulta DELETE.
                statement.executeUpdate(deleteQuery);
            }

            // Muestra un mensaje de éxito y recarga los datos del TableView.
            showMessage("Registros eliminados exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            loadTableData();
        } catch (SQLException e) {
            // Muestra un mensaje de error en caso de problemas con la eliminación.
            showMessage("Error al eliminar registros: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String[]> getColumnNamesAndTypes() {
        ObservableList<TableColumn<ObservableList<String>, ?>> columns = tblQueryResult.getColumns();
        List<String[]> columnNamesWithTypes = new ArrayList<>();
        String query = "DESCRIBE " + getTableNameFromQuery(this.query); // Usa el esquema de la tabla.
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String columnName = resultSet.getString("Field"); // Nombre de la columna.
                String columnType = resultSet.getString("Type"); // Tipo de la columna.

                // Verificar si columnName está en las columnas de la tabla.
                boolean columnExists = columns.stream()
                        .anyMatch(column -> column.getText().equals(columnName));

                if (columnExists) {
                    // Si la columna existe, agregarla a columnNamesWithTypes
                    columnNamesWithTypes.add(new String[]{columnName, columnType});
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columnNamesWithTypes;
    }

    private boolean isForeignKey(String databaseName, String tableName, String columnName) {
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

    private ArrayList<String> getForeignKeyValues(String databaseName, String tableName, String columnName) {
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
}
