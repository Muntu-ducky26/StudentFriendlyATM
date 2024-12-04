import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
class General {
   	protected String name;
	private int pin;
   	private double balance;
  	public General(String name,double balance,int pin){
       	this.name=name;
      	this.pin=pin;
		this.balance=balance;
}
public int getPin(){
        return pin;
}
public double getBalance(){
        return balance;
}
public void setBalance(double balance){
        this.balance = balance;
}
public boolean withdraw(int amount){
if (amount % 500 == 0 && balance >= amount){
	balance -= amount;
      System.out.println("Your current balance is " + balance);
      try{ updateFile();
           return true;
 	}
	catch(IOException e){
           System.out.println("Try again, Unknown Error Occured");
           return false;
}
} 
else{
JOptionPane.showMessageDialog(null, "Please insert a multiple of 500 or ensure sufficient balance.");
}
return false;}

public boolean deposit(int amount){
if (amount % 500 == 0 && amount >= 500){
	balance += amount;
   System.out.println("Your current balance is " + balance);
    try{ updateFile();
		  return true;}
	catch (IOException e){
         e.printStackTrace();
		  return false;}
        } 
else{
JOptionPane.showMessageDialog(null, "Please insert a multiple of 500 or ensure sufficient balance.");
}
return false;
}

public String getName(){
    return name;
}
public void updateFile() throws IOException {
    File originalFile = new File("Accounts.txt");
    File tempFile = new File("Accounts_temp.txt");
    BufferedReader reader = new BufferedReader(new FileReader(originalFile));
    BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
		String line;
        while((line = reader.readLine()) != null){
            String[] details =line.split(",");
            if (details[0].equals("General") && Integer.parseInt(details[3])==pin){
                line="General," + name + "," + balance + "," + pin;
            } 
			 else if(this instanceof Student && details[0].equals("Student") && Integer.parseInt(details[3]) == pin){
                line = "Student," + name + "," + balance + "," + pin + "," + ((Student) this).getPass();
            }
            writer.write(line);
            writer.newLine();
        }
        reader.close();
        writer.close();
        if (originalFile.delete()) {
            tempFile.renameTo(originalFile);
        }
	   else{
        	System.out.println("Could not update the file.");
}
}
}

class Student extends General {
    private int pass;
    public Student(String name, double balance, int pin, int pass) {
        super(name, balance, pin);
        this.pass = pass;
    }
public int getPass() {
     return pass;
 }
public boolean withdraw(int amount, int pass) {
        if(this.pass == pass){
            if (amount % 100 ==0 && getBalance()>= amount) {
                setBalance(getBalance() - amount);
                System.out.println("Your current balance is " + getBalance());
                try{
                    updateFile();
                } 
			 catch (IOException e) {
                    e.printStackTrace();
                }
            }
		 else{
                JOptionPane.showMessageDialog(null, "Please insert a multiple of 100 or ensure sufficient balance.");
            }
        }
	else{
            JOptionPane.showMessageDialog(null, "Invalid pass code!");
        }
return true;
}
public boolean deposit(int amount, int pass) {
    if(this.pass == pass) {
        if(amount % 100 == 0 && amount >= 100) {
            setBalance(getBalance()+amount);
              System.out.println("Your current balance is " + getBalance());
                try{
                    updateFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please insert a multiple of 100.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Invalid pass code!");
        }
        return true;
    }
}

public class ATMGUI extends JFrame{
    private JPasswordField pinField;
    private JTextArea display;
    private General currentGeneralAccount;
    private Student currentStudentAccount;
    public ATMGUI(){
        setTitle("Student Friendly ATM Machine NSTU");
        setSize(300,350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        display=new JTextArea();
        display.setEditable(false);
        add(new JScrollPane(display), BorderLayout.CENTER);
        display.setBackground(Color.LIGHT_GRAY);
        display.setForeground(Color.BLUE);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Enter PIN:"));
        panel.setBackground(Color.CYAN);
        pinField = new JPasswordField(10);
        panel.add(pinField);

        JButton loginButton=new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        panel.add(loginButton);

        add(panel, BorderLayout.NORTH);
    }

    private void handleLogin() {
        try {
            char[] passwordChars = pinField.getPassword();
            String password = new String(passwordChars);
            int pin = Integer.parseInt(password);
            java.util.Arrays.fill(passwordChars, '\0');
            currentGeneralAccount = loadGeneralAccount(pin);
            currentStudentAccount = loadStudentAccount(pin);

            if (currentGeneralAccount != null || currentStudentAccount != null) {
                showAccountSelection();
            } else {
                display.setText("Invalid PIN, please try again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            display.setText("Error loading account data.");
        }
    }

    private General loadGeneralAccount(int pin) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("Accounts.txt"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] details = line.split(",");
            if (details[0].equals("General") && Integer.parseInt(details[3]) == pin) {
                reader.close();
                return new General(details[1], Double.parseDouble(details[2]), pin);
            }
        }
        reader.close();
        return null;
    }

    private Student loadStudentAccount(int pin) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("Accounts.txt"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] details = line.split(",");
            if (details[0].equals("Student") && Integer.parseInt(details[3]) == pin) {
                reader.close();
                return new Student(details[1], Double.parseDouble(details[2]), pin, Integer.parseInt(details[4]));
            }
        }
        reader.close();
        return null;
    }

    private void showAccountSelection() {
        String[] options = {"General Account", "Student Account"};
        int choice = JOptionPane.showOptionDialog(this, "Select Account Type", "Account Selection",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0 && currentGeneralAccount != null) {
            showOperations(currentGeneralAccount);
        } else if (choice == 1 && currentStudentAccount != null) {
            int pass = Integer.parseInt(JOptionPane.showInputDialog("Enter pass code for Student Account:"));
            if (pass == currentStudentAccount.getPass()) {
                showOperations(currentStudentAccount,pass);
            } else {
                display.setText("Invalid pass code!");
            }
        } else {
            display.setText("Selected account type not found for this PIN.");
        }
    }

    private void showOperations(General account) {
        JPanel operationsPanel = new JPanel(new GridLayout(4, 1));
        JButton withdrawButton = new JButton("Withdraw");
        JButton depositButton = new JButton("Deposit");
        JButton balanceButton = new JButton("Check Balance");
        JButton exitButton = new JButton("Exit");

        withdrawButton.addActionListener(e -> handleWithdraw(account));
        depositButton.addActionListener(e -> handleDeposit(account));
        balanceButton.addActionListener(e -> display.setText("Current Balance: " + account.getBalance()));
        exitButton.addActionListener(e -> System.exit(0));

        operationsPanel.add(withdrawButton);
        operationsPanel.add(depositButton);
        operationsPanel.add(balanceButton);
        operationsPanel.add(exitButton);

        add(operationsPanel, BorderLayout.SOUTH);
        revalidate();
    }

    private void handleWithdraw(General account) {
        String input = JOptionPane.showInputDialog("Enter the amount to withdraw:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                int amount = Integer.parseInt(input.trim());
                account.withdraw(amount);
                display.setText("Withdrawn: " + amount + "\nCurrent Balance: " + account.getBalance());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            }
        } else {
            display.setText("Withdraw canceled or invalid input.");
        }
    }

    private void handleDeposit(General account) {
        String input = JOptionPane.showInputDialog("Enter the amount to deposit:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                int amount = Integer.parseInt(input.trim());
                account.deposit(amount);
                display.setText("Deposited: " + amount + "\nCurrent Balance: " + account.getBalance());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            }
        } else {
            display.setText("Deposit canceled or invalid input.");
        }
    }

private void showOperations(Student account,int pass) {
        JPanel operationsPanel = new JPanel(new GridLayout(4, 1));
        JButton withdrawButton = new JButton("Withdraw");
        JButton depositButton = new JButton("Deposit");
        JButton balanceButton = new JButton("Check Balance");
        JButton exitButton = new JButton("Exit");

        withdrawButton.addActionListener(e -> handleWithdraw(account,pass));
        depositButton.addActionListener(e -> handleDeposit(account,pass));
        balanceButton.addActionListener(e -> display.setText("Current Balance: " + account.getBalance()));
        exitButton.addActionListener(e -> System.exit(0));

        operationsPanel.add(withdrawButton);
        operationsPanel.add(depositButton);
        operationsPanel.add(balanceButton);
        operationsPanel.add(exitButton);

        add(operationsPanel,BorderLayout.SOUTH);
        revalidate();
    }

private void handleWithdraw(Student account,int pass){
        String input = JOptionPane.showInputDialog("Enter the amount to withdraw:");
        if (input != null && !input.trim().isEmpty()){
            try {
                int amount = Integer.parseInt(input.trim());
                account.withdraw(amount,pass);
                display.setText("Withdrawn: " + amount + "\nCurrent Balance: " + account.getBalance());
            } catch (NumberFormatException e){
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            }
        } else {
            display.setText("Withdraw canceled or invalid input.");
        }
 }
private void handleDeposit(Student account,int pass){
        String input = JOptionPane.showInputDialog("Enter the amount to deposit:");
        if (input != null && !input.trim().isEmpty()){
            try {
                int amount = Integer.parseInt(input.trim());
                account.deposit(amount,pass);
                display.setText("Deposited: " + amount + "\nCurrent Balance: " + account.getBalance());
            }
		catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,"Please enter a valid number.");
           }
}
else{
       display.setText("Deposit canceled or invalid input.");
 }
}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ATMGUI atmGui = new ATMGUI();
            atmGui.setVisible(true);
        });
    }
}
