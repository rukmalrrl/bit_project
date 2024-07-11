import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Delete {
    private JTextField search;
    private JButton btnSearch;
    private JLabel fname;
    private JLabel lName;
    private JLabel bDay;
    private JLabel address;
    private JLabel nic;
    private JLabel email;
    private JLabel gender;
    private JLabel jobId;
    private JLabel phoneNo;
    private JLabel bankAccount;
    private JLabel lbl1;
    private JLabel lbl2;
    private JLabel lbl4;
    private JLabel lbl3;
    private JLabel lbl5;
    private JLabel lbl6;
    private JLabel lbl7;
    private JLabel lbl8;
    private JLabel lbl10;
    private JLabel lbl11;
    JPanel deletePanel;
    private JButton btnDelete;

    public Delete() {
        btnDelete.setVisible(false);  // Initially set the delete button to be invisible

        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performDelete();
            }
        });
    }

    private void performSearch() {
        String searchText = search.getText();
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(deletePanel, "Please enter the employee name or ID", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isNumeric = searchText.chars().allMatch(Character::isDigit);

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitApp", "root", "root");
            String searchQuery;
            PreparedStatement searchStatement;

            if (isNumeric) {
                // Search by employee ID
                searchQuery = "SELECT * FROM employeeDetails WHERE employee_id = ?";
                searchStatement = connection.prepareStatement(searchQuery);
                searchStatement.setString(1, searchText);
            } else {
                // Search by employee name
                String[] names = searchText.split(" ");
                if (names.length != 2) {
                    JOptionPane.showMessageDialog(deletePanel, "Please enter the full name (first and last name)", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String firstName = names[0];
                String lastName = names[1];

                searchQuery = "SELECT * FROM employeeDetails WHERE first_name = ? AND last_name = ?";
                searchStatement = connection.prepareStatement(searchQuery);
                searchStatement.setString(1, firstName);
                searchStatement.setString(2, lastName);
            }

            ResultSet resultSet = searchStatement.executeQuery();

            if (resultSet.next()) {
                ShowLabel();
                fname.setText(resultSet.getString("first_name"));
                lName.setText(resultSet.getString("last_name"));
                address.setText(resultSet.getString("address"));
                bDay.setText(resultSet.getString("birthday"));
                nic.setText(resultSet.getString("nic"));
                gender.setText(resultSet.getString("gender"));
                email.setText(resultSet.getString("email"));
                jobId.setText(resultSet.getString("jobId"));
                phoneNo.setText(resultSet.getString("phoneNo"));
                bankAccount.setText(resultSet.getString("bank_account"));
                btnDelete.setVisible(true);  // Make the delete button visible after displaying search results
            } else {
                JOptionPane.showMessageDialog(deletePanel, "Employee not found", "Error", JOptionPane.ERROR_MESSAGE);
            }

            resultSet.close();
            searchStatement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(deletePanel, "Error searching for employee: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performDelete() {
        String firstName = fname.getText();
        String lastName = lName.getText();
        int employeeId = -1;

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitApp", "root", "root");

            // Retrieve the employee ID first
            String selectQuery = "SELECT employee_id FROM employeeDetails WHERE first_name = ? AND last_name = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setString(1, firstName);
            selectStatement.setString(2, lastName);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                employeeId = resultSet.getInt("employee_id");
            } else {
                JOptionPane.showMessageDialog(deletePanel, "Error finding employee", "Error", JOptionPane.ERROR_MESSAGE);
                resultSet.close();
                selectStatement.close();
                connection.close();
                return;
            }

            resultSet.close();
            selectStatement.close();

            // Delete dependent rows in the attendance table
            String deleteAttendanceQuery = "DELETE FROM attendance WHERE employee_id = ?";
            PreparedStatement deleteAttendanceStatement = connection.prepareStatement(deleteAttendanceQuery);
            deleteAttendanceStatement.setInt(1, employeeId);
            deleteAttendanceStatement.executeUpdate();
            deleteAttendanceStatement.close();

            // Now delete the employee
            String deleteEmployeeQuery = "DELETE FROM employeeDetails WHERE employee_id = ?";
            PreparedStatement deleteEmployeeStatement = connection.prepareStatement(deleteEmployeeQuery);
            deleteEmployeeStatement.setInt(1, employeeId);
            int rowsAffected = deleteEmployeeStatement.executeUpdate();
            deleteEmployeeStatement.close();

            connection.close();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(deletePanel, "Employee deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearLabels();
                btnDelete.setVisible(false);  // Hide the delete button after deletion
            } else {
                JOptionPane.showMessageDialog(deletePanel, "Error deleting employee", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(deletePanel, "Error deleting employee: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void ShowLabel() {
        lbl1.setText("First Name    :");
        lbl2.setText("Last Name     :");
        lbl3.setText("B Day         :");
        lbl4.setText("Address       :");
        lbl5.setText("NIC           :");
        lbl6.setText("Gender        :");
        lbl7.setText("E mail        :");
        lbl8.setText("Job Id      :");
        lbl10.setText("Phone No        :");
        lbl11.setText("Bank Account :");
    }

    public void clearLabels() {
        fname.setText("");
        lName.setText("");
        bDay.setText("");
        address.setText("");
        nic.setText("");
        gender.setText("");
        email.setText("");
        jobId.setText("");
        phoneNo.setText("");
        bankAccount.setText("");
        lbl1.setText("");
        lbl2.setText("");
        lbl3.setText("");
        lbl4.setText("");
        lbl5.setText("");
        lbl6.setText("");
        lbl7.setText("");
        lbl8.setText("");
        lbl10.setText("");
        lbl11.setText("");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Delete Employee Details");
        Delete delete = new Delete();
        frame.setContentPane(delete.deletePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
