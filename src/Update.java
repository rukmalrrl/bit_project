import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Update {
    private JTextField search;
    private JButton btnSearch;
    private JRadioButton maleRadioButton;
    private JRadioButton femaleRadioButton;
    private JButton btnUpdate;
    private JButton btnCancel;
    private JTextField otRate;
    private JTextField bAccount;
    private JTextField fName;
    private JTextField lName;
    private JTextField bDay;
    private JTextField address;
    private JTextField nic;
    private JTextField eMail;
    private JTextField jobRoll;
    private JTextField basicSalary;
    private JPanel updatePanel;

    public Update() {
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchEmployee();
            }
        });

        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEmployee();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
    }

    private void searchEmployee() {
        String searchText = search.getText();
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(updatePanel, "Please enter the employee name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] names = searchText.split(" ");
        if (names.length != 2) {
            JOptionPane.showMessageDialog(updatePanel, "Please enter the full name (first and last name)", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String firstName = names[0];
        String lastName = names[1];

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitApp", "root", "root");
            String searchQuery = "SELECT * FROM employeeDetails WHERE first_name = ? AND last_name = ?";
            PreparedStatement searchStatement = connection.prepareStatement(searchQuery);
            searchStatement.setString(1, firstName);
            searchStatement.setString(2, lastName);
            ResultSet resultSet = searchStatement.executeQuery();

            if (resultSet.next()) {
                fName.setText(resultSet.getString("first_name"));
                lName.setText(resultSet.getString("last_name"));
                address.setText(resultSet.getString("address"));
                bDay.setText(resultSet.getString("birthday"));
                nic.setText(resultSet.getString("nic"));
                eMail.setText(resultSet.getString("email"));
                jobRoll.setText(resultSet.getString("job_role"));
                basicSalary.setText(resultSet.getString("basic_salary"));
                otRate.setText(resultSet.getString("ot_rate"));
                bAccount.setText(resultSet.getString("bank_account"));
                String gender = resultSet.getString("gender");
                if ("Male".equals(gender)) {
                    maleRadioButton.setSelected(true);
                    femaleRadioButton.setSelected(false);
                } else if ("Female".equals(gender)) {
                    femaleRadioButton.setSelected(true);
                    maleRadioButton.setSelected(false);
                }
            } else {
                JOptionPane.showMessageDialog(updatePanel, "Employee not found", "Error", JOptionPane.ERROR_MESSAGE);
            }

            resultSet.close();
            searchStatement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(updatePanel, "Error searching for employee: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEmployee() {
        String firstName = fName.getText();
        String lastName = lName.getText();
        String addressText = address.getText();
        String birthdayText = bDay.getText();
        String nicText = nic.getText();
        String emailText = eMail.getText();
        String jobRoleText = jobRoll.getText();
        String basicSalaryText = basicSalary.getText();
        String otRateText = otRate.getText();
        String bankAccountText = bAccount.getText();
        String gender = maleRadioButton.isSelected() ? "Male" : "Female";

        if (firstName.isEmpty() || lastName.isEmpty() || addressText.isEmpty() || birthdayText.isEmpty() || nicText.isEmpty() ||
                emailText.isEmpty() || jobRoleText.isEmpty() || basicSalaryText.isEmpty() || otRateText.isEmpty() ||
                bankAccountText.isEmpty() || (!maleRadioButton.isSelected() && !femaleRadioButton.isSelected())) {
            JOptionPane.showMessageDialog(updatePanel, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitApp", "root", "root");
            String updateQuery = "UPDATE employeeDetails SET address = ?, birthday = ?, nic = ?, email = ?, gender = ?, job_role = ?, basic_salary = ?, ot_rate = ?, bank_account = ? WHERE first_name = ? AND last_name = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setString(1, addressText);
            updateStatement.setString(2, birthdayText);
            updateStatement.setString(3, nicText);
            updateStatement.setString(4, emailText);
            updateStatement.setString(5, gender);
            updateStatement.setString(6, jobRoleText);
            updateStatement.setString(7, basicSalaryText);
            updateStatement.setString(8, otRateText);
            updateStatement.setString(9, bankAccountText);
            updateStatement.setString(10, firstName);
            updateStatement.setString(11, lastName);

            int affectedRows = updateStatement.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(updatePanel, "Employee details updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(updatePanel, "Error updating employee details", "Error", JOptionPane.ERROR_MESSAGE);
            }

            updateStatement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(updatePanel, "Error updating employee: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        fName.setText("");
        lName.setText("");
        address.setText("");
        bDay.setText("");
        nic.setText("");
        eMail.setText("");
        jobRoll.setText("");
        basicSalary.setText("");
        otRate.setText("");
        bAccount.setText("");
        maleRadioButton.setSelected(false);
        femaleRadioButton.setSelected(false);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Update Employee Details");
        frame.setContentPane(new Update().updatePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
