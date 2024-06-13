import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Dashboard extends JPanel {
    private JLabel employeeLabel;
    private JLabel attendanceLabel;
    private JPanel upperLeftPanel;
    private JPanel upperRightPanel;
    private JPanel lowerLeftPanel;
    private JPanel lowerRightPanel;

    public Dashboard() {
        setLayout(new GridLayout(2, 2, 10, 10));
        setBackground(Color.WHITE);

        // Upper left square for Employee Details Card
        upperLeftPanel = new JPanel(new BorderLayout());
        upperLeftPanel.setBackground(new Color(250, 246, 228));
        add(upperLeftPanel);

        // Upper right square for Attendance Details Card
        upperRightPanel = new JPanel(new BorderLayout());
        upperRightPanel.setBackground(new Color(250, 234, 224));
        add(upperRightPanel);

        // Lower left square for Job Title Counts
        lowerLeftPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        lowerLeftPanel.setBackground(new Color(250, 234, 224));
        add(lowerLeftPanel);

        // Lower right square for Job Title Counts
        lowerRightPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        lowerRightPanel.setBackground(new Color(250, 246, 228));
        add(lowerRightPanel);

        // Initialize labels for upper panels
        employeeLabel = new JLabel("Loading...");
        employeeLabel.setFont(new Font("Bodoni MT", Font.BOLD, 40));
        employeeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        upperLeftPanel.add(employeeLabel, BorderLayout.CENTER);

        attendanceLabel = new JLabel("Loading...");
        attendanceLabel.setFont(new Font("Bodoni MT", Font.BOLD, 40));
        attendanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        upperRightPanel.add(attendanceLabel, BorderLayout.CENTER);

        // Fetch initial data
        refreshData();
    }

    public void refreshData() {
        fetchEmployeeCount();
        fetchAttendanceCount();
        fetchJobTitleCounts();
    }

    private void fetchEmployeeCount() {
        String query = "SELECT COUNT(DISTINCT employee_id) AS total_employees FROM employeeDetails";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitApp", "root", "root");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int totalEmployees = rs.getInt("total_employees");
                updateEmployeeCount(totalEmployees);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchAttendanceCount() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = sdf.format(new Date());

        String query = "SELECT COUNT(*) AS today_attendance FROM attendance WHERE aDate = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitApp", "root", "root");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, todayDate);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int todayAttendance = rs.getInt("today_attendance");
                updateAttendanceCount(todayAttendance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchJobTitleCounts() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = sdf.format(new Date());

        // Map to store job title counts
        Map<String, Integer> jobTitleCountsLeft = new HashMap<>();
        Map<String, Integer> jobTitleCountsRight = new HashMap<>();

        // Initialize counts to 0 for job titles
        jobTitleCountsLeft.put("Accountant", 0);
        jobTitleCountsLeft.put("Manager", 0);
        jobTitleCountsLeft.put("Clerk", 0);
        jobTitleCountsLeft.put("Supervisor", 0);
        jobTitleCountsLeft.put("Technician", 0);

        String query = "SELECT j.jobTitle, COUNT(*) AS count " +
                "FROM attendance a " +
                "INNER JOIN employeeDetails e ON a.employee_id = e.employee_id " +
                "INNER JOIN jobRoles j ON e.jobId = j.jobId " +
                "WHERE a.aDate = ? AND j.jobTitle IN ('Accountant', 'Manager', 'Clerk', 'Supervisor', 'Technician', 'labor', 'Engineer') " +
                "GROUP BY j.jobTitle";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitApp", "root", "root");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, todayDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String jobTitle = rs.getString("jobTitle");
                int count = rs.getInt("count");
                if (jobTitle.equals("labor")) {
                    jobTitleCountsLeft.put(jobTitle, count);
                } else if (jobTitle.equals("Engineer")) {
                    jobTitleCountsLeft.put(jobTitle, count);
                } else {
                    jobTitleCountsLeft.put(jobTitle, count);
                }
            }

            updateJobTitleCounts(jobTitleCountsLeft, jobTitleCountsRight);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateJobTitleCounts(Map<String, Integer> jobTitleCountsLeft, Map<String, Integer> jobTitleCountsRight) {
        lowerLeftPanel.removeAll(); // Clear existing content
        lowerRightPanel.removeAll();

        // Create job title count labels for lower panels
        for (Map.Entry<String, Integer> entry : jobTitleCountsLeft.entrySet()) {
            String jobTitle = entry.getKey();
            int count = entry.getValue();

            JLabel titleLabel = new JLabel(jobTitle + ": " + count);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            lowerLeftPanel.add(titleLabel);
        }

        for (Map.Entry<String, Integer> entry : jobTitleCountsRight.entrySet()) {
            String jobTitle = entry.getKey();
            int count = entry.getValue();

            JLabel titleLabel = new JLabel(jobTitle + ": " + count);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            lowerRightPanel.add(titleLabel);
        }

        revalidate(); // Refresh layout
        repaint(); // Repaint to show updated content
    }

    public void updateEmployeeCount(int count) {
        employeeLabel.setText("Total Employees: " + count);
    }

    public void updateAttendanceCount(int count) {
        attendanceLabel.setText("Today Attendance : " + count);
    }

    // Testing the Dashboard class
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dashboard Test");
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            Dashboard dashboard = new Dashboard();
            frame.add(dashboard);

            frame.setVisible(true);
        });
    }
}
