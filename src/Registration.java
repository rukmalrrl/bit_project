import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Registration {
    private JTextField fName;
    private JTextField lName;
    private JTextField address;
    private JTextField id;
    private JTextField nic;
    private JTextField eMail;
    private JTextField jobRoll;
    private JTextField basicSalary;
    private JTextField otRate;
    private JTextField bAccount;
    private JRadioButton maleRadioButton;
    private JTextField bDay;
    private JRadioButton femaleRadioButton;
    private JButton btnRegister;
    private JButton btnCancel;
    private JPanel registerPanel;

    public Registration() {
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the values from the text fields
                String firstName = fName.getText();
                String lastName = lName.getText();
                String addressText = address.getText();
                String idText = id.getText();
                String nicText = nic.getText();
                String emailText = eMail.getText();
                String jobRoleText = jobRoll.getText();
                String basicSalaryText = basicSalary.getText();
                String otRateText = otRate.getText();
                String bankAccountText = bAccount.getText();
                String birthdayText = bDay.getText();
                String gender = maleRadioButton.isSelected() ? "Male" : "Female";

                if (firstName.isEmpty() || lastName.isEmpty() || addressText.isEmpty() || idText.isEmpty() || nicText.isEmpty() ||
                        emailText.isEmpty() || jobRoleText.isEmpty() || basicSalaryText.isEmpty() || otRateText.isEmpty() ||
                        bankAccountText.isEmpty() || birthdayText.isEmpty() || (!maleRadioButton.isSelected() && !femaleRadioButton.isSelected())) {
                    JOptionPane.showMessageDialog(registerPanel, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Store these details in the database
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitApp", "root", "root");
                    String checkQuery = "SELECT * FROM employeeDetails WHERE employee_id = ?";
                    PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                    checkStatement.setString(1, idText);
                    ResultSet resultSet = checkStatement.executeQuery();

                    if (resultSet.next()) {
                        JOptionPane.showMessageDialog(registerPanel, "Employee ID already exists: " + resultSet.getString("employee_id"), "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        // Row does not exist, insert the new row
                        String insertQuery = "INSERT INTO employeeDetails (employee_id, first_name, last_name, address, birthday, nic, email, gender, job_role, basic_salary, ot_rate, bank_account) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                        insertStatement.setString(1, idText);
                        insertStatement.setString(2, firstName);
                        insertStatement.setString(3, lastName);
                        insertStatement.setString(4, addressText);
                        insertStatement.setString(5, birthdayText);
                        insertStatement.setString(6, nicText);
                        insertStatement.setString(7, emailText);
                        insertStatement.setString(8, gender);
                        insertStatement.setString(9, jobRoleText);
                        insertStatement.setString(10, basicSalaryText);
                        insertStatement.setString(11, otRateText);
                        insertStatement.setString(12, bankAccountText);
                        insertStatement.executeUpdate();
                        JOptionPane.showMessageDialog(registerPanel, "Inserted new employee: " + firstName + " " + lastName, "Success", JOptionPane.INFORMATION_MESSAGE);
                        ClearFields();
                        insertStatement.close();
                    }
                    resultSet.close();
                    checkStatement.close();
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(registerPanel, "Error registering employee: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ClearFields();
            }
        });
    }

    public void ClearFields() {
        // Clear all fields
        fName.setText("");
        lName.setText("");
        address.setText("");
        id.setText("");
        nic.setText("");
        eMail.setText("");
        jobRoll.setText("");
        basicSalary.setText("");
        otRate.setText("");
        bAccount.setText("");
        bDay.setText("");
        maleRadioButton.setSelected(false);
        femaleRadioButton.setSelected(false);
    };
    public static void main(String[] args) {
        JFrame frame = new JFrame("Register Form");
        frame.setContentPane(new Registration().registerPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setAlwaysOnTop(true);  // Set the frame to be always on top
        frame.setVisible(true);
    }
}
