import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class PrintSalarySheet extends JPanel {
    private JTextField employeeField;
    private JComboBox<String> monthSelector;
    private JButton previewButton;
    private JButton printButton;
    private JButton printAllButton;
    private SalarySheet salarySheet; // Instance of SalarySheet to interact with

    public PrintSalarySheet() {
        setBackground(new Color(250, 246, 228));
        setLayout(new BorderLayout());

        // Create the panel for the "Salary Report" label
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(250, 246, 228));
        JLabel titleLabel = new JLabel("Print Salary Sheet", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Bodoni MT", Font.BOLD, 36));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Create the panel for the rest of the content
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(new Color(250, 246, 228));
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel monthLabel = new JLabel("Select Month:");
        monthLabel.setFont(new Font("Bodoni MT", Font.PLAIN, 20));
        monthSelector = new JComboBox<>(new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
        monthSelector.setFont(new Font("Bodoni MT", Font.PLAIN, 20));

        contentPanel.add(monthLabel, gbc);
        gbc.gridx++;
        contentPanel.add(monthSelector, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel employeeLabel = new JLabel("Employee ID/Name:");
        employeeLabel.setFont(new Font("Bodoni MT", Font.PLAIN, 20));
        contentPanel.add(employeeLabel, gbc);
        gbc.gridx++;
        employeeField = new JTextField(15);
        employeeField.setFont(new Font("Bodoni MT", Font.PLAIN, 20));
        contentPanel.add(employeeField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        previewButton = new JButton("Preview");
        previewButton.setFont(new Font("Bodoni MT", Font.BOLD, 20));
        previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previewSalarySlip();
            }
        });

        printButton = new JButton("Print");
        printButton.setFont(new Font("Bodoni MT", Font.BOLD, 20));
        printButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printSalarySlip();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 246, 228));
        buttonPanel.add(previewButton);
        buttonPanel.add(printButton);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        contentPanel.add(buttonPanel, gbc);

        gbc.gridy++;
        printAllButton = new JButton("Print All");
        printAllButton.setFont(new Font("Bodoni MT", Font.BOLD, 20));
        printAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printAllSalarySlips();
            }
        });
        contentPanel.add(printAllButton, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // Initialize SalarySheet instance
        salarySheet = new SalarySheet();
    }

    private void previewSalarySlip() {
        String employee = employeeField.getText();
        if (employee.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select employee first");
        } else {
            String month = (String) monthSelector.getSelectedItem();
            // Call performSearch method of SalarySheet instance
            try {
                salarySheet.performSearch(employee, month);
                // Create and display the SalarySheet frame
                JFrame frame = new JFrame("Salary Sheet");
                frame.setContentPane(salarySheet.sheetPanel);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error retrieving salary slip: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printSalarySlip() {
        String employee = employeeField.getText();
        String month = (String) monthSelector.getSelectedItem();
        if (employee.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select employee first");
        } else {
            // In real application, implement printing functionality
            JOptionPane.showMessageDialog(this, "Printing salary slip for " + employee + " for the month of " + month);
        }
    }

    private void printAllSalarySlips() {
        String month = (String) monthSelector.getSelectedItem();
        // In real application, implement printing all salary slips functionality
        JOptionPane.showMessageDialog(this, "Printing all salary slips for " + month);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Example of how to integrate into a main dashboard
            JFrame frame = new JFrame("Main Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            // Create instance of PrintSalarySheet panel
            PrintSalarySheet printSalarySheet = new PrintSalarySheet();

            // Add PrintSalarySheet panel to the main dashboard
            frame.getContentPane().add(printSalarySheet, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }
}
