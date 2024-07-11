import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Search {

    private JTextField search;
    private JButton btnSearch;
    private JLabel fname;
    private JLabel lName;
    private JLabel bDay;
    private JLabel address;
    private JLabel nic;
    private JLabel email;
    private JLabel gender;
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

    private JLabel lbl11;
    JPanel searchPanel;
    private JLabel lbl9;
    private JLabel jobTitleLabel;

    public Search() {
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
    }

    private void performSearch() {
        String searchText = search.getText();
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(searchPanel, "Please enter the employee name or ID", "Error", JOptionPane.ERROR_MESSAGE);
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
                    JOptionPane.showMessageDialog(searchPanel, "Please enter the full name (first and last name)", "Error", JOptionPane.ERROR_MESSAGE);
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
                String jobId = resultSet.getString("jobId"); // Get the jobId of the employee
                String jobTitle = ""; // Initialize jobTitle
                // Retrieve jobTitle from jobRoles table using jobId
                String jobQuery = "SELECT jobTitle FROM jobRoles WHERE jobId = ?";
                PreparedStatement jobStatement = connection.prepareStatement(jobQuery);
                jobStatement.setString(1, jobId);
                ResultSet jobResultSet = jobStatement.executeQuery();
                if (jobResultSet.next()) {
                    jobTitle = jobResultSet.getString("jobTitle");
                }
                jobResultSet.close();
                jobStatement.close();

                ShowLabel();
                fname.setText(resultSet.getString("first_name"));
                lName.setText(resultSet.getString("last_name"));
                address.setText(resultSet.getString("address"));
                bDay.setText(resultSet.getString("birthday"));
                nic.setText(resultSet.getString("nic"));
                gender.setText(resultSet.getString("gender"));
                email.setText(resultSet.getString("email"));
                bankAccount.setText(resultSet.getString("bank_account"));
                phoneNo.setText(resultSet.getString("phoneNo"));
                jobTitleLabel.setText(jobTitle); // Update the job title label with the retrieved job title

            } else {
                JOptionPane.showMessageDialog(searchPanel, "Employee not found", "Error", JOptionPane.ERROR_MESSAGE);
            }

            resultSet.close();
            searchStatement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(searchPanel, "Error searching for employee: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        lbl8.setText("Phone No      :");
        lbl9.setText("Job Title      :");
        lbl11.setText("Bank Account :");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Search Employee Details");
        Search search = new Search();
        frame.setContentPane(search.searchPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
