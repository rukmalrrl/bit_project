import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class JobRoles extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnUpdate;
    private JButton btnSubmit;
    private JPanel mainPanel;
    private JLabel titleLabel;

    public JobRoles() {
        mainPanel = new JPanel(new BorderLayout());

        // Title label
        titleLabel = new JLabel("Job Role Details", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Bodoni MT", Font.BOLD, 48));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Create the table with a non-editable model
        tableModel = new DefaultTableModel(new String[]{"Job ID", "Job Title", "Hourly Rate", "OT Rate", "Allowances", "Deductions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Bodoni MT", Font.PLAIN, 20));
        table.getTableHeader().setFont(new Font("Bodoni MT", Font.BOLD, 20));
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Load data from the database
        loadJobRoles();

        // Create the buttons
        btnUpdate = new JButton("Update");
        btnUpdate.setFont(new Font("Bodoni MT", Font.BOLD, 20));
        btnSubmit = new JButton("Submit");
        btnSubmit.setFont(new Font("Bodoni MT", Font.BOLD, 20));
        btnSubmit.setVisible(false);

        // Add action listeners to the buttons
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTableEditable(true);
                btnUpdate.setVisible(false);
                btnSubmit.setVisible(true);
            }
        });

        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateJobRoles();
                setTableEditable(false);
                btnUpdate.setVisible(true);
                btnSubmit.setVisible(false);
            }
        });

        // Create a panel for the buttons and add it to the main panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnSubmit);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set up the frame
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Job Roles Management");
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void loadJobRoles() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitApp", "root", "root")) {
            String query = "SELECT * FROM jobRoles";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            tableModel.setRowCount(0);
            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getInt("jobId"),
                        resultSet.getString("jobTitle"),
                        resultSet.getInt("hourlyRate"),
                        resultSet.getInt("otRate"),
                        resultSet.getInt("allowances"),
                        resultSet.getInt("deductions")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading job roles: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setTableEditable(boolean editable) {
        tableModel = new DefaultTableModel(new String[]{"Job ID", "Job Title", "Hourly Rate", "OT Rate", "Allowances", "Deductions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return editable;
            }
        };
        table.setModel(tableModel);
        table.setFont(new Font("Bodoni MT", Font.PLAIN, 20));
        table.getTableHeader().setFont(new Font("Bodoni MT", Font.BOLD, 20));
        table.setRowHeight(30);
        loadJobRoles();
    }

    private void updateJobRoles() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitApp", "root", "root")) {
            connection.setAutoCommit(false);  // Begin transaction
            String updateQuery = "UPDATE jobRoles SET jobTitle = ?, hourlyRate = ?, otRate = ?, allowances = ?, deductions = ? WHERE jobId = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int jobId = (int) tableModel.getValueAt(i, 0);
                String jobTitle = (String) tableModel.getValueAt(i, 1);
                int hourlyRate = (int) tableModel.getValueAt(i, 2);
                int otRate = (int) tableModel.getValueAt(i, 3);
                int allowances = (int) tableModel.getValueAt(i, 4);
                int deductions = (int) tableModel.getValueAt(i, 5);

                updateStatement.setString(1, jobTitle);
                updateStatement.setInt(2, hourlyRate);
                updateStatement.setInt(3, otRate);
                updateStatement.setInt(4, allowances);
                updateStatement.setInt(5, deductions);
                updateStatement.setInt(6, jobId);
                updateStatement.addBatch();
            }
            updateStatement.executeBatch();
            connection.commit();  // Commit transaction
            updateStatement.close();
            JOptionPane.showMessageDialog(this, "Job roles updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating job roles: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JobRoles().setVisible(true);
            }
        });
    }
}
