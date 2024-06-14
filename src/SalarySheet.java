import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SalarySheet {
    JPanel sheetPanel;
    private JLabel name;
    private JLabel jobTitle;
    private JLabel monthLabel;
    private JLabel bankAccount;
    private JLabel phoneNo;
    private JLabel wHour;
    private JLabel OtHour;
    private JLabel allowances;

    private JLabel amount1;
    private JLabel amount2;
    private JLabel amount3;
    private JLabel subTotal;
    private JLabel amount4;
    private JLabel netSalary;
    private JLabel titleLabel;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bitApp";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public SalarySheet() {
    }

    public void updateFields(String employee_id, String firstName, String lastName, String jobTitleText, String month, double wHourDouble, double otHourDouble,
                             String phoneNum, String bAccount, double salary1, double salary2, double allowances, double subSalary, double deductions,
                             double netSalaryAmount) {
        // Set the name label to display both first name and last name
        name.setText(firstName + " " + lastName);
        jobTitle.setText(jobTitleText);
        monthLabel.setText(month);
        wHour.setText(String.valueOf(wHourDouble));
        OtHour.setText(String.valueOf(otHourDouble));
        phoneNo.setText(phoneNum);
        bankAccount.setText(bAccount);
        amount1.setText(String.valueOf(salary1));
        amount2.setText(String.valueOf(salary2));
        amount3.setText(String.valueOf(allowances));
        subTotal.setText(String.valueOf(subSalary));
        amount4.setText(String.valueOf(deductions));
        netSalary.setText(String.valueOf(netSalaryAmount));
        titleLabel.setText("Pay slip for the month of " + month + " 2024");
    }

    void performSearch(String id, String month) throws SQLException {
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(sheetPanel, "Please enter the employee name or ID", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isNumeric = id.chars().allMatch(Character::isDigit);

        try {
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            String searchQuery;
            PreparedStatement searchStatement;

            if (isNumeric) {
                // Search by employee ID
                searchQuery = "SELECT * FROM salaryDetails WHERE employee_id = ? AND month = ?";
                searchStatement = connection.prepareStatement(searchQuery);
                searchStatement.setString(1, id);
                searchStatement.setString(2, month);
            } else {
                // Search by employee name
                String[] names = id.split(" ");
                if (names.length != 2) {
                    JOptionPane.showMessageDialog(sheetPanel, "Please enter the full name (first and last name)", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String firstName = names[0];
                String lastName = names[1];

                searchQuery = "SELECT * FROM salaryDetails WHERE first_name = ? AND last_name = ? AND month = ?";
                searchStatement = connection.prepareStatement(searchQuery);
                searchStatement.setString(1, firstName);
                searchStatement.setString(2, lastName);
                searchStatement.setString(3, month);
            }

            ResultSet resultSet = searchStatement.executeQuery();

            if (resultSet.next()) {
                String employee_id = resultSet.getString("employee_id");
                String bAccount = "";
                String bQuery = "SELECT bank_account FROM employeeDetails WHERE employee_id = ?";
                PreparedStatement bStatement = connection.prepareStatement(bQuery);
                bStatement.setString(1, employee_id);
                ResultSet bResultSet = bStatement.executeQuery();
                if (bResultSet.next()) {
                    bAccount = bResultSet.getString("bank_account");
                }
                bResultSet.close();
                bStatement.close();

                String phoneNum = "";
                String phoneQuery = "SELECT phoneNo FROM employeeDetails WHERE employee_id = ?";
                PreparedStatement phoneStatement = connection.prepareStatement(phoneQuery);
                phoneStatement.setString(1, employee_id);
                ResultSet phoneResultSet = phoneStatement.executeQuery();
                if (phoneResultSet.next()) {
                    phoneNum = phoneResultSet.getString("phoneNo");
                }
                phoneResultSet.close();
                phoneStatement.close();

                double hourlyRate = 0.0;
                double otRate = 0.0;
                double allowances = 0.0;
                double deductions = 0.0;

                try {
                    // Query to fetch jobId from employeeDetails table
                    String jobIdQuery = "SELECT jobId FROM employeeDetails WHERE employee_id = ?";
                    PreparedStatement jobIdStatement = connection.prepareStatement(jobIdQuery);
                    jobIdStatement.setString(1, employee_id);
                    ResultSet jobIdResultSet = jobIdStatement.executeQuery();

                    // Check if a jobId exists for the employee
                    if (jobIdResultSet.next()) {
                        String jobId = jobIdResultSet.getString("jobId");

                        // Query to fetch job role details from jobRoles table using jobId
                        String jobRoleQuery = "SELECT hourlyRate, otRate, allowances, deductions FROM jobRoles WHERE jobId = ?";
                        PreparedStatement jobRoleStatement = connection.prepareStatement(jobRoleQuery);
                        jobRoleStatement.setString(1, jobId);
                        ResultSet jobRoleResultSet = jobRoleStatement.executeQuery();

                        // Check if a job role exists for the jobId
                        if (jobRoleResultSet.next()) {
                            hourlyRate = jobRoleResultSet.getDouble("hourlyRate");
                            otRate = jobRoleResultSet.getDouble("otRate");
                            allowances = jobRoleResultSet.getDouble("allowances");
                            deductions = jobRoleResultSet.getDouble("deductions");
                        }

                        // Close jobRoles result set and statement
                        jobRoleResultSet.close();
                        jobRoleStatement.close();
                    }

                    // Close jobId result set and statement
                    jobIdResultSet.close();
                    jobIdStatement.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(sheetPanel, "Error fetching job role details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }

                double wHourDouble = resultSet.getDouble("total_work_hours");
                double otHourDouble = resultSet.getDouble("total_ot_hours");
                double salary1 = hourlyRate * wHourDouble;
                double salary2 = otRate * otHourDouble;
                double subSalary = salary1 + salary2 + allowances;
                double netSalaryAmount = resultSet.getDouble("salary");

                updateFields(employee_id, resultSet.getString("first_name"), resultSet.getString("last_name"), resultSet.getString("jobTitle"), month, wHourDouble, otHourDouble,
                        phoneNum, bAccount, salary1, salary2, allowances, subSalary, deductions, netSalaryAmount);

            } else {
                JOptionPane.showMessageDialog(sheetPanel, "Employee not found for the specified ID/Name and Month", "Error", JOptionPane.ERROR_MESSAGE);
            }

            resultSet.close();
            searchStatement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(sheetPanel, "Error searching for employee: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    public static void main(String[] args) throws SQLException {
        JFrame frame = new JFrame("Salary Sheet");
        SalarySheet salarySheet = new SalarySheet();
        frame.setContentPane(salarySheet.sheetPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        // Example usage to show how to use performSearch
        String employeeId = "001";
        String selectedMonth = "May";
        salarySheet.performSearch(employeeId, selectedMonth);
    }
}
