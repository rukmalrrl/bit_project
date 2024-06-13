import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainDashboard extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JButton selectedButton;
    private Dashboard dashboardPanel; // Declare Dashboard panel instance

    public MainDashboard() {
        setTitle("Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Sidebar panel
        JPanel sidebar = new JPanel(new GridLayout(6, 1, 0, 10));
        sidebar.setBackground(new Color(34, 40, 44));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));

        // Sidebar buttons
        JButton dashboardButton = createSidebarButton("Dashboard");
        JButton employeeManagementButton = createSidebarButton("Employee Management");
        JButton attendanceButton = createSidebarButton("Attendance");
        JButton salaryButton = createSidebarButton("Salary");
        JButton calculatorButton = createSidebarButton("Calculator");
        JButton settingsButton = createSidebarButton("Settings");

        sidebar.add(dashboardButton);
        sidebar.add(employeeManagementButton);
        sidebar.add(attendanceButton);
        sidebar.add(salaryButton);
        sidebar.add(calculatorButton);
        sidebar.add(settingsButton);

        // Main content panel with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add content panels for each section
        dashboardPanel = new Dashboard(); // Initialize Dashboard panel instance
        mainPanel.add(dashboardPanel, "Dashboard");
        mainPanel.add(createEmployeeManagementPanel(), "Employee Management");
        mainPanel.add(createAttendancePanel(), "Attendance");
        mainPanel.add(createSalaryPanel(), "Salary");
        mainPanel.add(new Calculator(), "Calculator");
        mainPanel.add(createContentPanel("Settings Content"), "Settings");

        // Add action listeners to sidebar buttons
        dashboardButton.addActionListener(e -> {
            showCard("Dashboard");
            updateButtonSelection(dashboardButton);
            dashboardPanel.refreshData(); // Refresh Dashboard data when button is clicked
        });
        employeeManagementButton.addActionListener(e -> {
            showCard("Employee Management");
            updateButtonSelection(employeeManagementButton);
        });
        attendanceButton.addActionListener(e -> {
            showCard("Attendance");
            updateButtonSelection(attendanceButton);
        });
        salaryButton.addActionListener(e -> {
            showCard("Salary");
            updateButtonSelection(salaryButton);
        });
        calculatorButton.addActionListener(e -> {
            showCard("Calculator");
            updateButtonSelection(calculatorButton);
        });
        settingsButton.addActionListener(e -> {
            showCard("Settings");
            updateButtonSelection(settingsButton);
        });

        // Layout setup
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // Set the initial selected button
        updateButtonSelection(dashboardButton);
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(34, 40, 44));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setOpaque(true);

        // Add mouse listener for hover and click effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(new Color(60, 63, 65)); // Hover color
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(new Color(34, 40, 44)); // Original color
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(new Color(70, 75, 78)); // Click color
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(new Color(60, 63, 65)); // Revert to hover color after click
                }
            }
        });

        return button;
    }

    private JPanel createContentPanel(String text) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel(text));
        return panel;
    }

    private JPanel createDashboardPanel() {
        return new Dashboard(); // Return an instance of Dashboard
    }

    private JPanel createEmployeeManagementPanel() {
        JPanel employeeManagementPanel = new JPanel();
        employeeManagementPanel.setLayout(new GridLayout(2, 2, 10, 10));
        employeeManagementPanel.setBackground(Color.WHITE);

        JButton registerButton = new JButton("Register", ImageUtil.resizeImageIcon("src/images/register.png", 200, 200));
        registerButton.setBackground(new Color(250, 246, 228));
        registerButton.setFont(new Font("Arial", Font.BOLD, 30));
        JButton searchButton = new JButton("Search", ImageUtil.resizeImageIcon("src/images/search.png", 200, 200));
        searchButton.setBackground(new Color(250, 234, 224));
        searchButton.setFont(new Font("Arial", Font.BOLD, 30));
        JButton updateButton = new JButton("Update", ImageUtil.resizeImageIcon("src/images/update.png", 200, 200));
        updateButton.setBackground(new Color(250, 234, 224));
        updateButton.setFont(new Font("Arial", Font.BOLD, 30));
        JButton deleteButton = new JButton("Delete", ImageUtil.resizeImageIcon("src/images/delete.png", 200, 200));
        deleteButton.setBackground(new Color(250, 246, 228));
        deleteButton.setFont(new Font("Arial", Font.BOLD, 30));

        // Add action listeners to the buttons
        registerButton.addActionListener(e -> {
            Registration registration = new Registration();
            mainPanel.add(registration.registerPanel, "Register");
            cardLayout.show(mainPanel, "Register");
        });
        searchButton.addActionListener(e -> {
            Search search = new Search();
            mainPanel.add(search.searchPanel, "Search");
            cardLayout.show(mainPanel, "Search");
        });
        updateButton.addActionListener(e -> {
            Update update = new Update();
            mainPanel.add(update.updatePanel, "Update");
            cardLayout.show(mainPanel, "Update");
        });
        deleteButton.addActionListener(e -> {
            Delete delete = new Delete();
            mainPanel.add(delete.deletePanel, "Delete");
            cardLayout.show(mainPanel, "Delete");
        });

        // Add buttons to the panel
        employeeManagementPanel.add(registerButton);
        employeeManagementPanel.add(searchButton);
        employeeManagementPanel.add(updateButton);
        employeeManagementPanel.add(deleteButton);

        return employeeManagementPanel;
    }

    private JPanel createAttendancePanel() {
        JPanel attendancePanel = new JPanel();
        attendancePanel.setLayout(new GridLayout(2, 2, 10, 10));
        attendancePanel.setBackground(Color.WHITE);

        JButton markAttendanceButton = new JButton("Mark Attendance", ImageUtil.resizeImageIcon("src/images/markAttendance.png", 200, 200));
        markAttendanceButton.setBackground(new Color(250, 246, 228));
        markAttendanceButton.setFont(new Font("Arial", Font.BOLD, 30));
        JButton oneEmpButton = new JButton("One Employee", ImageUtil.resizeImageIcon("src/images/attendanceRepo1.png", 200, 200));
        oneEmpButton.setBackground(new Color(250, 234, 224));
        oneEmpButton.setFont(new Font("Arial", Font.BOLD, 30));
        JButton allEmpButton = new JButton("All Employees", ImageUtil.resizeImageIcon("src/images/attendanceRepo2.png", 200, 200));
        allEmpButton.setBackground(new Color(250, 234, 224));
        allEmpButton.setFont(new Font("Arial", Font.BOLD, 30));

        // Add action listeners to the buttons
        markAttendanceButton.addActionListener(e -> {
            MarkAttendance markAttendance = new MarkAttendance();
            mainPanel.add(markAttendance, "MarkAttendance");
            cardLayout.show(mainPanel, "MarkAttendance");
        });
        oneEmpButton.addActionListener(e -> {
            AttendanceOneEmp attendanceOneEmp = new AttendanceOneEmp();
            mainPanel.add(attendanceOneEmp, "AttendanceOneEmp");
            cardLayout.show(mainPanel, "AttendanceOneEmp");
        });
        allEmpButton.addActionListener(e -> {
            AttendanceAllEmp attendanceAllEmp = new AttendanceAllEmp();
            mainPanel.add(attendanceAllEmp, "AttendanceAllEmp");
            cardLayout.show(mainPanel, "AttendanceAllEmp");
        });

        // Add buttons to the panel
        attendancePanel.add(markAttendanceButton);
        attendancePanel.add(oneEmpButton);
        attendancePanel.add(allEmpButton);

        return attendancePanel;
    }

    private JPanel createSalaryPanel() {
        JPanel salaryPanel = new JPanel();
        salaryPanel.setLayout(new GridLayout(2, 2, 10, 10));
        salaryPanel.setBackground(Color.WHITE);

        JButton salaryButton = new JButton("Salary Report");
        JButton jobRolesButton = new JButton("Job Role Details");

        // Add action listeners to the buttons
        salaryButton.addActionListener(e -> {
            SalaryDetailsUpdater salaryDetailsUpdater = new SalaryDetailsUpdater();
            mainPanel.add(salaryDetailsUpdater, "SalaryDetailsUpdater");
            cardLayout.show(mainPanel, "SalaryDetailsUpdater");
        });
        jobRolesButton.addActionListener(e -> {
            JobRoles jobRoles = new JobRoles();
            mainPanel.add(jobRoles, "JobRoles");
            cardLayout.show(mainPanel, "JobRoles");
        });

        // Add buttons to the panel
        salaryPanel.add(salaryButton);
        salaryPanel.add(jobRolesButton);

        return salaryPanel;
    }

    private void showCard(String card) {
        cardLayout.show(mainPanel, card);
    }

    private void updateButtonSelection(JButton button) {
        if (selectedButton != null) {
            selectedButton.setBackground(new Color(34, 40, 44)); // Revert color of previous button
        }
        button.setBackground(new Color(60, 63, 65)); // Selected button color
        selectedButton = button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}
