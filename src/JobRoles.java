import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class JobRoles extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnUpdate;
    private JButton btnSubmit;
    private JPanel mainPanel;
    private JLabel titleLabel;

    public JobRoles() {
        setLayout(new BorderLayout());
        setBackground(new Color(250, 248, 228));

        // Title label
        titleLabel = new JLabel("Job Role Details", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Bodoni MT", Font.BOLD, 48));
        add(titleLabel, BorderLayout.NORTH);

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
        add(scrollPane, BorderLayout.CENTER);

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
        buttonPanel.setBackground(new Color(250, 248, 228));
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnSubmit);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadJobRoles() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitApp", "root", "root")) {
            String query = "SELECT * FROM jobRoles";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            tableModel.setRowCount(0);
            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getString("jobId"),
                        resultSet.getString("jobTitle"),
                        resultSet.getString("hourlyRate"),
                        resultSet.getString("otRate"),
                        resultSet.getString("allowances"),
                        resultSet.getString("deductions")
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
                String jobId = (String) tableModel.getValueAt(i, 0);
                String jobTitle = (String) tableModel.getValueAt(i, 1);
                String hourlyRate = (String) tableModel.getValueAt(i, 2);
                String otRate = (String) tableModel.getValueAt(i, 3);
                String allowances = (String) tableModel.getValueAt(i, 4);
                String deductions =  (String) tableModel.getValueAt(i, 5);

                // Logging to debug
                System.out.println("Updating Job Role: " + jobId);
                System.out.println("Job Title: " + jobTitle);
                System.out.println("Hourly Rate: " + hourlyRate);
                System.out.println("OT Rate: " + otRate);
                System.out.println("Allowances: " + allowances);
                System.out.println("Deductions: " + deductions);

                updateStatement.setString(1, jobTitle);
                updateStatement.setString(2, hourlyRate);
                updateStatement.setString(3, otRate);
                updateStatement.setString(4, allowances);
                updateStatement.setString(5, deductions);
                updateStatement.setString(6, jobId);
                updateStatement.addBatch();
            }

            int[] updateCounts = updateStatement.executeBatch();
            connection.commit();  // Commit transaction
            updateStatement.close();

            // Check the update counts
            for (int count : updateCounts) {
                if (count == Statement.SUCCESS_NO_INFO) {
                    System.out.println("Update succeeded but number of rows affected is unknown.");
                } else if (count == Statement.EXECUTE_FAILED) {
                    System.out.println("Update failed.");
                } else {
                    System.out.println("Update succeeded; number of rows affected: " + count);
                }
            }

            JOptionPane.showMessageDialog(this, "Job roles updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating job roles: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid number format in table: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Job Roles Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.add(new JobRoles());
        frame.setVisible(true);
    }
}
