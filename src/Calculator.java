import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

class Calculator extends JPanel implements ActionListener {
    private JTextField textField;
    private JButton[] numberButtons;
    private JButton[] functionButtons;
    private String[] functionNames = {
            "C", "DEL", "=", "+", "-", "*", "/", ".", "sqrt", "sin", "cos", "tan", "(", ")", "pi", "E"
    };

    public Calculator() {
        setLayout(new BorderLayout());

        // Create and configure text field
        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.BOLD, 24));
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.setEditable(false);
        add(textField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 4, 10, 10));
        add(buttonPanel, BorderLayout.CENTER);

        // Initialize number buttons
        numberButtons = new JButton[10];
        for (int i = 0; i < 10; i++) {
            numberButtons[i] = createButton(String.valueOf(i), new Color(173, 216, 230)); // light pleasant blue
        }

        // Initialize function buttons
        functionButtons = new JButton[functionNames.length];
        for (int i = 0; i < functionNames.length; i++) {
            functionButtons[i] = createButton(functionNames[i], new Color(173, 216, 230)); // light pleasant blue
        }

        // Add buttons to panel
        buttonPanel.add(numberButtons[7]);
        buttonPanel.add(numberButtons[8]);
        buttonPanel.add(numberButtons[9]);
        buttonPanel.add(functionButtons[6]); // /

        buttonPanel.add(numberButtons[4]);
        buttonPanel.add(numberButtons[5]);
        buttonPanel.add(numberButtons[6]);
        buttonPanel.add(functionButtons[5]); // *

        buttonPanel.add(numberButtons[1]);
        buttonPanel.add(numberButtons[2]);
        buttonPanel.add(numberButtons[3]);
        buttonPanel.add(functionButtons[4]); // -

        buttonPanel.add(numberButtons[0]);
        buttonPanel.add(functionButtons[7]); // .
        buttonPanel.add(functionButtons[2]); // =
        buttonPanel.add(functionButtons[3]); // +

        buttonPanel.add(functionButtons[12]); // (
        buttonPanel.add(functionButtons[13]); // )
        buttonPanel.add(functionButtons[0]); // C
        buttonPanel.add(functionButtons[1]); // DEL

        buttonPanel.add(functionButtons[9]); // sin
        buttonPanel.add(functionButtons[10]); // cos
        buttonPanel.add(functionButtons[11]); // tan
        buttonPanel.add(functionButtons[8]); // sqrt
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        button.setBackground(color);
        button.addActionListener(this);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < 10; i++) {
            if (e.getSource() == numberButtons[i]) {
                textField.setText(textField.getText() + i);
            }
        }

        for (int i = 0; i < functionNames.length; i++) {
            if (e.getSource() == functionButtons[i]) {
                switch (functionNames[i]) {
                    case "=":
                        try {
                            String expression = textField.getText();
                            double result = evaluateExpression(expression);
                            textField.setText(String.valueOf(result));
                        } catch (Exception ex) {
                            textField.setText("Error");
                        }
                        break;
                    case "C":
                        textField.setText("");
                        break;
                    case "DEL":
                        String currentText = textField.getText();
                        if (!currentText.isEmpty()) {
                            textField.setText(currentText.substring(0, currentText.length() - 1));
                        }
                        break;
                    default:
                        textField.setText(textField.getText() + " " + functionNames[i] + " ");
                        break;
                }
            }
        }
    }

    private double evaluateExpression(String expression) {
        String[] tokens = expression.split(" ");
        Stack<Double> values = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (token.matches("-?\\d+(\\.\\d+)?")) {
                values.push(Double.parseDouble(token));
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.peek().equals("(")) {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop();
            } else if (isOperator(token)) {
                while (!operators.isEmpty() && hasPrecedence(token, operators.peek())) {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(token);
            } else if (isFunction(token)) {
                values.push(applyFunction(token, values.pop()));
            }
        }

        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("^");
    }

    private boolean isFunction(String token) {
        return token.equals("sqrt") || token.equals("sin") || token.equals("cos") || token.equals("tan") || token.equals("pi") || token.equals("E");
    }

    private boolean hasPrecedence(String op1, String op2) {
        if (op2.equals("(") || op2.equals(")")) {
            return false;
        }
        if ((op1.equals("*") || op1.equals("/") || op1.equals("^")) && (op2.equals("+") || op2.equals("-"))) {
            return false;
        } else {
            return true;
        }
    }

    private double applyOperator(String op, double b, double a) {
        switch (op) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) {
                    throw new UnsupportedOperationException("Cannot divide by zero");
                }
                return a / b;
            case "^":
                return Math.pow(a, b);
        }
        return 0;
    }

    private double applyFunction(String func, double value) {
        switch (func) {
            case "sqrt":
                return Math.sqrt(value);
            case "sin":
                return Math.sin(Math.toRadians(value));
            case "cos":
                return Math.cos(Math.toRadians(value));
            case "tan":
                return Math.tan(Math.toRadians(value));
            case "pi":
                return Math.PI;
            case "E":
                return Math.E;
        }
        return 0;
    }
}
