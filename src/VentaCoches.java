/*
    Cavero Fernández Víctor
*/

import javax.swing.*;
import java.sql.*;


//Clase principal de la aplicación
public class VentaCoches {

    //Componentes del formulario Swing
    public JPanel panel1;
    private JComboBox<String> comboBoxModelo;
    private JComboBox<String> comboBoxMotor;
    private JComboBox<String> comboBoxColor;
    private JComboBox<String> comboBoxRuedas;
    private JComboBox<String> comboBoxPiloto;
    private JButton btnHacerPedido;
    private JButton btnVisualizarPedidos;
    private JButton btnEliminarPedido;
    private JList<String> listaPedidos;

    //Modelo de lista para el JList
    private DefaultListModel<String> modeloLista = new DefaultListModel<>();

    //Conexión con la base de datos en MySQL
    private final String URL = "jdbc:mysql://localhost:3306/finalCoches";
    private final String USER = "root";
    private final String PASS = "root";

    //Nombres de columnas y tablas correspondientes a cada comboBox
    private final String[] columnas = {"modelo", "motor", "color", "ruedas", "pilotoAutomatico"};
    private final String[] tablas = {"opcionesModelo", "opcionesMotor", "opcionesColor", "opcionesRuedas", "opcionesPilotoAutomatico"};

    //Interfaz
    public VentaCoches() {
        listaPedidos.setModel(modeloLista); //Asigna el modelo al JList
        cargarComboBoxes(); //Rellena los comboBoxes desde la BD

        //Definición de botones con sus acciones
        btnHacerPedido.addActionListener(e -> hacerPedido());
        btnVisualizarPedidos.addActionListener(e -> mostrarPedidos());
        btnEliminarPedido.addActionListener(e -> eliminarPedidoSeleccionado());

        btnEliminarPedido.setEnabled(false);
    }

    //Rellenar los ComboBox con los datos de cada tabla en la base de datos
    private void cargarComboBoxes() {
        JComboBox<?>[] combos = {comboBoxModelo, comboBoxMotor, comboBoxColor, comboBoxRuedas, comboBoxPiloto};

        for (int i = 0; i < combos.length; i++) {
            JComboBox<String> combo = (JComboBox<String>) combos[i];
            combo.removeAllItems(); // Limpia el comboBox

            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT " + columnas[i] + " FROM " + tablas[i])) {

                // Añade cada valor como una opción del comboBox
                while (rs.next()) {
                    String valor = rs.getString(columnas[i]);
                    //Comprobar si la opción seleccionada en el indice 4 (pilotoAutomatico) es true o false
                    if (i == 4) {
                        if ("1".equals(valor)) { //En caso de ser 1 es TRUE
                            combo.addItem("Si"); //Introducimos TRUE en el combobox
                        } else if ("0".equals(valor)) { //En caso de ser 0 es FALSE
                            combo.addItem("No"); //Introducimos FALSE en el combobox
                        }
                    } else {
                        combo.addItem(valor); //Para el resto de indices, simplemente añadimos al combobox
                    }
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(panel1, "Error cargando " + tablas[i] + ": " + e.getMessage());
            }
        }
    }

    //Registrar pedidos en la base de datos
    private void hacerPedido() {
        // Captura las selecciones del usuario
        String modelo = (String) comboBoxModelo.getSelectedItem();
        String motor = (String) comboBoxMotor.getSelectedItem();
        String color = (String) comboBoxColor.getSelectedItem();
        String ruedas = (String) comboBoxRuedas.getSelectedItem();
        String pilotoAutomatico = (String) comboBoxPiloto.getSelectedItem();

        //Conversion de las variables del piloto automatico
        if (pilotoAutomatico == "Si") {
            pilotoAutomatico = "0";
        } else {
            pilotoAutomatico = "1";
        }

        //Verifica que no haya campos vacíos
        if (modelo == null || motor == null || color == null || ruedas == null || pilotoAutomatico == null) {
            JOptionPane.showMessageDialog(panel1, "Debes seleccionar todas las opciones.");
            return;
        }

        //Confirma con el usuario
        int confirm = JOptionPane.showConfirmDialog(panel1, "¿Confirmar pedido?");
        if (confirm != JOptionPane.YES_OPTION) return;

        //Inserta el pedido en la tabla "pedidos"
        String sql = "INSERT INTO pedidos (modelo, motor, color, ruedas, pilotoAutomatico) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            //Asigna los valores a la consulta
            ps.setString(1, modelo);
            ps.setString(2, motor);
            ps.setString(3, color);
            ps.setString(4, ruedas);
            ps.setString(5, pilotoAutomatico);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(panel1, "Pedido realizado.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel1, "Error al hacer el pedido: " + e.getMessage());
        }
    }

    //Muestra todos los pedidos existentes en el JList
    private void mostrarPedidos() {
        modeloLista.clear(); //Limpia la lista antes de cargar

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pedidos")) {

            while (rs.next()) {
                //Conversion de las variables dentro del pilotoAutomatico
                String pilotoAutomatico = rs.getString("pilotoAutomatico");
                if (pilotoAutomatico == "0") {
                    pilotoAutomatico = "Si";
                } else {
                    pilotoAutomatico = "No";
                }

                //Crea una cadena con todos los datos del pedido
                String linea = rs.getInt("id") + ": " +
                        rs.getString("modelo") + ", " +
                        rs.getString("motor") + ", " +
                        rs.getString("color") + ", " +
                        rs.getString("ruedas") + ", " +
                        pilotoAutomatico;
                modeloLista.addElement(linea); // Añade a la lista
            }

            //Activa o desactiva el botón de eliminación según si hay pedidos
            if (modeloLista.isEmpty()) {
                JOptionPane.showMessageDialog(panel1, "No hay pedidos registrados.");
                btnEliminarPedido.setEnabled(false);
            } else {
                btnEliminarPedido.setEnabled(true);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel1, "Error al mostrar pedidos: " + e.getMessage());
        }
    }

    //Elimina el pedido seleccionado de la base de datos
    private void eliminarPedidoSeleccionado() {
        String seleccionado = listaPedidos.getSelectedValue();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(panel1, "No hay pedido seleccionado.");
            return;
        }

        //Confirma eliminación
        int confirm = JOptionPane.showConfirmDialog(panel1, "¿Eliminar pedido seleccionado?");
        if (confirm != JOptionPane.YES_OPTION) return;

        //Extrae el ID del pedido desde el string del JList
        int id = Integer.parseInt(seleccionado.split(":")[0]);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM pedidos WHERE id = ?")) {

            ps.setInt(1, id); // Pasa el ID al DELETE
            ps.executeUpdate();

            JOptionPane.showMessageDialog(panel1, "Pedido eliminado.");


        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel1, "Error al eliminar: " + e.getMessage());
        }
    }

    //Mostrar el programa
    public static void main(String[] args) {
        JFrame frame = new JFrame("Venta de Coches");
        frame.setContentPane(new VentaCoches().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
