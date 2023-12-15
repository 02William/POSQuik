package LadetechHoldings_System;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.awt.event.KeyEvent;
import java.io.FileReader;
import static java.lang.Double.parseDouble;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author WILLIAM OMOLADE
 */
public class Main_menu extends javax.swing.JFrame {
    
    // Used for database query statements
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    private Point mouseOffset; // variable used for postioning Jframe window on screen
    
    POS_source pss = new POS_source(); // Called early in the program to load data from database - might need to put it in the login form when login to complete to avoid error from startup with no database connection
    login log = new login(); // calls/initiates the login form to the screen
    
    String user_type = "";
    
    // data from variables used when generating pdf
    static String cusName = "", cusTel = "", cusVatNumber = "";
    
    // data from variables used when generating pdf
    static String posTotal = "", posDiscount = "", posNoVatTotal = ""; 
    
    //Company data - company vat number, tel, email, P.O Box, address, name, bank, account number, branch, etc
    static String companyVatNo = "", tel = "", email = "", poBox = "", address = "", companyName = "", 
                  bank = "", accountNo = "", branch = "";
    static int invoiceNo = 0, quotationNo = 0;
    static double countryTax = 0.14;
    
    boolean saved; // Variable used to track if user has saved current POS sales list
    

    /** Creates new form Main_Menu
     * @throws java.io.IOException */
    public Main_menu() throws IOException {
        this.saved = true;
        initComponents();
        setDate();
        setTime();
        inventoryList();
        userList();
        load_companyData();
        setCompanyInfo();
        
        loggedIn.setText(login.user_name); // Setting name of logged in user to the POS screen
        settings_id.setText(login.user_id); // Setting user ID of logged in user in settings screen
        settings_usrName.setText(login.user_name); // Setting username of logged in user in settings screen
        user_type = login.user_type; // Setting user priviledge status
    }
    
    
    // Log file message processing class
    public static class MyLogger {
        
        private final Logger LOGGER = Logger.getLogger(MyLogger.class.getName());
        
        public void log(String message) throws IOException {
            
            String desktopPath  = System.getProperty("user.home") + "/Desktop"; // Getting path to desktop
            String logPath = desktopPath + "\\POS Log\\POS_Sys.log"; // Path of log file
            
            FileHandler handler = new FileHandler(logPath, true); // appends new log messages to "logPath" with boolean value "true"
            LOGGER.addHandler(handler);
            LOGGER.info(message);
        }
    }
    MyLogger logMessage = new MyLogger(); // Used to access MyLogger class
    
    
    
    // Setting date 
    public final void setDate() {
        Date d = new Date();
        SimpleDateFormat s = new SimpleDateFormat("EEE, MMM d, yyyy");
        home_date.setText(s.format(d));
        pos_date.setText(s.format(d));
    }
    
    // Setting time
    public final void setTime() {

        new Timer(0, (ActionEvent e) -> {
            Date d = new Date();
            SimpleDateFormat s = new SimpleDateFormat("hh:mm:ss a"); // ("HH:mm:ss") - for 24hr clock
            home_time.setText(" " + s.format(d));
            pos_time.setText(" " + s.format(d));
        }).start();
    }
    
    // Populatinng inventory list with data from database
    private void inventoryList() throws IOException{
        
        try {
            
            String query = "Select product_name from inventory_data";
            pst = POS_source.mycon().prepareStatement(query);
            rs = pst.executeQuery();
        
            while (rs.next()) {     
                select_prod.addItem(rs.getString("product_name"));     
            }
            
        } catch (SQLException e){
            System.out.println(e.getMessage());
            logMessage.log("SQLException: " + e.getMessage());
        }        
    }
    
    // Populatinng user list with data from database
    private void userList() throws IOException{
        
        try {
            
            String query = ("Select user_name from users");
            pst = POS_source.mycon().prepareStatement(query);
            rs = pst.executeQuery();
        
            while (rs.next()) {     
                select_user.addItem(rs.getString("user_name"));     
            }
            
        } catch (SQLException e){
            System.out.println(e.getMessage());
            logMessage.log("SQLException: " + e.getMessage());
        }        
    }
    
    
    // Calculation with VAT added
    public void total_purchase() {

        int numofrow = posTable.getRowCount();

        double total = 0;
        int i;

        for (i = 0; i < numofrow; i++) {

            double value = Double.parseDouble(posTable.getValueAt(i, 5).toString());
            total += value;
        }

        total = total + (total * countryTax); // Adding tax to total ammount
        String price = String.format("%.2f", total);
        totalPrice.setText(price);
        
    }
    
    // Calculation with no VAT added
    public void noVAT_purchase() {

        int numofrow = posTable.getRowCount();

        double total = 0;
        int i;

        for (i = 0; i < numofrow; i++) {

            double value = Double.parseDouble(posTable.getValueAt(i, 5).toString());
            total += value;
        }

        String price = String.format("%.2f", total);
        posNoVatTotal = price; 
        
    }
        
    public static JTable getJTable() {
    return posTable; // Assuming that posTable is the JTable object in your Main_menu class
}
    
    public static DefaultTableModel getTableModel() {
        JTable table = Main_menu.getJTable(); // Get the JTable object from your Main_menu class
    return (DefaultTableModel) table.getModel(); // Cast the table model to DefaultTableModel and return it
}
    //Collecting company data from database - which can only be modiified by the administrator 
    private void load_companyData() throws IOException{
        
        try {
                String query = ("SELECT * FROM `company_data`");
                pst = POS_source.mycon().prepareStatement(query);
                rs = pst.executeQuery();

                if (rs.next()) {

                    //Getting company data from the database
                    companyVatNo = (rs.getString("companyVatNo"));
                    tel = (rs.getString("tel"));
                    email = (rs.getString("email")); 
                    poBox = (rs.getString("poBox"));
                    address = (rs.getString("address")); 
                    companyName = (rs.getString("name"));
                    countryTax = Double.parseDouble(rs.getString("countryTax"));
                    bank = (rs.getString("bank")); 
                    accountNo = (rs.getString("accountNo"));
                    branch = (rs.getString("branch"));
                    invoiceNo = Integer.parseInt((rs.getString("invoiceNo")));
                    quotationNo = Integer.parseInt(rs.getString("quotationNo"));
                    
                }

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                logMessage.log("SQLException: " + ex.getMessage());
            }
    }
    
    // Setting data in company information section in settings
    private void setCompanyInfo() {
        
        Double tax = (countryTax * 100);
        
        companyName_field.setText(companyName);
        companyAddress_field.setText(address);
        companyPO_field.setText(poBox);
        companyVat_field.setText(companyVatNo);
        companyTel_field.setText(tel);
        companyEmail_field.setText(email);
        taxRate_field.setText(String.format("%.2f", tax));
//        String.format("%.2f", total);
        bankName_field.setText(bank);
        accountNumber_field.setText(accountNo);
        bankBranch_field.setText(branch);
        
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel48 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        navigationBar = new javax.swing.JPanel();
        home = new javax.swing.JLabel();
        pos = new javax.swing.JLabel();
        inventory = new javax.swing.JLabel();
        tools = new javax.swing.JLabel();
        settings = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        home_screen = new javax.swing.JPanel();
        home_date = new javax.swing.JLabel();
        home_time = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        pos_panel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        inventory_panel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        tools_panel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        settings_panel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        pos_screen = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        posTable = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        prodID_search = new javax.swing.JTextField();
        qty = new javax.swing.JSpinner();
        jPanel19 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        avail_stocks = new javax.swing.JLabel();
        select_prod = new javax.swing.JComboBox<>();
        jPanel24 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        totalPrice = new javax.swing.JLabel();
        vat = new javax.swing.JCheckBox();
        jLabel34 = new javax.swing.JLabel();
        jPanel21 = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        change = new javax.swing.JLabel();
        paid = new javax.swing.JTextField();
        discount = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jPanel32 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jPanel33 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel35 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jPanel36 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jPanel30 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        pos_time = new javax.swing.JLabel();
        pos_date = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jPanel23 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        loggedIn = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        customerVatNumber = new javax.swing.JTextField();
        customerTel = new javax.swing.JTextField();
        customerName = new javax.swing.JTextField();
        jSeparator4 = new javax.swing.JSeparator();
        unit_price = new javax.swing.JLabel();
        avail_quantity = new javax.swing.JLabel();
        pos_prodID = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        stock_status = new javax.swing.JLabel();
        inventory_screen = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        logo5 = new javax.swing.JLabel();
        logo6 = new javax.swing.JLabel();
        search_inventory = new javax.swing.JTextField();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        inventory_table = new javax.swing.JTable();
        jPanel15 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        product_id = new javax.swing.JTextField();
        product_name = new javax.swing.JTextField();
        stock_level = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        product_price = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        product_descr = new javax.swing.JTextArea();
        jPanel16 = new javax.swing.JPanel();
        showAll_inventory = new javax.swing.JLabel();
        update_inventory = new javax.swing.JLabel();
        delete_inventory = new javax.swing.JLabel();
        add_inventory = new javax.swing.JLabel();
        refresh_inventory = new javax.swing.JLabel();
        tools_screen = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        logo3 = new javax.swing.JLabel();
        logo4 = new javax.swing.JLabel();
        search_tools = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tools_table = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        tool_id = new javax.swing.JTextField();
        tool_name = new javax.swing.JTextField();
        tool_quantity = new javax.swing.JTextField();
        tool_possession = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tool_details = new javax.swing.JTextArea();
        jPanel11 = new javax.swing.JPanel();
        show_tools = new javax.swing.JLabel();
        update_tools = new javax.swing.JLabel();
        delete_tools = new javax.swing.JLabel();
        add_tools = new javax.swing.JLabel();
        refresh_tools = new javax.swing.JLabel();
        settings_screen = new javax.swing.JPanel();
        general_settings = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        settings_id = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        settings_usrName = new javax.swing.JTextField();
        jPanel27 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jPanel25 = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        admin_settings = new javax.swing.JPanel();
        jLabel49 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        select_user = new javax.swing.JComboBox<>();
        userID_search = new javax.swing.JTextField();
        jPanel28 = new javax.swing.JPanel();
        jLabel73 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        userID = new javax.swing.JTextField();
        jLabel58 = new javax.swing.JLabel();
        userName = new javax.swing.JTextField();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        userType = new javax.swing.JComboBox<>();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jPanel31 = new javax.swing.JPanel();
        jLabel70 = new javax.swing.JLabel();
        jPanel34 = new javax.swing.JPanel();
        jLabel69 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        jLabel52 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        companyName_field = new javax.swing.JTextField();
        companyAddress_field = new javax.swing.JTextField();
        companyPO_field = new javax.swing.JTextField();
        companyVat_field = new javax.swing.JTextField();
        companyTel_field = new javax.swing.JTextField();
        companyEmail_field = new javax.swing.JTextField();
        jPanel22 = new javax.swing.JPanel();
        jLabel71 = new javax.swing.JLabel();
        jPanel29 = new javax.swing.JPanel();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        taxRate_field = new javax.swing.JTextField();
        jLabel76 = new javax.swing.JLabel();
        bankName_field = new javax.swing.JTextField();
        jLabel77 = new javax.swing.JLabel();
        accountNumber_field = new javax.swing.JTextField();
        jLabel78 = new javax.swing.JLabel();
        bankBranch_field = new javax.swing.JTextField();
        userPass = new javax.swing.JPasswordField();
        jLabel72 = new javax.swing.JLabel();
        jPanel37 = new javax.swing.JPanel();
        jLabel79 = new javax.swing.JLabel();
        jPanel26 = new javax.swing.JPanel();
        minimize = new javax.swing.JLabel();
        close = new javax.swing.JLabel();

        jLabel48.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel48.setText("Customer Number:");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(0, 102, 102));
        jPanel1.setMaximumSize(new java.awt.Dimension(1100, 650));
        jPanel1.setMinimumSize(new java.awt.Dimension(110000, 65000));
        jPanel1.setPreferredSize(new java.awt.Dimension(1220, 610));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        navigationBar.setBackground(new java.awt.Color(51, 51, 51));

        home.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        home.setForeground(new java.awt.Color(255, 255, 255));
        home.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/home.png"))); // NOI18N
        home.setText("Home");
        home.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        home.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                homeMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                homeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                homeMouseExited(evt);
            }
        });

        pos.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        pos.setForeground(new java.awt.Color(255, 255, 255));
        pos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/pos_small.png"))); // NOI18N
        pos.setText("Point of Sale");
        pos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                posMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                posMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                posMouseExited(evt);
            }
        });

        inventory.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        inventory.setForeground(new java.awt.Color(255, 255, 255));
        inventory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/inventory_small.png"))); // NOI18N
        inventory.setText("Inventory");
        inventory.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        inventory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                inventoryMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                inventoryMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                inventoryMouseExited(evt);
            }
        });

        tools.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        tools.setForeground(new java.awt.Color(255, 255, 255));
        tools.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/tool_small.png"))); // NOI18N
        tools.setText("Tools & Machines");
        tools.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tools.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                toolsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                toolsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                toolsMouseExited(evt);
            }
        });

        settings.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        settings.setForeground(new java.awt.Color(255, 255, 255));
        settings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/settings_small.png"))); // NOI18N
        settings.setText("Settings");
        settings.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        settings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                settingsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                settingsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                settingsMouseExited(evt);
            }
        });

        javax.swing.GroupLayout navigationBarLayout = new javax.swing.GroupLayout(navigationBar);
        navigationBar.setLayout(navigationBarLayout);
        navigationBarLayout.setHorizontalGroup(
            navigationBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(navigationBarLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(home)
                .addGap(149, 149, 149)
                .addComponent(pos)
                .addGap(144, 144, 144)
                .addComponent(inventory)
                .addGap(183, 183, 183)
                .addComponent(tools)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 149, Short.MAX_VALUE)
                .addComponent(settings)
                .addGap(26, 26, 26))
        );
        navigationBarLayout.setVerticalGroup(
            navigationBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, navigationBarLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(navigationBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(home)
                    .addComponent(pos)))
            .addGroup(navigationBarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(navigationBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tools)
                    .addComponent(inventory)
                    .addComponent(settings)))
        );

        jPanel1.add(navigationBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 1150, -1));

        jPanel3.setLayout(new java.awt.CardLayout());

        home_screen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                home_screenMouseEntered(evt);
            }
        });

        home_date.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        home_date.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/calendar.png"))); // NOI18N
        home_date.setText("99-99-9999");

        home_time.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        home_time.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/clock.png"))); // NOI18N
        home_time.setText("99:99:99 PM");

        jLabel7.setFont(new java.awt.Font("Segoe UI Semibold", 2, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(51, 51, 51));
        jLabel7.setText("Point of Sale & Inventory System");

        jLabel1.setFont(new java.awt.Font("Nirmala UI", 0, 11)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/phone.png"))); // NOI18N
        jLabel1.setText("POS-LADETECH@gmail.com");

        pos_panel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pos_panel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pos_panel.setPreferredSize(new java.awt.Dimension(314, 153));
        pos_panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pos_panelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pos_panelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pos_panelMouseExited(evt);
            }
        });

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/pos_big.png"))); // NOI18N

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel11.setText("Point of Sale System");

        javax.swing.GroupLayout pos_panelLayout = new javax.swing.GroupLayout(pos_panel);
        pos_panel.setLayout(pos_panelLayout);
        pos_panelLayout.setHorizontalGroup(
            pos_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pos_panelLayout.createSequentialGroup()
                .addGroup(pos_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pos_panelLayout.createSequentialGroup()
                        .addGap(168, 168, 168)
                        .addComponent(jLabel8))
                    .addGroup(pos_panelLayout.createSequentialGroup()
                        .addGap(127, 127, 127)
                        .addComponent(jLabel11)))
                .addContainerGap(136, Short.MAX_VALUE))
        );
        pos_panelLayout.setVerticalGroup(
            pos_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pos_panelLayout.createSequentialGroup()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(jLabel11))
        );

        inventory_panel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        inventory_panel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        inventory_panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                inventory_panelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                inventory_panelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                inventory_panelMouseExited(evt);
            }
        });

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/inventory_big.png"))); // NOI18N

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel12.setText(" Inventory System");

        javax.swing.GroupLayout inventory_panelLayout = new javax.swing.GroupLayout(inventory_panel);
        inventory_panel.setLayout(inventory_panelLayout);
        inventory_panelLayout.setHorizontalGroup(
            inventory_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inventory_panelLayout.createSequentialGroup()
                .addContainerGap(146, Short.MAX_VALUE)
                .addGroup(inventory_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inventory_panelLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(151, 151, 151))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inventory_panelLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(135, 135, 135))))
        );
        inventory_panelLayout.setVerticalGroup(
            inventory_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inventory_panelLayout.createSequentialGroup()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(jLabel12))
        );

        tools_panel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        tools_panel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tools_panel.setPreferredSize(new java.awt.Dimension(314, 153));
        tools_panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tools_panelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tools_panelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tools_panelMouseExited(evt);
            }
        });

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/tool_big.png"))); // NOI18N

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel13.setText("Tools & Machines");

        javax.swing.GroupLayout tools_panelLayout = new javax.swing.GroupLayout(tools_panel);
        tools_panel.setLayout(tools_panelLayout);
        tools_panelLayout.setHorizontalGroup(
            tools_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tools_panelLayout.createSequentialGroup()
                .addGap(150, 150, 150)
                .addComponent(jLabel9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tools_panelLayout.createSequentialGroup()
                .addContainerGap(134, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addGap(155, 155, 155))
        );
        tools_panelLayout.setVerticalGroup(
            tools_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tools_panelLayout.createSequentialGroup()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addContainerGap())
        );

        settings_panel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        settings_panel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        settings_panel.setPreferredSize(new java.awt.Dimension(314, 153));
        settings_panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                settings_panelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                settings_panelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                settings_panelMouseExited(evt);
            }
        });

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/setting-lines.png"))); // NOI18N

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel14.setText("Settings");

        javax.swing.GroupLayout settings_panelLayout = new javax.swing.GroupLayout(settings_panel);
        settings_panel.setLayout(settings_panelLayout);
        settings_panelLayout.setHorizontalGroup(
            settings_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settings_panelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(settings_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settings_panelLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(152, 152, 152))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settings_panelLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(184, 184, 184))))
        );
        settings_panelLayout.setVerticalGroup(
            settings_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settings_panelLayout.createSequentialGroup()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(jLabel14)
                .addContainerGap())
        );

        javax.swing.GroupLayout home_screenLayout = new javax.swing.GroupLayout(home_screen);
        home_screen.setLayout(home_screenLayout);
        home_screenLayout.setHorizontalGroup(
            home_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_screenLayout.createSequentialGroup()
                .addGroup(home_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(home_screenLayout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(jLabel7))
                    .addGroup(home_screenLayout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(jLabel1))
                    .addGroup(home_screenLayout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addGroup(home_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tools_panel, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                            .addComponent(pos_panel, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE))
                        .addGap(165, 165, 165)
                        .addGroup(home_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(inventory_panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(settings_panel, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE))))
                .addGap(254, 254, 254))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, home_screenLayout.createSequentialGroup()
                .addComponent(home_date)
                .addGap(47, 47, 47)
                .addComponent(home_time)
                .addGap(240, 240, 240))
        );
        home_screenLayout.setVerticalGroup(
            home_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_screenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(home_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(home_date)
                    .addComponent(home_time))
                .addGap(23, 23, 23)
                .addComponent(jLabel7)
                .addGap(23, 23, 23)
                .addGroup(home_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(inventory_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pos_panel, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(home_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(home_screenLayout.createSequentialGroup()
                        .addComponent(settings_panel, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1))
                    .addComponent(tools_panel, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel3.add(home_screen, "card2");

        pos_screen.setBackground(new java.awt.Color(204, 204, 204));
        pos_screen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pos_screenMouseEntered(evt);
            }
        });
        pos_screen.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));

        posTable.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        posTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product ID", "Product Name", "Purchase Quantity", "Unit Price", "Tax Amount", "Sub Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        posTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        posTable.setDoubleBuffered(true);
        posTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                posTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(posTable);
        if (posTable.getColumnModel().getColumnCount() > 0) {
            posTable.getColumnModel().getColumn(0).setResizable(false);
            posTable.getColumnModel().getColumn(1).setResizable(false);
            posTable.getColumnModel().getColumn(2).setResizable(false);
            posTable.getColumnModel().getColumn(3).setResizable(false);
            posTable.getColumnModel().getColumn(4).setResizable(false);
            posTable.getColumnModel().getColumn(5).setResizable(false);
        }

        jPanel6.setBackground(new java.awt.Color(51, 51, 51));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel29.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setText("Available Stocks:");
        jPanel6.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, -1, -1));

        jLabel31.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setText("Select Product:");
        jPanel6.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, -1, -1));

        jLabel32.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(255, 255, 255));
        jLabel32.setText("Purchase Quantity:");
        jPanel6.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 10, -1, -1));

        jLabel30.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setText("Product ID:");
        jPanel6.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 70, -1, -1));

        prodID_search.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        prodID_search.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                prodID_searchKeyPressed(evt);
            }
        });
        jPanel6.add(prodID_search, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 70, 150, 25));

        qty.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        qty.setModel(new javax.swing.SpinnerNumberModel(1.0d, 1.0d, null, 1.0d));
        jPanel6.add(qty, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 10, 150, -1));

        jLabel3.setBackground(new java.awt.Color(204, 204, 204));
        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Add to Cart");
        jLabel3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel3.setFocusable(false);
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel3MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel3MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
        );

        jPanel6.add(jPanel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 60, -1, -1));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/cart.png"))); // NOI18N
        jPanel6.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 70, -1, -1));

        avail_stocks.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        avail_stocks.setForeground(new java.awt.Color(255, 255, 255));
        avail_stocks.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        avail_stocks.setText("0.0");
        jPanel6.add(avail_stocks, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 10, 157, -1));

        select_prod.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        select_prod.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select a Product" }));
        select_prod.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        select_prod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_prodActionPerformed(evt);
            }
        });
        jPanel6.add(select_prod, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 40, 230, -1));

        jPanel24.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel33.setText("Search");
        jLabel33.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel33.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel33MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel33MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel33MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel6.add(jPanel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 70, -1, 25));

        jLabel53.setForeground(new java.awt.Color(255, 255, 255));
        jLabel53.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/import.png"))); // NOI18N
        jLabel53.setText("import");
        jLabel53.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel53.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel53MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel53MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel53MouseExited(evt);
            }
        });
        jPanel6.add(jLabel53, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 10, -1, -1));

        jLabel80.setForeground(new java.awt.Color(255, 255, 255));
        jLabel80.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/save.png"))); // NOI18N
        jLabel80.setText("save");
        jLabel80.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel80.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel80MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel80MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel80MouseExited(evt);
            }
        });
        jPanel6.add(jLabel80, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 60, -1, -1));

        jPanel17.setBackground(new java.awt.Color(51, 51, 51));
        jPanel17.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel20.setBackground(new java.awt.Color(51, 51, 51));
        jPanel20.setPreferredSize(new java.awt.Dimension(833, 31));

        totalPrice.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        totalPrice.setForeground(new java.awt.Color(255, 255, 0));
        totalPrice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        totalPrice.setText("0.00");

        vat.setBackground(new java.awt.Color(51, 51, 51));
        vat.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        vat.setForeground(new java.awt.Color(255, 255, 255));
        vat.setText(" Remove VAT");
        vat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vatActionPerformed(evt);
            }
        });

        jLabel34.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(255, 255, 255));
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel34.setText("Total (P):");

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGap(85, 85, 85)
                .addComponent(vat)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 225, Short.MAX_VALUE)
                .addComponent(jLabel34)
                .addGap(108, 108, 108)
                .addComponent(totalPrice)
                .addContainerGap(120, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(vat, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(totalPrice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel34)))
                .addContainerGap())
        );

        jPanel17.add(jPanel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 834, 40));

        jPanel21.setBackground(new java.awt.Color(204, 204, 204));

        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setText("Discount (P):");

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel38.setText("Paid Amount (P):");

        jLabel40.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel40.setText("Change (P):");

        change.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        change.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        change.setText("0.00");

        paid.setBackground(new java.awt.Color(204, 204, 204));
        paid.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        paid.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        paid.setText("0");
        paid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paidActionPerformed(evt);
            }
        });
        paid.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                paidKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                paidKeyReleased(evt);
            }
        });

        discount.setBackground(new java.awt.Color(204, 204, 204));
        discount.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        discount.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        discount.setText("0");
        discount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discountActionPerformed(evt);
            }
        });
        discount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                discountKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                discountKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel36)
                .addGap(10, 10, 10)
                .addComponent(discount, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel38)
                .addGap(27, 27, 27)
                .addComponent(paid, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jLabel40)
                .addGap(42, 42, 42)
                .addComponent(change)
                .addGap(50, 50, 50))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel21Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(paid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(change))
                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel36)
                        .addComponent(discount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel17.add(jPanel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 45, 834, 40));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 834, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pos_screen.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        jPanel5.setBackground(new java.awt.Color(153, 153, 153));

        jPanel32.setBackground(new java.awt.Color(204, 204, 255));
        jPanel32.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("Invoice");
        jLabel16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel16MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel16MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel16MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel32Layout = new javax.swing.GroupLayout(jPanel32);
        jPanel32.setLayout(jPanel32Layout);
        jPanel32Layout.setHorizontalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
        );
        jPanel32Layout.setVerticalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
        );

        jPanel33.setBackground(new java.awt.Color(204, 204, 255));
        jPanel33.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Quotation");
        jLabel15.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel15MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel15MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel15MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33.setLayout(jPanel33Layout);
        jPanel33Layout.setHorizontalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
        );
        jPanel33Layout.setVerticalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
        );

        jPanel35.setBackground(new java.awt.Color(204, 204, 255));
        jPanel35.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Remove");
        jLabel17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel17MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel17MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel17MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel35Layout = new javax.swing.GroupLayout(jPanel35);
        jPanel35.setLayout(jPanel35Layout);
        jPanel35Layout.setHorizontalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
        );
        jPanel35Layout.setVerticalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel36.setBackground(new java.awt.Color(204, 204, 255));
        jPanel36.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Refresh");
        jLabel18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel18MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel18MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel18MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel36Layout = new javax.swing.GroupLayout(jPanel36);
        jPanel36.setLayout(jPanel36Layout);
        jPanel36Layout.setHorizontalGroup(
            jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
        );
        jPanel36Layout.setVerticalGroup(
            jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jPanel33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(93, 93, 93)
                .addComponent(jPanel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(104, 104, 104)
                .addComponent(jPanel35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 95, Short.MAX_VALUE)
                .addComponent(jPanel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11))
        );

        pos_screen.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, 830, 50));

        jPanel30.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel24.setFont(new java.awt.Font("Script MT Bold", 0, 18)); // NOI18N
        jLabel24.setText("Date & Time");
        jPanel30.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(133, 11, -1, -1));
        jPanel30.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(133, 32, 93, -1));

        pos_time.setFont(new java.awt.Font("Times New Roman", 0, 21)); // NOI18N
        pos_time.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/clock.png"))); // NOI18N
        pos_time.setText("99:99:99 PM");
        jPanel30.add(pos_time, new org.netbeans.lib.awtextra.AbsoluteConstraints(105, 44, -1, -1));

        pos_date.setFont(new java.awt.Font("Times New Roman", 0, 13)); // NOI18N
        pos_date.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/calendar.png"))); // NOI18N
        pos_date.setText("99-99-9999");
        jPanel30.add(pos_date, new org.netbeans.lib.awtextra.AbsoluteConstraints(135, 75, -1, -1));
        jPanel30.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 350, -1));

        jLabel42.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel42.setText("Total Price (P):");
        jPanel30.add(jLabel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 118, -1, -1));

        jLabel43.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel43.setText("Available Quantity:");
        jPanel30.add(jLabel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 146, -1, -1));

        jLabel44.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel44.setText("Product ID:");
        jPanel30.add(jLabel44, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 174, -1, -1));

        jLabel45.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel45.setText("Stock Status:");
        jPanel30.add(jLabel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 203, -1, -1));

        jLabel50.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel50.setText("Logged in:");
        jPanel30.add(jLabel50, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 420, -1, -1));

        jPanel23.setBackground(new java.awt.Color(153, 153, 153));
        jPanel23.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel37.setText("Log Out");
        jLabel37.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel37MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel37MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel37MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        jPanel30.add(jPanel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 460, -1, -1));

        loggedIn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        loggedIn.setText("Tiroyamodimo");
        jPanel30.add(loggedIn, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 420, -1, -1));
        jPanel30.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 405, 350, -1));

        customerVatNumber.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        customerVatNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerVatNumberActionPerformed(evt);
            }
        });
        jPanel30.add(customerVatNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 320, -1));

        customerTel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel30.add(customerTel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 320, 320, -1));

        customerName.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel30.add(customerName, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 320, -1));
        jPanel30.add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 231, 350, -1));

        unit_price.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        unit_price.setText("0.0");
        jPanel30.add(unit_price, new org.netbeans.lib.awtextra.AbsoluteConstraints(133, 118, 113, -1));

        avail_quantity.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        avail_quantity.setText("0.0");
        jPanel30.add(avail_quantity, new org.netbeans.lib.awtextra.AbsoluteConstraints(133, 146, 113, -1));

        pos_prodID.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        pos_prodID.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        pos_prodID.setText("0");
        jPanel30.add(pos_prodID, new org.netbeans.lib.awtextra.AbsoluteConstraints(133, 174, 110, -1));

        jLabel55.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel55.setText("Customer Name:");
        jPanel30.add(jLabel55, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 244, -1, -1));

        jLabel56.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel56.setText("Customer Number:");
        jPanel30.add(jLabel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 300, -1, -1));

        jLabel57.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel57.setText("VAT Number:");
        jPanel30.add(jLabel57, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 350, -1, -1));

        stock_status.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        stock_status.setText("STATUS UNAVAILABLE");
        jPanel30.add(stock_status, new org.netbeans.lib.awtextra.AbsoluteConstraints(133, 203, 140, 17));

        pos_screen.add(jPanel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 11, 340, 500));

        jPanel3.add(pos_screen, "card3");

        inventory_screen.setBackground(new java.awt.Color(204, 204, 204));
        inventory_screen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                inventory_screenMouseEntered(evt);
            }
        });

        jPanel12.setBackground(new java.awt.Color(204, 204, 204));

        jPanel13.setBackground(new java.awt.Color(51, 51, 51));
        jPanel13.setPreferredSize(new java.awt.Dimension(705, 75));

        logo5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        logo5.setForeground(new java.awt.Color(255, 255, 255));
        logo5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/product-review.png"))); // NOI18N
        logo5.setText(" Inventory Information");

        logo6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/magnifying-glass.png"))); // NOI18N

        search_inventory.setFont(new java.awt.Font("Segoe Print", 0, 12)); // NOI18N
        search_inventory.setForeground(new java.awt.Color(102, 102, 102));
        search_inventory.setText("Search product name...");
        search_inventory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                search_inventoryActionPerformed(evt);
            }
        });
        search_inventory.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                search_inventoryKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                search_inventoryKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(logo5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logo6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(search_inventory, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(131, 131, 131))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(logo6)
                        .addComponent(search_inventory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(logo5))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jPanel14.setBackground(new java.awt.Color(204, 204, 204));
        jPanel14.setPreferredSize(new java.awt.Dimension(1060, 240));

        jScrollPane5.setBackground(new java.awt.Color(204, 204, 204));
        jScrollPane5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jScrollPane5.setPreferredSize(new java.awt.Dimension(1060, 240));

        inventory_table.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        inventory_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product ID", "Product Name", "Stock Level", "Price (P)", "Description"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        inventory_table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                inventory_tableMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(inventory_table);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel15.setBackground(new java.awt.Color(204, 204, 204));
        jPanel15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        jLabel23.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(102, 102, 102));
        jLabel23.setText("Product ID:");

        product_id.setEditable(false);
        product_id.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N

        product_name.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N

        stock_level.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        stock_level.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stock_levelActionPerformed(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(102, 102, 102));
        jLabel25.setText("Product Name:");

        jLabel26.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(102, 102, 102));
        jLabel26.setText("Stock Level:");

        jLabel28.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(102, 102, 102));
        jLabel28.setText("Price");

        product_price.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N

        jLabel27.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(102, 102, 102));
        jLabel27.setText("Description:");

        product_descr.setColumns(20);
        product_descr.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        product_descr.setRows(5);
        jScrollPane6.setViewportView(product_descr);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addGap(120, 120, 120)
                        .addComponent(product_id, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel26)
                            .addComponent(jLabel25))
                        .addGap(99, 99, 99)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(product_name, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(stock_level, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(232, 232, 232)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addGap(104, 104, 104)
                        .addComponent(product_price, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(49, 49, 49))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel28)
                        .addComponent(product_price, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel15Layout.createSequentialGroup()
                            .addComponent(jLabel23)
                            .addGap(6, 6, 6))
                        .addComponent(product_id, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel15Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel26))
                            .addGroup(jPanel15Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(stock_level, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel27))))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addComponent(product_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel16.setBackground(new java.awt.Color(204, 204, 204));

        showAll_inventory.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        showAll_inventory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/eye.png"))); // NOI18N
        showAll_inventory.setText("Show All");
        showAll_inventory.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        showAll_inventory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showAll_inventoryMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                showAll_inventoryMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                showAll_inventoryMouseExited(evt);
            }
        });

        update_inventory.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        update_inventory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/cloud-upload.png"))); // NOI18N
        update_inventory.setText("Update");
        update_inventory.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        update_inventory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                update_inventoryMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                update_inventoryMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                update_inventoryMouseExited(evt);
            }
        });

        delete_inventory.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        delete_inventory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/bin.png"))); // NOI18N
        delete_inventory.setText("Delete");
        delete_inventory.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        delete_inventory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                delete_inventoryMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                delete_inventoryMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                delete_inventoryMouseExited(evt);
            }
        });

        add_inventory.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        add_inventory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/add.png"))); // NOI18N
        add_inventory.setText("Add");
        add_inventory.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        add_inventory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                add_inventoryMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                add_inventoryMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                add_inventoryMouseExited(evt);
            }
        });

        refresh_inventory.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        refresh_inventory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/refresh.png"))); // NOI18N
        refresh_inventory.setText("Refresh");
        refresh_inventory.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        refresh_inventory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                refresh_inventoryMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refresh_inventoryMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                refresh_inventoryMouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGap(113, 113, 113)
                .addComponent(showAll_inventory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(update_inventory)
                .addGap(150, 150, 150)
                .addComponent(delete_inventory)
                .addGap(162, 162, 162)
                .addComponent(add_inventory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(refresh_inventory)
                .addGap(113, 113, 113))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                .addContainerGap(33, Short.MAX_VALUE)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(update_inventory)
                        .addComponent(delete_inventory)
                        .addComponent(add_inventory)
                        .addComponent(refresh_inventory))
                    .addComponent(showAll_inventory))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, 1188, Short.MAX_VALUE)
            .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, 1188, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 24, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout inventory_screenLayout = new javax.swing.GroupLayout(inventory_screen);
        inventory_screen.setLayout(inventory_screenLayout);
        inventory_screenLayout.setHorizontalGroup(
            inventory_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inventory_screenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        inventory_screenLayout.setVerticalGroup(
            inventory_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inventory_screenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.add(inventory_screen, "card4");

        tools_screen.setBackground(new java.awt.Color(204, 204, 204));
        tools_screen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tools_screenMouseEntered(evt);
            }
        });

        jPanel7.setBackground(new java.awt.Color(204, 204, 204));

        jPanel8.setBackground(new java.awt.Color(51, 51, 51));
        jPanel8.setPreferredSize(new java.awt.Dimension(705, 75));

        logo3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        logo3.setForeground(new java.awt.Color(255, 255, 255));
        logo3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/helmet.png"))); // NOI18N
        logo3.setText(" Tools & Machinery Information");

        logo4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/magnifying-glass.png"))); // NOI18N

        search_tools.setFont(new java.awt.Font("Segoe Print", 0, 12)); // NOI18N
        search_tools.setForeground(new java.awt.Color(102, 102, 102));
        search_tools.setText("Search Tools & Machines...");
        search_tools.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                search_toolsActionPerformed(evt);
            }
        });
        search_tools.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                search_toolsKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                search_toolsKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(logo3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logo4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(search_tools, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(131, 131, 131))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(logo4)
                        .addComponent(search_tools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(logo3))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jPanel9.setBackground(new java.awt.Color(204, 204, 204));
        jPanel9.setPreferredSize(new java.awt.Dimension(1060, 240));

        jScrollPane4.setBackground(new java.awt.Color(204, 204, 204));
        jScrollPane4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jScrollPane4.setPreferredSize(new java.awt.Dimension(1060, 240));

        tools_table.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        tools_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tools / Machinery ID", "Tool / Machine Name", "Quantity", "In Possession of", "Details / Location"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tools_table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tools_tableMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tools_table);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 1188, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel10.setBackground(new java.awt.Color(204, 204, 204));
        jPanel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(102, 102, 102));
        jLabel4.setText("Tools / Machine ID:");

        tool_id.setEditable(false);
        tool_id.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N

        tool_name.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N

        tool_quantity.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N

        tool_possession.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        tool_possession.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tool_possessionActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(102, 102, 102));
        jLabel19.setText("In Possession of:");

        jLabel20.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(102, 102, 102));
        jLabel20.setText("Quanitity:");

        jLabel21.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(102, 102, 102));
        jLabel21.setText("Tools / Machine Name:");

        jLabel22.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(102, 102, 102));
        jLabel22.setText("Details / Location:");

        tool_details.setColumns(20);
        tool_details.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        tool_details.setRows(5);
        jScrollPane3.setViewportView(tool_details);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tool_id, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tool_name, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tool_quantity, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(106, 106, 106)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tool_possession, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(48, 48, 48))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tool_possession, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel19)))
                    .addComponent(tool_id, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tool_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel10Layout.createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addComponent(jLabel21))))
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(jLabel22)))
                        .addGap(11, 11, 11)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(jLabel20))
                            .addComponent(tool_quantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel11.setBackground(new java.awt.Color(204, 204, 204));

        show_tools.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        show_tools.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/eye.png"))); // NOI18N
        show_tools.setText("Show All");
        show_tools.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        show_tools.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                show_toolsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                show_toolsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                show_toolsMouseExited(evt);
            }
        });

        update_tools.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        update_tools.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/cloud-upload.png"))); // NOI18N
        update_tools.setText("Update");
        update_tools.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        update_tools.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                update_toolsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                update_toolsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                update_toolsMouseExited(evt);
            }
        });

        delete_tools.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        delete_tools.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/bin.png"))); // NOI18N
        delete_tools.setText("Delete");
        delete_tools.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        delete_tools.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                delete_toolsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                delete_toolsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                delete_toolsMouseExited(evt);
            }
        });

        add_tools.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        add_tools.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/add.png"))); // NOI18N
        add_tools.setText("Add");
        add_tools.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        add_tools.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                add_toolsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                add_toolsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                add_toolsMouseExited(evt);
            }
        });

        refresh_tools.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        refresh_tools.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/refresh.png"))); // NOI18N
        refresh_tools.setText("Refresh");
        refresh_tools.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        refresh_tools.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                refresh_toolsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refresh_toolsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                refresh_toolsMouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(113, 113, 113)
                .addComponent(show_tools)
                .addGap(155, 155, 155)
                .addComponent(update_tools)
                .addGap(155, 155, 155)
                .addComponent(delete_tools)
                .addGap(155, 155, 155)
                .addComponent(add_tools)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 124, Short.MAX_VALUE)
                .addComponent(refresh_tools)
                .addGap(113, 113, 113))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(update_tools)
                        .addComponent(delete_tools)
                        .addComponent(add_tools)
                        .addComponent(refresh_tools))
                    .addComponent(show_tools))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, 1188, Short.MAX_VALUE)
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 1188, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout tools_screenLayout = new javax.swing.GroupLayout(tools_screen);
        tools_screen.setLayout(tools_screenLayout);
        tools_screenLayout.setHorizontalGroup(
            tools_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tools_screenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        tools_screenLayout.setVerticalGroup(
            tools_screenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tools_screenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.add(tools_screen, "card5");

        settings_screen.setBackground(new java.awt.Color(204, 204, 204));
        settings_screen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                settings_screenMouseEntered(evt);
            }
        });
        settings_screen.setLayout(new java.awt.CardLayout());

        jLabel39.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/settings_user.png"))); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setText("User ID:");

        settings_id.setEditable(false);
        settings_id.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        settings_id.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        settings_id.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settings_idActionPerformed(evt);
            }
        });

        jLabel35.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel35.setText("User Name:");

        settings_usrName.setEditable(false);
        settings_usrName.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        settings_usrName.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        jPanel27.setBackground(new java.awt.Color(0, 102, 102));

        jLabel46.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(255, 255, 255));
        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel46.setText("Change Password");
        jLabel46.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel46.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel46MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel46MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel46MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel46, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel46, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
        );

        jPanel25.setBackground(new java.awt.Color(0, 102, 102));
        jPanel25.setToolTipText("");

        jLabel47.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel47.setText("Admin Settings");
        jLabel47.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel47.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel47MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel47MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel47MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel47, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel47, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
        );

        jLabel41.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel41.setText("Logged In:");

        javax.swing.GroupLayout general_settingsLayout = new javax.swing.GroupLayout(general_settings);
        general_settings.setLayout(general_settingsLayout);
        general_settingsLayout.setHorizontalGroup(
            general_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(general_settingsLayout.createSequentialGroup()
                .addGap(386, 386, 386)
                .addGroup(general_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(general_settingsLayout.createSequentialGroup()
                        .addGap(177, 177, 177)
                        .addComponent(jLabel39))
                    .addGroup(general_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(settings_id, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, general_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, general_settingsLayout.createSequentialGroup()
                                .addComponent(jLabel35)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(settings_usrName, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, general_settingsLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(365, 365, 365)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, general_settingsLayout.createSequentialGroup()
                            .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(94, 94, 94)
                            .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(23, 23, 23)))
                    .addGroup(general_settingsLayout.createSequentialGroup()
                        .addGap(209, 209, 209)
                        .addComponent(jLabel41)))
                .addContainerGap(399, Short.MAX_VALUE))
        );
        general_settingsLayout.setVerticalGroup(
            general_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(general_settingsLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jLabel39)
                .addGap(43, 43, 43)
                .addComponent(jLabel41)
                .addGap(26, 26, 26)
                .addGroup(general_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(settings_id, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(31, 31, 31)
                .addGroup(general_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(settings_usrName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(63, 63, 63)
                .addGroup(general_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(84, Short.MAX_VALUE))
        );

        settings_screen.add(general_settings, "card2");

        admin_settings.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel49.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel49.setText("Select User:");
        admin_settings.add(jLabel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(86, 88, -1, -1));

        jLabel51.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel51.setText("Enter User ID:");
        admin_settings.add(jLabel51, new org.netbeans.lib.awtextra.AbsoluteConstraints(86, 126, -1, -1));

        select_user.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        select_user.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select a User" }));
        select_user.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                select_userMouseClicked(evt);
            }
        });
        select_user.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_userActionPerformed(evt);
            }
        });
        admin_settings.add(select_user, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 85, 278, -1));

        userID_search.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        userID_search.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                userID_searchKeyPressed(evt);
            }
        });
        admin_settings.add(userID_search, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 123, 165, -1));

        jPanel28.setBackground(new java.awt.Color(102, 102, 102));
        jPanel28.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel73.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel73.setForeground(new java.awt.Color(255, 255, 255));
        jLabel73.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel73.setText("Search");
        jLabel73.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel73MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel73MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel73MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel73, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel73, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        admin_settings.add(jPanel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 123, 95, -1));

        jLabel54.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel54.setText("User ID:");
        admin_settings.add(jLabel54, new org.netbeans.lib.awtextra.AbsoluteConstraints(129, 229, -1, -1));

        userID.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        admin_settings.add(userID, new org.netbeans.lib.awtextra.AbsoluteConstraints(226, 226, 190, -1));

        jLabel58.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel58.setText("Password:");
        admin_settings.add(jLabel58, new org.netbeans.lib.awtextra.AbsoluteConstraints(129, 275, -1, -1));

        userName.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        admin_settings.add(userName, new org.netbeans.lib.awtextra.AbsoluteConstraints(226, 316, 190, -1));

        jLabel59.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel59.setText("User Name:");
        admin_settings.add(jLabel59, new org.netbeans.lib.awtextra.AbsoluteConstraints(129, 319, -1, -1));

        jLabel60.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel60.setText("User Type:");
        admin_settings.add(jLabel60, new org.netbeans.lib.awtextra.AbsoluteConstraints(129, 365, -1, -1));

        userType.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        userType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "staff", "admin" }));
        admin_settings.add(userType, new org.netbeans.lib.awtextra.AbsoluteConstraints(226, 362, 190, -1));

        jLabel61.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel61.setText("User Settings");
        admin_settings.add(jLabel61, new org.netbeans.lib.awtextra.AbsoluteConstraints(209, 23, -1, -1));

        jLabel62.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel62.setText("User Data");
        admin_settings.add(jLabel62, new org.netbeans.lib.awtextra.AbsoluteConstraints(238, 186, -1, -1));

        jPanel31.setBackground(new java.awt.Color(102, 102, 102));
        jPanel31.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel70.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel70.setForeground(new java.awt.Color(255, 255, 255));
        jLabel70.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel70.setText("Add New User");
        jLabel70.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel70MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel70MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel70MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel70, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
        );
        jPanel31Layout.setVerticalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel70, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        admin_settings.add(jPanel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 420, -1, -1));

        jPanel34.setBackground(new java.awt.Color(102, 102, 102));
        jPanel34.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel69.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel69.setForeground(new java.awt.Color(255, 255, 255));
        jLabel69.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel69.setText("Delete User");
        jLabel69.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel69MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel69MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel69MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel69, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel34Layout.setVerticalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel69, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        admin_settings.add(jPanel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 420, 110, -1));

        jPanel18.setBackground(new java.awt.Color(204, 204, 204));
        jPanel18.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel52.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel52.setText("Company Information");
        jPanel18.add(jLabel52, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 10, -1, -1));

        jLabel63.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel63.setText("Company Name:");
        jPanel18.add(jLabel63, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 70, -1, -1));

        jLabel64.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel64.setText("Company Address:");
        jPanel18.add(jLabel64, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 110, -1, -1));

        jLabel65.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel65.setText("Company P.O Box:");
        jPanel18.add(jLabel65, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 150, -1, -1));

        jLabel66.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel66.setText("Company VAT No:");
        jPanel18.add(jLabel66, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 190, -1, -1));

        jLabel67.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel67.setText("Company Tel:");
        jPanel18.add(jLabel67, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 230, -1, -1));

        jLabel68.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel68.setText("Company Email:");
        jPanel18.add(jLabel68, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 270, -1, -1));

        companyName_field.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel18.add(companyName_field, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 70, 270, -1));

        companyAddress_field.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel18.add(companyAddress_field, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 110, 270, -1));

        companyPO_field.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel18.add(companyPO_field, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 150, 270, -1));

        companyVat_field.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel18.add(companyVat_field, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 190, 270, -1));

        companyTel_field.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel18.add(companyTel_field, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 230, 270, -1));

        companyEmail_field.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel18.add(companyEmail_field, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 270, 270, -1));

        jPanel22.setBackground(new java.awt.Color(102, 102, 102));

        jLabel71.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel71.setForeground(new java.awt.Color(255, 255, 255));
        jLabel71.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel71.setText("Update Information");
        jLabel71.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel71.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel71MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel71MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel71MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel71, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel71, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );

        jPanel18.add(jPanel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 480, 140, 30));

        jPanel29.setBackground(new java.awt.Color(102, 102, 102));

        jLabel74.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel74.setForeground(new java.awt.Color(255, 255, 255));
        jLabel74.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel74.setText("Clear All Data");
        jLabel74.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel74.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel74MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel74MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel74MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel29Layout = new javax.swing.GroupLayout(jPanel29);
        jPanel29.setLayout(jPanel29Layout);
        jPanel29Layout.setHorizontalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel74, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
        );
        jPanel29Layout.setVerticalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel74, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );

        jPanel18.add(jPanel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 480, 140, -1));

        jLabel75.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel75.setText("Tax rate (%):");
        jPanel18.add(jLabel75, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 310, -1, -1));

        taxRate_field.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel18.add(taxRate_field, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 310, 270, -1));

        jLabel76.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel76.setText("Bank Name:");
        jPanel18.add(jLabel76, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 350, -1, -1));

        bankName_field.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel18.add(bankName_field, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 350, 270, -1));

        jLabel77.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel77.setText("Account Number:");
        jPanel18.add(jLabel77, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 390, -1, -1));

        accountNumber_field.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel18.add(accountNumber_field, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 390, 270, -1));

        jLabel78.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel78.setText("Bank Branch:");
        jPanel18.add(jLabel78, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 430, -1, -1));

        bankBranch_field.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel18.add(bankBranch_field, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 430, 270, -1));

        admin_settings.add(jPanel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(547, 0, 653, 520));

        userPass.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        admin_settings.add(userPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(226, 272, 190, -1));

        jLabel72.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/back arrow.png"))); // NOI18N
        jLabel72.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel72.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel72MouseClicked(evt);
            }
        });
        admin_settings.add(jLabel72, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, -1, -1));

        jPanel37.setBackground(new java.awt.Color(102, 102, 102));
        jPanel37.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel79.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel79.setForeground(new java.awt.Color(255, 255, 255));
        jLabel79.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel79.setText("Update User");
        jLabel79.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel79MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel79MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel79MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel37Layout = new javax.swing.GroupLayout(jPanel37);
        jPanel37.setLayout(jPanel37Layout);
        jPanel37Layout.setHorizontalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel79, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel37Layout.setVerticalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel79, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        admin_settings.add(jPanel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 420, 110, -1));

        settings_screen.add(admin_settings, "card3");

        jPanel3.add(settings_screen, "card6");

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 81, 1200, 520));

        jPanel26.setBackground(new java.awt.Color(0, 102, 102));
        jPanel26.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel26.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jPanel26MouseDragged(evt);
            }
        });
        jPanel26.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jPanel26MousePressed(evt);
            }
        });

        minimize.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        minimize.setForeground(new java.awt.Color(255, 255, 255));
        minimize.setText("  -");
        minimize.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        minimize.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        minimize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                minimizeMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                minimizeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                minimizeMouseExited(evt);
            }
        });

        close.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        close.setForeground(new java.awt.Color(255, 255, 255));
        close.setText(" X");
        close.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        close.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeMouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel26Layout.createSequentialGroup()
                .addGap(1160, 1160, 1160)
                .addComponent(minimize, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(close, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(minimize, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(close, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1220, 30));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1220, 610));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void minimizeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMouseClicked
        this.setState(Main_menu.ICONIFIED); // Minimize window
    }//GEN-LAST:event_minimizeMouseClicked

    private void closeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseClicked

        /* Logging off from POS application */
        
        // logs user off from POS system without warning if price list has been saved or no price list has been made
        if (saved){
            
            dispose(); // Closes Main Menu form
            log.show(); // Show login form
            
        } else {
            
            // Warning to user when about to log out with out saving current price list
            int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the POS application before saving current "
                    + "price list?\nClicking \"YES\" will close and erase all POS price list data and log you out.", "Exit Without Saving?", 
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (reply == JOptionPane.YES_OPTION) {
                    dispose(); // Closes Main Menu form
                    log.show(); // Show login form
                    
                } else {
                    // POS not closed and user is not logged out
                }
        }  
    }//GEN-LAST:event_closeMouseClicked

    private void homeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homeMouseEntered
        home.setForeground(Color.black);
    }//GEN-LAST:event_homeMouseEntered

    private void homeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homeMouseExited
        home.setForeground(Color.white);
    }//GEN-LAST:event_homeMouseExited

    private void posMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_posMouseEntered
        pos.setForeground(Color.black);
    }//GEN-LAST:event_posMouseEntered

    private void posMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_posMouseExited
        pos.setForeground(Color.white);
    }//GEN-LAST:event_posMouseExited

    private void inventoryMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inventoryMouseEntered
        inventory.setForeground(Color.black);
    }//GEN-LAST:event_inventoryMouseEntered

    private void inventoryMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inventoryMouseExited
        inventory.setForeground(Color.white);
    }//GEN-LAST:event_inventoryMouseExited

    private void toolsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolsMouseEntered
        tools.setForeground(Color.black);
    }//GEN-LAST:event_toolsMouseEntered

    private void toolsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolsMouseExited
        tools.setForeground(Color.white);
    }//GEN-LAST:event_toolsMouseExited

    private void settingsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingsMouseEntered
        settings.setForeground(Color.black);
    }//GEN-LAST:event_settingsMouseEntered

    private void settingsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingsMouseExited
        settings.setForeground(Color.white);
    }//GEN-LAST:event_settingsMouseExited

    private void homeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homeMouseClicked
        // Display home screen
        home_screen.setVisible(true); 
        pos_screen.setVisible(false);
        inventory_screen.setVisible(false);
        tools_screen.setVisible(false);
        settings_screen.setVisible(false);
    }//GEN-LAST:event_homeMouseClicked

    private void posMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_posMouseClicked
        // Display pos screen
        home_screen.setVisible(false);
        pos_screen.setVisible(true); 
        inventory_screen.setVisible(false);
        tools_screen.setVisible(false);
        settings_screen.setVisible(false);
    }//GEN-LAST:event_posMouseClicked

    private void inventoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inventoryMouseClicked
        // Display invetory screen
        home_screen.setVisible(false);
        pos_screen.setVisible(false);
        inventory_screen.setVisible(true); 
        tools_screen.setVisible(false);
        settings_screen.setVisible(false);
    }//GEN-LAST:event_inventoryMouseClicked

    private void toolsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolsMouseClicked
        // Display tools screen
        home_screen.setVisible(false);
        pos_screen.setVisible(false);
        inventory_screen.setVisible(false);
        tools_screen.setVisible(true); 
        settings_screen.setVisible(false);
    }//GEN-LAST:event_toolsMouseClicked

    private void settingsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingsMouseClicked

        int demo = 1;
        if (demo == 1){
            // Display settings screen
            home_screen.setVisible(false);
            pos_screen.setVisible(false);
            inventory_screen.setVisible(false);
            tools_screen.setVisible(false);
            settings_screen.setVisible(true); 

            // Displaying general settings screen
            general_settings.setVisible(true);
            admin_settings.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(rootPane, "Feature not yet available.", "Settings", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_settingsMouseClicked

    private void pos_panelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pos_panelMouseClicked
        // Display pos screen
        home_screen.setVisible(false);
        pos_screen.setVisible(true); 
        inventory_screen.setVisible(false);
        tools_screen.setVisible(false);
        settings_screen.setVisible(false);
    }//GEN-LAST:event_pos_panelMouseClicked

    private void inventory_panelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inventory_panelMouseClicked
        // Display invetory screen
        home_screen.setVisible(false);
        pos_screen.setVisible(false);
        inventory_screen.setVisible(true); 
        tools_screen.setVisible(false);
        settings_screen.setVisible(false);
    }//GEN-LAST:event_inventory_panelMouseClicked

    private void tools_panelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tools_panelMouseClicked
        // Display tools screen
        home_screen.setVisible(false);
        pos_screen.setVisible(false);
        inventory_screen.setVisible(false);
        tools_screen.setVisible(true); 
        settings_screen.setVisible(false);
    }//GEN-LAST:event_tools_panelMouseClicked

    private void settings_panelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settings_panelMouseClicked

        int demo = 1;
        if (demo == 1) {

            // Display settings screen
            home_screen.setVisible(false);
            pos_screen.setVisible(false);
            inventory_screen.setVisible(false);
            tools_screen.setVisible(false);
            settings_screen.setVisible(true); 
        }else {
            JOptionPane.showMessageDialog(rootPane, "Feature not yet available", "Settings", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_settings_panelMouseClicked

    private void pos_panelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pos_panelMouseEntered
        jLabel11.setForeground(Color.red);
    }//GEN-LAST:event_pos_panelMouseEntered

    private void pos_panelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pos_panelMouseExited
        jLabel11.setForeground(Color.black);
    }//GEN-LAST:event_pos_panelMouseExited

    private void inventory_panelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inventory_panelMouseEntered
        jLabel12.setForeground(Color.red);
    }//GEN-LAST:event_inventory_panelMouseEntered

    private void inventory_panelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inventory_panelMouseExited
        jLabel12.setForeground(Color.black);
    }//GEN-LAST:event_inventory_panelMouseExited

    private void tools_panelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tools_panelMouseEntered
       jLabel13.setForeground(Color.red);
    }//GEN-LAST:event_tools_panelMouseEntered

    private void tools_panelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tools_panelMouseExited
        jLabel13.setForeground(Color.black);
    }//GEN-LAST:event_tools_panelMouseExited

    private void settings_panelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settings_panelMouseEntered
        jLabel14.setForeground(Color.red);
    }//GEN-LAST:event_settings_panelMouseEntered

    private void settings_panelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settings_panelMouseExited
        jLabel14.setForeground(Color.black);
    }//GEN-LAST:event_settings_panelMouseExited

    private void search_toolsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_search_toolsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_search_toolsActionPerformed

    private void tool_possessionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tool_possessionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tool_possessionActionPerformed

    private void search_inventoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_search_inventoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_search_inventoryActionPerformed

    private void stock_levelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stock_levelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stock_levelActionPerformed

    private void showAll_inventoryMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showAll_inventoryMouseExited
        showAll_inventory.setForeground(Color.black);
    }//GEN-LAST:event_showAll_inventoryMouseExited

    private void showAll_inventoryMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showAll_inventoryMouseEntered
        showAll_inventory.setForeground(Color.white);
    }//GEN-LAST:event_showAll_inventoryMouseEntered

    private void update_inventoryMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_update_inventoryMouseEntered
        update_inventory.setForeground(Color.white);
    }//GEN-LAST:event_update_inventoryMouseEntered

    private void update_inventoryMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_update_inventoryMouseExited
        update_inventory.setForeground(Color.black);
    }//GEN-LAST:event_update_inventoryMouseExited

    private void delete_inventoryMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delete_inventoryMouseEntered
        delete_inventory.setForeground(Color.white);
    }//GEN-LAST:event_delete_inventoryMouseEntered

    private void delete_inventoryMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delete_inventoryMouseExited
        delete_inventory.setForeground(Color.black);
    }//GEN-LAST:event_delete_inventoryMouseExited

    private void add_inventoryMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_add_inventoryMouseEntered
        add_inventory.setForeground(Color.white);
    }//GEN-LAST:event_add_inventoryMouseEntered

    private void add_inventoryMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_add_inventoryMouseExited
        add_inventory.setForeground(Color.black);
    }//GEN-LAST:event_add_inventoryMouseExited

    private void refresh_inventoryMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refresh_inventoryMouseEntered
        refresh_inventory.setForeground(Color.white);
    }//GEN-LAST:event_refresh_inventoryMouseEntered

    private void refresh_inventoryMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refresh_inventoryMouseExited
        refresh_inventory.setForeground(Color.black);
    }//GEN-LAST:event_refresh_inventoryMouseExited

    private void show_toolsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_show_toolsMouseEntered
        show_tools.setForeground(Color.white);
    }//GEN-LAST:event_show_toolsMouseEntered

    private void show_toolsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_show_toolsMouseExited
        show_tools.setForeground(Color.black);
    }//GEN-LAST:event_show_toolsMouseExited

    private void update_toolsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_update_toolsMouseEntered
        update_tools.setForeground(Color.white);
    }//GEN-LAST:event_update_toolsMouseEntered

    private void update_toolsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_update_toolsMouseExited
        update_tools.setForeground(Color.black);
    }//GEN-LAST:event_update_toolsMouseExited

    private void delete_toolsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delete_toolsMouseEntered
        delete_tools.setForeground(Color.white);
    }//GEN-LAST:event_delete_toolsMouseEntered

    private void delete_toolsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delete_toolsMouseExited
        delete_tools.setForeground(Color.black);
    }//GEN-LAST:event_delete_toolsMouseExited

    private void add_toolsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_add_toolsMouseEntered
        add_tools.setForeground(Color.white);
    }//GEN-LAST:event_add_toolsMouseEntered

    private void add_toolsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_add_toolsMouseExited
        add_tools.setForeground(Color.black);
    }//GEN-LAST:event_add_toolsMouseExited

    private void refresh_toolsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refresh_toolsMouseEntered
        refresh_tools.setForeground(Color.white);
    }//GEN-LAST:event_refresh_toolsMouseEntered

    private void refresh_toolsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refresh_toolsMouseExited
        refresh_tools.setForeground(Color.black);
    }//GEN-LAST:event_refresh_toolsMouseExited

    private void inventory_tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inventory_tableMouseClicked
        
        /* Populating description fields of a product selected on inventory table*/
        
        // Getting data from each column in inventory table and storing them in a variable
        int t = inventory_table.getSelectedRow();
        String pid = inventory_table.getValueAt(t, 0).toString();
        String pn = inventory_table.getValueAt(t, 1).toString();
        String sl = inventory_table.getValueAt(t, 2).toString();
        String pp = inventory_table.getValueAt(t, 3).toString();
        String pd = inventory_table.getValueAt(t, 4).toString();

        // Setting data on input fields in the inventory screen
        product_id.setText(pid);
        product_name.setText(pn);
        stock_level.setText(sl);
        product_price.setText(pp);
        product_descr.setText(pd);
        
    }//GEN-LAST:event_inventory_tableMouseClicked

    private void showAll_inventoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showAll_inventoryMouseClicked
        
        /* Displaying all inventory items on inventory table */
        
        try {
            DefaultTableModel table = (DefaultTableModel) inventory_table.getModel();
            Statement state = POS_source.mycon().createStatement();
            rs = state.executeQuery("select * from inventory_data"); // Query to get inventory data from database
            table.setRowCount(0);

            // Adding each inventory item in rows into the inventory table
            while (rs.next()) {

                Object o[] = {rs.getString("product_id"), rs.getString("product_name"), rs.getString("stock_level"), 
                    rs.getString("price"), rs.getString("description")};
                table.addRow(o);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            try {
                logMessage.log("SQLException: " + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }//GEN-LAST:event_showAll_inventoryMouseClicked

    private void delete_toolsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delete_toolsMouseClicked
        
        /* Deleting tool/machine from tools & machine list */
        
        try {

            Statement state = POS_source.mycon().createStatement();
            
            if (tool_id.getText().isEmpty()) {

                // Message to user to select a tool to delete if button is pressed when no selection has been made
                JOptionPane.showMessageDialog(null, "Select a tool / machine from the table you want to delete.");

            } else {
                
                // Warning message to user that they are about to delete the tool/machine
                int result = JOptionPane.showConfirmDialog(null,"Are you sure you want to delete the tool / machine \"" 
                        + tool_name.getText() + "\" from the system?\nThis action cannot be reversed.", "WARNING", 
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                // Tool/machine deleted from tools & machine list when the yes option is chosen to delete the tool/machine
                if(result == JOptionPane.YES_OPTION){
                    
                    state.executeUpdate("delete from tool_data where tool_id = '" + tool_id.getText() + "'"); // Delete statement to delete tool/machine 
                    JOptionPane.showMessageDialog(null, "Delete successful."); // Delete success message displayed to user
                    pss.clearTools(tool_id, tool_name, tool_quantity, tool_possession, tool_details);
                    search_tools.setText("Search Tools & Machines...");
                    
                    // Clearing all rows after deleting tool
                    DefaultTableModel table = (DefaultTableModel) tools_table.getModel();
                    table.setRowCount(0);

                }else if (result == JOptionPane.NO_OPTION){
                    // Nothing done if user selects No option after warning message to delete tool/machine
                } 
            }
        } catch (HeadlessException | SQLException x) {
            System.out.println(x.getMessage());
            try {
                logMessage.log("HeadlessException | SQLException: " + x.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }//GEN-LAST:event_delete_toolsMouseClicked

    private void update_toolsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_update_toolsMouseClicked
        
        /* Updating tool/machine information */
        
        try {
            Statement state = POS_source.mycon().createStatement();
            
            if (tool_id.getText().isEmpty()) {
                
                // Message displayed to user if no tool/machiine is selected
                JOptionPane.showMessageDialog(null, "Select a tool / machine from the table you want to update.");
                
            } else {

                // Updating information of selected tool/machine
                state.executeUpdate("update tool_data set  tool_name = '" + tool_name.getText() + "' , quantity = '" 
                        + tool_quantity.getText() + "', possession_of = '" + tool_possession.getText() + "', details = '" + tool_details.getText() 
                        + "' where tool_id = '" + tool_id.getText() + "'");
                JOptionPane.showMessageDialog(rootPane, "Update successful."); // Update successful message displayed to user
                pss.clearTools(tool_id, tool_name, tool_quantity, tool_possession, tool_details);
                search_inventory.setText("Search Tools & Machines...");
                
                // Clearing all rows beiing displayed in the table 
                DefaultTableModel table = (DefaultTableModel) tools_table.getModel();
                table.setRowCount(0);
                
            }
        } catch (HeadlessException | SQLException ex) {
            System.out.println(ex.getMessage());
            // Update unscuccessful message displayed to user
            JOptionPane.showMessageDialog(null, "Update unscuccessful. Check all input fields.");
            try {
                logMessage.log("HeadlessException | SQLException: " + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
            
        }
        
    }//GEN-LAST:event_update_toolsMouseClicked

    private void update_inventoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_update_inventoryMouseClicked
        
        /* Updating inventory information */
        
        try {
            Statement state = POS_source.mycon().createStatement();
            if (product_id.getText().isEmpty()) {

                // Message displayed to user if no inventory item is selected
                JOptionPane.showMessageDialog(null, "Select a product from the table you want to update.");
                
            } else {

                // Updating information of selected inventory item
                state.executeUpdate("update inventory_data set  product_name = '" + product_name.getText() + "' , stock_level = '" 
                        + stock_level.getText() + "', price = '" + product_price.getText() + "', description = '" + product_descr.getText() 
                        + "' where product_id = '" + product_id.getText() + "'");
                JOptionPane.showMessageDialog(rootPane, "Update successful."); // Update successful message displayed to user
                pss.clearInventory(product_id, product_name, stock_level, product_price, product_descr);
                search_inventory.setText("Search product name...");
                
                // Clearing all rows beiing displayed in the table 
                DefaultTableModel table = (DefaultTableModel) inventory_table.getModel();
                table.setRowCount(0);
                
            }
        } catch (HeadlessException | SQLException ex) {
            System.out.println(ex);
            // Update unscuccessful message displayed to user
            JOptionPane.showMessageDialog(null, "Update unscuccessful. Check all input fields.");
            try {
                logMessage.log("HeadlessException | SQLException: " + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }      
    }//GEN-LAST:event_update_inventoryMouseClicked

    private void delete_inventoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delete_inventoryMouseClicked
        
        /* Deleting Inventory item from inventory list */
        
        try {

            Statement state = POS_source.mycon().createStatement();
            if (product_id.getText().isEmpty()) {

                // Message to user to select a tool to delete if button is pressed when no selection has been made
                JOptionPane.showMessageDialog(null, "Select a product from the table you want to delete.");

            } else {
                
                // Warning message to user that they are about to delete the inventory item
                int result = JOptionPane.showConfirmDialog(null,"Are you sure you want to delete the product \"" + product_name.getText() 
                        + "\" from the inventory?\nThis action cannot be reversed.", "WARNING", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                // Inventory item deleted from inventory list when the yes option is chosen to delete the inventory item
                if(result == JOptionPane.YES_OPTION){
                    
                    state.executeUpdate("delete from inventory_data where product_id = '" + product_id.getText() + "'"); // Delete statement to delete inventory item
                    JOptionPane.showMessageDialog(null, "Delete successful."); // Delete success message displayed to user
                    pss.clearInventory(product_id, product_name, stock_level, product_price, product_descr);
                    search_inventory.setText("Search product name...");
                    
                    // Clearing all rows after deleting inventory item
                    DefaultTableModel table = (DefaultTableModel) inventory_table.getModel();
                    table.setRowCount(0);

                }else if (result == JOptionPane.NO_OPTION){
                    // Nothing done if user selects No option after warning message to delete inventory item
                } 
            }
        } catch (HeadlessException | SQLException x) {
            System.out.println(x.getMessage());
            try {
                logMessage.log("HeadlessException | SQLException: " + x.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_delete_inventoryMouseClicked

    private void refresh_inventoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refresh_inventoryMouseClicked
        
        /* Clearing all items in inventory screen */
        
        DefaultTableModel table = (DefaultTableModel) inventory_table.getModel();
        table.setRowCount(0);
        pss.clearInventory(product_id, product_name, stock_level, product_price, product_descr);
        search_inventory.setText("Search product name...");
        
    }//GEN-LAST:event_refresh_inventoryMouseClicked

    private void search_inventoryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_search_inventoryKeyPressed
        
        // Text on search bar for inventory screen
        if (search_inventory.getText().equals("Search product name...")) {
            search_inventory.setText("");
        }
    }//GEN-LAST:event_search_inventoryKeyPressed

    private void search_inventoryKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_search_inventoryKeyReleased

        /* Searching inventory items using search bar */
        
        String search = search_inventory.getText(); // Variable used in sql query

        // Getting inventory data from values typed in search bar
        try {
            DefaultTableModel table = (DefaultTableModel) inventory_table.getModel();
            table.setRowCount(0);
            Statement state = POS_source.mycon().createStatement();
            
            // Query used to get data from database
            rs = state.executeQuery("select * from inventory_data  where  product_name like '%" + search
                + "%' or description like '%" + search + "%' or product_id like '%" + search + "%'");

            if (search.isEmpty()) {

                // Text displayed if theres nothing in the text box
                search_inventory.setText("Search product name...");
                // Clearing input boxes when a search is made
                pss.clearInventory(product_id, product_name, stock_level, product_price, product_descr);

            } else {

                // Matching results found from database query disaplyed on the inventory table
                while (rs.next()) {

                    Object o[] = {rs.getString("product_id"), rs.getString("product_name"), rs.getString("stock_level"),
                        rs.getString("price"), rs.getString("description")};
                    table.addRow(o);

                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            try {
                logMessage.log("SQLException: " + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }//GEN-LAST:event_search_inventoryKeyReleased

    private void show_toolsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_show_toolsMouseClicked
        
        /* Displaying all tools/machine items on tools/machine table */
                
        try {
            DefaultTableModel table = (DefaultTableModel) tools_table.getModel();
            Statement state = POS_source.mycon().createStatement();
            rs = state.executeQuery("select * from tool_data"); // Query to get tools/machine data from database
            table.setRowCount(0);

            // Adding each tools/machine item in rows into the tools/machine table
            while (rs.next()) {

                Object o[] = {rs.getString("tool_id"), rs.getString("tool_name"), rs.getString("quantity"), 
                    rs.getString("possession_of"), rs.getString("details")};
                table.addRow(o);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            try {
                logMessage.log("SQLException: " + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }        
    }//GEN-LAST:event_show_toolsMouseClicked

    private void tools_tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tools_tableMouseClicked
        
        /* Populating description fields of a tool/machine selected on tools/machine table*/
        
        // Getting data from each column in tools/machine table and storing them in a variable
        int t = tools_table.getSelectedRow();
        String tid = tools_table.getValueAt(t, 0).toString();
        String tn = tools_table.getValueAt(t, 1).toString();
        String tq = tools_table.getValueAt(t, 2).toString();
        String tp = tools_table.getValueAt(t, 3).toString();
        String td = tools_table.getValueAt(t, 4).toString();

        // Setting data on input fields in the tools/machine screen
        tool_id.setText(tid);
        tool_name.setText(tn);
        tool_quantity.setText(tq);
        tool_possession.setText(tp);
        tool_details.setText(td);
        
    }//GEN-LAST:event_tools_tableMouseClicked

    private void refresh_toolsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refresh_toolsMouseClicked
        
        /* Clearing all items in tools/machines screen */
        
        DefaultTableModel table = (DefaultTableModel) tools_table.getModel();
        table.setRowCount(0);
        pss.clearInventory(tool_id, tool_name, tool_quantity, tool_possession, tool_details);
        search_tools.setText("Search Tools & Machines...");
        
    }//GEN-LAST:event_refresh_toolsMouseClicked

    private void search_toolsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_search_toolsKeyPressed
        
        // Text on search bar for tools/machine screen
        if (search_tools.getText().equals("Search Tools & Machines...")) {
            search_tools.setText("");
        }
    }//GEN-LAST:event_search_toolsKeyPressed

    private void search_toolsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_search_toolsKeyReleased
        
        /* Searching tools/machine items using search bar */                       
        
        String search = search_tools.getText(); // Variable used in sql query

        // Getting tools/machine data from values typed in search bar
        try {
            DefaultTableModel table = (DefaultTableModel) tools_table.getModel();
            table.setRowCount(0);
            Statement state = POS_source.mycon().createStatement();
            
            // Query used to get data from database
            rs = state.executeQuery("select * from tool_data  where  tool_name like '%" + search
                + "%' or possession_of like '%" + search + "%' or details like '%" + search + "%' or tool_id like '%" + search + "%'");

            if (search.isEmpty()) {

                // Text displayed if theres nothing in the text box
                search_tools.setText("Search Tools & Machines...");
                // Clearing input boxes when a search is made
                pss.clearTools(tool_id, tool_name, tool_quantity, tool_possession, tool_details);

            } else {

                // Matching results found from database query disaplyed on the tools/machine table
                while (rs.next()) {

                    Object o[] = {rs.getString("tool_id"), rs.getString("tool_name"), rs.getString("quantity"),
                        rs.getString("possession_of"), rs.getString("details")};
                    table.addRow(o);
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            try {
                logMessage.log("SQLException: " + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }    
    }//GEN-LAST:event_search_toolsKeyReleased

    private void add_inventoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_add_inventoryMouseClicked
        
        /* Display form to add new inventroy items */
        Add_inventory ai = new Add_inventory();
        ai.show();

    }//GEN-LAST:event_add_inventoryMouseClicked

    private void add_toolsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_add_toolsMouseClicked
        
        /* Displaying form used to add new tools */
        Add_Tools ad = new Add_Tools();
        ad.show();
        
    }//GEN-LAST:event_add_toolsMouseClicked

    private void select_prodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_prodActionPerformed

        /* Getting product details from database using select action (dropdown box for products) */
        
        String selected_prod = select_prod.getSelectedItem().toString(); // Variable used for query
            
        if (selected_prod.equals("Select a Product")){
                
                // Reset values when no product is in selection
                pss.posDefault(pos_prodID, prodID_search, avail_quantity, avail_stocks, unit_price, stock_status, change, discount, paid);
        } else{
                        
            try {
                
                // Query to get data from database of product selected
                String query = "Select product_id, stock_level, price from inventory_data where product_name=?";
                pst = POS_source.mycon().prepareStatement(query);
                pst.setString(1, selected_prod);
                rs = pst.executeQuery();   

                // Setting all the data gathered about the product in their respected places
                if (rs.next()) {

                    pos_prodID.setText(rs.getString("product_id"));
                    prodID_search.setText(rs.getString("product_id"));
                    avail_quantity.setText(rs.getString("stock_level"));
                    avail_stocks.setText(rs.getString("stock_level"));
                    unit_price.setText(rs.getString("price"));

                }

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                try {
                    logMessage.log("SQLException: " + ex.getMessage());
                } catch (IOException ex1) {
                    Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            
            // Settings stock status information on POS screen
            
            double stock = parseDouble(avail_quantity.getText());
            
            if (stock >= 100){
                stock_status.setText("HIGH");
                stock_status.setForeground(Color.green);
            } else if (stock >= 50){
                stock_status.setText("MEDIUM");
                stock_status.setForeground(Color.blue);
            } else if (stock >= 1){
                stock_status.setText("LOW STOCK");
                stock_status.setForeground(Color.red);
            } else{
                stock_status.setText("NO AVAILABLE STOCK");
                stock_status.setForeground(Color.red);
            }
            
        }
        
    }//GEN-LAST:event_select_prodActionPerformed

    private void paidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paidActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_paidActionPerformed

    private void customerVatNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerVatNumberActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_customerVatNumberActionPerformed

    private void jLabel15MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MouseEntered
        jLabel15.setForeground(Color.blue); 
    }//GEN-LAST:event_jLabel15MouseEntered

    private void jLabel15MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MouseExited
        jLabel15.setForeground(Color.black);
    }//GEN-LAST:event_jLabel15MouseExited

    private void jLabel16MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MouseEntered
        jLabel16.setForeground(Color.blue);
    }//GEN-LAST:event_jLabel16MouseEntered

    private void jLabel16MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MouseExited
        jLabel16.setForeground(Color.black);
    }//GEN-LAST:event_jLabel16MouseExited

    private void jLabel17MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MouseEntered
        jLabel17.setForeground(Color.blue);
    }//GEN-LAST:event_jLabel17MouseEntered

    private void jLabel17MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MouseExited
        jLabel17.setForeground(Color.black);
    }//GEN-LAST:event_jLabel17MouseExited

    private void jLabel18MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseEntered
        jLabel18.setForeground(Color.blue);
    }//GEN-LAST:event_jLabel18MouseEntered

    private void jLabel18MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseExited
        jLabel18.setForeground(Color.black);
    }//GEN-LAST:event_jLabel18MouseExited

    private void jLabel37MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel37MouseEntered
        jLabel37.setForeground(Color.white);
    }//GEN-LAST:event_jLabel37MouseEntered

    private void jLabel37MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel37MouseExited
        jLabel37.setForeground(Color.black);
    }//GEN-LAST:event_jLabel37MouseExited

    private void jLabel3MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseEntered
        jLabel3.setForeground(Color.red);
    }//GEN-LAST:event_jLabel3MouseEntered

    private void jLabel3MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseExited
        jLabel3.setForeground(Color.black);
    }//GEN-LAST:event_jLabel3MouseExited

    private void jLabel37MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel37MouseClicked
        
        /* Logging off from POS application */
        
        // logs user off from POS system without warning if price list has been saved or no price list has been made
        if (saved){
            
            dispose(); // Closes Main Menu form
            log.show(); // Show login form
            
        } else {
            
            // Warning to user when about to log out with out saving current price list
            int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the POS application before saving current "
                    + "price list?\nClicking \"YES\" will close and erase all POS price list data and log you out.", "Exit Without Saving?", 
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (reply == JOptionPane.YES_OPTION) {
                    dispose(); // Closes Main Menu form
                    log.show(); // Show login form
                    
                } else {
                    // POS not closed and user is not logged out
                }
        } 
        
    }//GEN-LAST:event_jLabel37MouseClicked

    private void jLabel33MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel33MouseEntered
        jLabel33.setForeground(Color.blue);
    }//GEN-LAST:event_jLabel33MouseEntered

    private void jLabel33MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel33MouseExited
        jLabel33.setForeground(Color.black);
    }//GEN-LAST:event_jLabel33MouseExited

    private void jLabel33MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel33MouseClicked

        // Searching for products in database using the product ID field          
         try {
             
             // Query used to get data using product id search from the database
             String query = "Select product_id, stock_level, price, product_name from inventory_data where product_id=?";
             pst = POS_source.mycon().prepareStatement(query);
             pst.setString(1, prodID_search.getText());
             rs = pst.executeQuery();   

             // Setting all the data gathered about the product in their respected places
             if (rs.next()) {
                 pos_prodID.setText(rs.getString("product_id"));
                 prodID_search.setText(rs.getString("product_id"));
                 avail_quantity.setText(rs.getString("stock_level"));
                 avail_stocks.setText(rs.getString("stock_level"));
                 unit_price.setText(rs.getString("price"));
                 select_prod.setSelectedItem(rs.getString("product_name"));
             
             }
         
         } catch (SQLException ex) {
                System.out.println(ex.getMessage());
             try {
                 logMessage.log("SQLException: " + ex.getMessage());
             } catch (IOException ex1) {
                 Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
             }
         }
        
    }//GEN-LAST:event_jLabel33MouseClicked

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
                
        // Validation - to prevent values being added to POS table if no product is selected
        if (!(pos_prodID.getText().equals("0"))){
            
            String qtyStr = qty.getValue().toString(); // Getting quantity value and converting to a String
            Double q = Double.valueOf(qtyStr);    // Converting quantity String value to a Double value
            Double unitP = Double.valueOf(unit_price.getText()); // Getting Price of a product and converting it to a Double value
            Double subTotal = q * unitP; // Calculating subTotal by multiplying quantity by unit price
            Double calculateTax = subTotal * 0.14;
            
            String subPrice = String.format("%.2f", subTotal); // Used to round up values to nearest 2 decimal places
            String tax = String.format("%.2f", calculateTax); // Used to round up values to nearest 2 decimal places
                        
            DefaultTableModel table = (DefaultTableModel) posTable.getModel();
            //int r = 1;

            @SuppressWarnings("UseOfObsoleteCollectionType")
            Vector v = new Vector();

            // Adding values to table in POS (goods to be purchased)
            v.add(pos_prodID.getText());
            v.add(select_prod.getSelectedItem().toString());
            v.add(qty.getValue().toString());
            v.add(unit_price.getText());
            v.add(tax);
            v.add(Double.valueOf(subPrice)); // Converted to Double value to make total price calculation easier (Allows for sub total not to be converted to a double before adding values together for total)
            table.addRow(v);
            total_purchase();
            noVAT_purchase();
            
            // Reseting values for discount, paid amount and change when a new item is added to the cart
            discount.setText("0");
            paid.setText("0");
            change.setText("0.00");
            vat.setSelected(false);
            
            // Reseting boolean value saved when the user has added to the price list being worked on
            saved = false;
            
        } else{}
        
        
    }//GEN-LAST:event_jLabel3MouseClicked

    private void jLabel17MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MouseClicked
        
        /* Code to remove a selected item in the POS table */
        
        int rem = posTable.getSelectedRow(); // Get selected row position
        
        // Validation preventing out of bounds error
        if (!(rem < 0)){
            
            DefaultTableModel table = (DefaultTableModel) posTable.getModel();
            table.removeRow(rem);
            total_purchase();
            
            // Reset values to default when a selection is deleted
            pss.posDefault(pos_prodID, prodID_search, avail_quantity, avail_stocks, unit_price, stock_status, change, discount, paid);
            select_prod.setSelectedIndex(0);
            stock_status.setForeground(Color.black);
            
            // Reseting boolean value saved when the user has removed a product from the price list being worked on
            saved = false;
            
        } else{
            // Do Nothing
        }     
     
    }//GEN-LAST:event_jLabel17MouseClicked

    private void posTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_posTableMouseClicked
        
        // Section of code used to set text fields in the POS against a selected item in the table
        
        int t = posTable.getSelectedRow(); // Get selection position in the table
        String productID = posTable.getValueAt(t, 0).toString(); // Gets product ID to search and collect all other values from the database
        
        // Statement to collect data from the database
         try {
                
                Statement state = POS_source.mycon().createStatement();
                rs = state.executeQuery("Select product_id, stock_level, price, product_name from inventory_data where product_id =  '" + productID + "'");   

                if (rs.next()) {

                    pos_prodID.setText(rs.getString("product_id"));
                    prodID_search.setText(rs.getString("product_id"));
                    avail_quantity.setText(rs.getString("stock_level"));
                    avail_stocks.setText(rs.getString("stock_level"));
                    unit_price.setText(rs.getString("price"));
                    select_prod.setSelectedItem(rs.getString("product_name"));

                }

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            try {
                logMessage.log("SQLException: " + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
            }
        
    }//GEN-LAST:event_posTableMouseClicked

    private void closeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseEntered
        close.setForeground(Color.red);
    }//GEN-LAST:event_closeMouseEntered

    private void closeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseExited
        close.setForeground(Color.white);
    }//GEN-LAST:event_closeMouseExited

    private void minimizeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMouseEntered
        minimize.setForeground(Color.red);
    }//GEN-LAST:event_minimizeMouseEntered

    private void minimizeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMouseExited
        minimize.setForeground(Color.white);
    }//GEN-LAST:event_minimizeMouseExited

    private void paidKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_paidKeyPressed
        // Clear text to add amount being paid by customer
        if (paid.getText().equals("0")) {
            paid.setText("");
        }
    }//GEN-LAST:event_paidKeyPressed

    private void paidKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_paidKeyReleased

        // Sets POS values to "0" if nothing has been added to the cart
        if (totalPrice.getText().equals("0.00") || totalPrice.getText().isEmpty()) {
            
            discount.setText("0");
            paid.setText("0");
            change.setText("0.00");

        }
        
        else {

            // Calcuating change fot customer
            Double CustomerChange;
            Double payment = 0.0;
            Double totalCost = Double.valueOf(totalPrice.getText()); // Getting total price value from POS system and converting to a Double variable.
            
            try {
                payment = Double.valueOf(paid.getText()); // Getting amount being paid by customer from POS system and converting to a Double variable.
            } catch (NumberFormatException e){
                try {
                    logMessage.log("NumberFormatException: " + e.getMessage());
                } catch (IOException ex) {
                    Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
                }
            }    
                
            CustomerChange = totalCost - payment; // Calculating the difference (change) from the total cost and amount being paid by customer.
            String cc = String.format("%.2f", Math.abs(CustomerChange)); // Formating change to 2 decimal places ("%.2f") and remvoing minus sign with ( Math.abs() ) method.

            change.setText(String.valueOf(cc)); // Setting change on POS screen

        }

    }//GEN-LAST:event_paidKeyReleased

    private void discountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_discountActionPerformed

    private void discountKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_discountKeyPressed
        
        // Clear text to add discount for customer
        if (discount.getText().equals("0")) {
            discount.setText("");
        }
        
    }//GEN-LAST:event_discountKeyPressed

    private void discountKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_discountKeyReleased
       
        // Sets POS values to "0" if nothing has been added to the cart
        if (totalPrice.getText().equals("0.00")/* || totalPrice.getText().isEmpty()*/) {
            
            discount.setText("0");
            paid.setText("0");
            change.setText("0.00");

        }
        else {

            /* Calculating discount for customer */
            
            
            // Discount when VAT checkbox is active (Discounted price without VAT)
            if(vat.isSelected()){
                
                noVAT_purchase(); // Recalculating total cost without VAT
                Double CustomerDiscount; // Used in calculating difference between total price without vat and discount from POS
                Double POS_Discount = 0.0; // Declared and initialized variable used to get discount value from POS
                
                Double price = Double.valueOf(posNoVatTotal); // Converting global variable posNoVatTotal to double to use for discount calculation
                
                // Try-catch block to catch potential numberFormatException when getting discount from POS
                try{
                    POS_Discount = Double.valueOf(discount.getText()); // Getting amount being discounted for the customer from POS system and converting to a Double variable.
                } catch (NumberFormatException e){
                    try {
                        logMessage.log("NumberFormatException: " + e.getMessage());
                    } catch (IOException ex) {
                        Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                CustomerDiscount = price - POS_Discount; // Calculating the difference (discount) from the total cost and amount being discounted for the customer.
                String cd = String.format("%.2f", Math.abs(CustomerDiscount)); // Formating change to 2 decimal places ("%.2f") and remvoing minus sign with ( Math.abs() ) method.

                totalPrice.setText(String.valueOf(cd)); // Displaying discounted total in POS
                
            }
            
            // Discount when VAT checkbox is inactive (Discounted price with VAT)
            if(!(vat.isSelected())){
                
                total_purchase(); // Recalculating total cost with VAT
                Double CustomerDiscount; // Used in calculating difference between total price without vat and discount from POS
                Double POS_Discount = 0.0; // Declared and initialized variable used to get discount value from POS
                
                Double price = Double.valueOf(totalPrice.getText()); // Getting total price value from POS system and converting to a Double variable
                
                // Try-catch block to catch potential numberFormatException when getting discount from POS
                try{
                    POS_Discount = Double.valueOf(discount.getText()); // Getting amount being discounted for the customer from POS system and converting to a Double variable.
                } catch (NumberFormatException e){
                    try {
                        logMessage.log("NumberFormatException: " + e.getMessage());
                    } catch (IOException ex) {
                        Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                CustomerDiscount = price - POS_Discount; // Calculating the difference (discount) from the total cost and amount being discounted for the customer.
                String cd = String.format("%.2f", Math.abs(CustomerDiscount)); // Formating change to 2 decimal places ("%.2f") and remvoing minus sign with ( Math.abs() ) method.

                totalPrice.setText(String.valueOf(cd)); // Displaying discounted total in POS
                
            }
            
        }
        
    }//GEN-LAST:event_discountKeyReleased

    private void vatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vatActionPerformed
        
        if (vat.isSelected()) {
            
            // Reseting values for discount, paid amount and change when add VAT is checked
            discount.setText("0");
            paid.setText("0");
            change.setText("0.00");
            
           // Recalculating total amount without VAT
           noVAT_purchase();
           totalPrice.setText(posNoVatTotal);
            
        } else {
        
            // Reseting values for discount, paid amount and change when add VAT is checked
            discount.setText("0");
            paid.setText("0");
            change.setText("0.00");
            
            total_purchase(); // Recalculate total without VAT included 
            
        }        
    }//GEN-LAST:event_vatActionPerformed

    private void jLabel18MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseClicked

        /* Clearing all the contents/data in the POS table */
                       
        if (!saved){
            
            // Warning to user when about to clear unsaved price list
            int reply = JOptionPane.showConfirmDialog(null, "Current price list has not been saved. Continue?"
                    + "\nClicking \"YES\" will clear current price listing.", "Refresh", 
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (reply == JOptionPane.YES_OPTION) {
                    
                    DefaultTableModel model = (DefaultTableModel) posTable.getModel();
                    model.setRowCount(0); // Clearing all rows in POS table

                    // Resetting all other values in the POS screen
                    pss.posDefault(pos_prodID, prodID_search, avail_quantity, avail_stocks, unit_price, stock_status, change, discount, paid);
                    stock_status.setForeground(Color.black);
                    select_prod.setSelectedIndex(0);
                    totalPrice.setText("0.00");
                    qty.setValue(1);
                    vat.setSelected(false);
                    
                    saved = true; // Returning boolean value back to default
                    
                } else {
                    // Price list on POS is not cleared/refereshed
                }
        
        // No warning message if price list has been saved
        } else {
            
            DefaultTableModel model = (DefaultTableModel) posTable.getModel();
            model.setRowCount(0); // Clearing all rows in POS table

            // Resetting all other values in the POS screen
            pss.posDefault(pos_prodID, prodID_search, avail_quantity, avail_stocks, unit_price, stock_status, change, discount, paid);
            stock_status.setForeground(Color.black);
            select_prod.setSelectedIndex(0);
            totalPrice.setText("0.00");
            qty.setValue(1);
            vat.setSelected(false);
            
            saved = true; // Returning boolean value back to default
            
        }

    }//GEN-LAST:event_jLabel18MouseClicked

    private void jLabel16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MouseClicked

        if (!(posTable.getRowCount() == 0)){
        
            boolean error = false; // Used to check whether loop found an error with available stock level and purchase quantity
            int numRows = posTable.getRowCount();

            // Check if inventory stock levels are enough to make a sale
            for (int i = 0; i < numRows; i++) {

                // Geeting data from the POS table
                String productID = (String) posTable.getValueAt(i, posTable.getColumnModel().getColumnIndex("Product ID"));
                String PurchaseQty = (String) posTable.getValueAt(i, posTable.getColumnModel().getColumnIndex("Purchase Quantity"));
                String ProductName = (String) posTable.getValueAt(i, posTable.getColumnModel().getColumnIndex("Product Name"));
                double stockLevel = 0.0; // Stock level from the database

                // Statement to collect stock level data from the database
                try {

                    Statement state = POS_source.mycon().createStatement();
                    rs = state.executeQuery("Select stock_level from inventory_data where product_id =  '" + productID + "'");   

                    if (rs.next()) {
                        stockLevel = parseDouble(rs.getString("stock_level"));
                    }


                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                    try {
                        logMessage.log("SQLException: " + ex.getMessage());
                    } catch (IOException ex1) {
                        Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }

                // check if purchase quantity is greater than availavble stock
                if (parseDouble(PurchaseQty) > stockLevel){

                    error = true;
                    JOptionPane.showMessageDialog(null, "Quantity selected for " + ProductName + " (" + PurchaseQty + ") is more than available stock (" 
                            + stockLevel + ").", "Low Stock", JOptionPane.ERROR_MESSAGE);

                    break;
                }
            }

            // check if there was an error with available stock level and purchase quantity 
            // If there is no error the invoice pdf can be genereated
            if (!error) {

                int reply = JOptionPane.showConfirmDialog(null, "Are you sure? Clicking \"YES\" will create the invoice\nand update available stock information.", "Create Invoice", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (reply == JOptionPane.YES_OPTION) {

                    // Creating invoice and updating inventory stock data 
                    total_purchase(); // Recalculate values with VAT before being used to generate PDF invoice.
                    noVAT_purchase(); // Recalculate values without VAT before being used to generate PDF invoice.

                    cusName = customerName.getText();
                    cusTel = customerTel.getText();
                    cusVatNumber = customerVatNumber.getText();

                    posTotal = totalPrice.getText();
                    posDiscount = discount.getText();

                    // Updating stock levels in inventory
                    for (int i = 0; i < numRows; i++) {

                        // Geeting data from the POS table
                        String productID = (String) posTable.getValueAt(i, posTable.getColumnModel().getColumnIndex("Product ID"));
                        String PurchaseQty = (String) posTable.getValueAt(i, posTable.getColumnModel().getColumnIndex("Purchase Quantity"));
                        double stockLevel = 0.0; // Stock level from the database

                        // Statement to collect stock level data from the database
                        try {

                            Statement state = POS_source.mycon().createStatement();
                            rs = state.executeQuery("Select stock_level from inventory_data where product_id =  '" + productID + "'");   

                            if (rs.next()) {
                                stockLevel = parseDouble(rs.getString("stock_level"));
                            }


                        } catch (SQLException ex) {
                            System.out.println(ex.getMessage());
                            try {
                                logMessage.log("SQLException: " + ex.getMessage());
                            } catch (IOException ex1) {
                                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }


                        double newStockLevel = stockLevel - parseDouble(PurchaseQty);

                         try {

                            Statement state = POS_source.mycon().createStatement();
                            state.executeUpdate("UPDATE inventory_data SET stock_level = " + newStockLevel + " WHERE product_id = " + productID);

                         }catch (SQLException ex) {
                            System.out.println("SQL Exception error: " + ex);
                            try {
                                logMessage.log("SQLException: " + ex.getMessage());
                            } catch (IOException ex1) {
                                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                         }   

                    }

                        InvoiceDetails id = new InvoiceDetails(); // Get customer additional customer details with InvoiceDetails class
                        id.show();

                } else {
                    // Invoice generation cancelled
                    JOptionPane.showMessageDialog(null, "Invoice was not created.", "Create Invoice", JOptionPane.INFORMATION_MESSAGE);
                }

            }
            
            
        } 
        // Message to user if sale button is clicked when nothing has been added to the cart    
        else{
            JOptionPane.showMessageDialog(null, "Add a product to cart first.", "Add To Cart", JOptionPane.INFORMATION_MESSAGE);
        }
        
    }//GEN-LAST:event_jLabel16MouseClicked

    private void jLabel15MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MouseClicked

        if (!(posTable.getRowCount() == 0)){
            
            total_purchase(); // Recalculate values with VAT before being used to generate PDF quotation.
            noVAT_purchase(); // Recalculate values without VAT before being used to generate PDF quotation.
        
            cusName = customerName.getText();
            cusTel = customerTel.getText();
            cusVatNumber = customerVatNumber.getText();

            posTotal = totalPrice.getText();
            posDiscount = discount.getText();

            QuotationDetails qd = new QuotationDetails(); // Get customer additional customer details with QuotationDetails class
            qd.show();
            
        // Message to user if quotation button is clicked when nothing has been added to the cart    
        } else{
            JOptionPane.showMessageDialog(null, "Add a product to cart first.", "Add To Cart", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jLabel15MouseClicked

    private void jLabel47MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel47MouseClicked

        if (user_type.equals("admin")){
            // Displaying Administrator settings screen
            general_settings.setVisible(false);
            admin_settings.setVisible(true);
        } else{
            JOptionPane.showMessageDialog(rootPane, "You do not have administrator priviledges\nto access this screen.", "Admin Settings", JOptionPane.INFORMATION_MESSAGE);
        }

        
    }//GEN-LAST:event_jLabel47MouseClicked

    private void settings_idActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settings_idActionPerformed

    }//GEN-LAST:event_settings_idActionPerformed

    private void jLabel46MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel46MouseClicked
    
        // Declaration of fields to be used to create new password
        JTextField pass = new JPasswordField();
        JTextField confirmPass = new JPasswordField();
        Object[] message = {
            "Enter New Password:", pass,
            "Confirm New Passowrd:", confirmPass
        };
        
        try{
        
            Object oldPass = JOptionPane.showInputDialog(null, "Enter old password:", "Password Change", JOptionPane.WARNING_MESSAGE);
            
            // Confirming old password before password change
            if (oldPass.equals(login.pass)){

                int option = JOptionPane.showConfirmDialog(null, message, "Enter New Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (option == JOptionPane.OK_OPTION) {
                    
                    if (pass.getText().equals(confirmPass.getText())) {
                                               
                        try {
                            
                            String query = "UPDATE `users` SET `password`=? WHERE user_id =?";
                            pst = POS_source.mycon().prepareStatement(query);
                            pst.setString(1, confirmPass.getText());
                            pst.setString(2, login.user_id);
                            pst.executeUpdate();
                            
                            JOptionPane.showMessageDialog(null, "Your password has been changed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            login.pass = confirmPass.getText();
                                                       
                        }catch (SQLException ex){
                            System.out.println(ex.getMessage());
                            try {
                                logMessage.log("SQLException: " + ex.getMessage());
                            } catch (IOException ex1) {
                                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                        
                    } else {
                        // Error message if passwords do not match when changing password
                        JOptionPane.showMessageDialog(null, "New passwords do not match.\nPassword not changed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                } else {
                    System.out.println("Password change cancelled.");
                }

            } else{
                // Error message if old password is wrong
                JOptionPane.showMessageDialog(null, "Passoword is incorrect.", "Wrong Password", JOptionPane.ERROR_MESSAGE);

            }
            
        } catch (NullPointerException ex){
            System.out.println(ex.getMessage());
            JOptionPane.showMessageDialog(null, "Error updating password. Please try again later.", "Failed", JOptionPane.ERROR_MESSAGE);
            try {
                logMessage.log("NullPointerException: " + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }             
    }//GEN-LAST:event_jLabel46MouseClicked

    private void jPanel26MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel26MousePressed

        mouseOffset = evt.getPoint();

    }//GEN-LAST:event_jPanel26MousePressed

    private void jPanel26MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel26MouseDragged

        // Setting location of Jframe when mouse is pressed and dragged
        int x = evt.getXOnScreen() - mouseOffset.x;
        int y = evt.getYOnScreen() - mouseOffset.y;
        setLocation(x, y);

    }//GEN-LAST:event_jPanel26MouseDragged

    private void jLabel74MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel74MouseClicked
        companyName_field.setText("");
        companyAddress_field.setText("");
        companyPO_field.setText("");
        companyVat_field.setText("");
        companyTel_field.setText("");
        companyEmail_field.setText("");
    }//GEN-LAST:event_jLabel74MouseClicked

    private void jLabel72MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel72MouseClicked
        // Display settings screen
        home_screen.setVisible(false);
        pos_screen.setVisible(false);
        inventory_screen.setVisible(false);
        tools_screen.setVisible(false);
        settings_screen.setVisible(true); 
        
        // Displaying general settings screen
        general_settings.setVisible(true);
        admin_settings.setVisible(false);
        
        //Resetting user settings fields
        select_user.setSelectedIndex(0);
        userID_search.setText("");
        userID.setText("");
        userPass.setText("");
        userName.setText("");
        userType.setSelectedIndex(0);
        
    }//GEN-LAST:event_jLabel72MouseClicked

    private void jLabel73MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel73MouseClicked
        // Searching for users in database using the user ID field          
         try {
                
                String query = "Select user_id, password, user_name, user_type from users where user_id=?";
                pst = POS_source.mycon().prepareStatement(query);
                pst.setString(1, userID_search.getText());
                rs = pst.executeQuery();   

                if (rs.next()) {

                    userID.setText(rs.getString("user_id"));
                    userPass.setText(rs.getString("password"));
                    userName.setText(rs.getString("user_name"));
                    userType.setSelectedItem(rs.getString("user_type"));
                    select_user.setSelectedItem(rs.getString("user_name"));

                }

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                try {
                    logMessage.log("SQLException: " + ex.getMessage());
                } catch (IOException ex1) {
                    Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
    }//GEN-LAST:event_jLabel73MouseClicked

    private void select_userMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_select_userMouseClicked
        String selected_user = select_user.getSelectedItem().toString(); 
        
            try {
                
                String query = "Select product_id, stock_level, price from inventory_data where product_name=?";
                pst = POS_source.mycon().prepareStatement(query);
                pst.setString(1, selected_user);
                rs = pst.executeQuery();   

                if (rs.next()) {

                    pos_prodID.setText(rs.getString("product_id"));
                    prodID_search.setText(rs.getString("product_id"));
                    avail_quantity.setText(rs.getString("stock_level"));
                    avail_stocks.setText(rs.getString("stock_level"));
                    unit_price.setText(rs.getString("price"));

                }

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                try {
                    logMessage.log("SQLException: " + ex.getMessage());
                } catch (IOException ex1) {
                    Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            
            if (selected_user.equals("Select a Product")){
                
                // Reset values when no product is in selection
                pss.posDefault(pos_prodID, prodID_search, avail_quantity, avail_stocks, unit_price, stock_status, change, discount, paid);
            }
    }//GEN-LAST:event_select_userMouseClicked

    private void select_userActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_userActionPerformed
        
        // Searching for user in database using the user name field
        String selected_user = select_user.getSelectedItem().toString(); 
              
            try {
                
                String query = "Select user_id, password, user_name, user_type from users where user_name=?";
                pst = POS_source.mycon().prepareStatement(query);
                pst.setString(1, selected_user);
                rs = pst.executeQuery();   

                if (rs.next()) {

                    userID.setText(rs.getString("user_id"));
                    userID_search.setText(rs.getString("user_id"));
                    userPass.setText(rs.getString("password"));
                    userName.setText(rs.getString("user_name"));
                    userType.setSelectedItem(rs.getString("user_type")); // issue with usertpe not changing -- look for fix
                    select_user.setSelectedItem(rs.getString("user_name"));

                }

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                try {
                    logMessage.log("SQLException: " + ex.getMessage());
                } catch (IOException ex1) {
                    Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            
            if (selected_user.equals("Select a User")){
                
                // Reset values when no user is in selection
                pss.userDefault(userID, userPass, userName, userType, select_user);
                
        }
    }//GEN-LAST:event_select_userActionPerformed

    private void jLabel71MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel71MouseClicked
        
        // Updating company information
        
        // Updating company information on database
        try {
            if (companyName_field.getText().isEmpty()) {

                JOptionPane.showMessageDialog(null, "Please enter a company name.", "Add Company Name", JOptionPane.ERROR_MESSAGE);
                
            } else {

                String query = "UPDATE company_data SET companyVatNo=?, tel=?, email=?, poBox=?, address=?, name =?, countryTax=?,"
                        + "bank=?, accountNo=?, branch=? WHERE `companyIdentifier` = 'company579' ";
                pst = POS_source.mycon().prepareStatement(query);
                
                Double taxCal = (Double.parseDouble(taxRate_field.getText())/100); // Converting Perecentage Tax value to a double value
                
                pst.setString(1, companyVat_field.getText());
                pst.setString(2, companyTel_field.getText());
                pst.setString(3, companyEmail_field.getText());
                pst.setString(4, companyPO_field.getText());
                pst.setString(5, companyAddress_field.getText());
                pst.setString(6, companyName_field.getText());
                pst.setString(7, String.valueOf(taxCal));
                pst.setString(8, bankName_field.getText());
                pst.setString(9, accountNumber_field.getText());
                pst.setString(10, bankBranch_field.getText());
                pst.executeUpdate();
                
                load_companyData();
                setCompanyInfo();
                JOptionPane.showMessageDialog(rootPane, "Update successful.", "Company Information Update", JOptionPane.INFORMATION_MESSAGE);
                
            }
        } catch (HeadlessException | SQLException ex) {
            System.out.println(ex.getMessage());
            JOptionPane.showMessageDialog(null, "Update unscuccessful. Check all input fields.", "Error", JOptionPane.ERROR_MESSAGE);
            try {
                logMessage.log("HeadlessException | SQLException: " + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (NumberFormatException ex){
            JOptionPane.showMessageDialog(null, "Update unscuccessful. Only input numeric values in \"Tax Rate (%)\" field", "Error", JOptionPane.ERROR_MESSAGE);
            try {
                logMessage.log("NumberFormatException: " + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (IOException ex) {
            Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jLabel71MouseClicked

    private void jLabel69MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel69MouseClicked
        // Deleting a user from the users database
        try {
            
            if (userID.getText().isEmpty() || userName.getText().isEmpty()) {

                JOptionPane.showMessageDialog(rootPane, "Select / Search a user you want to delete.", "Delete User", JOptionPane.INFORMATION_MESSAGE);

            } else {
                
                int result = JOptionPane.showConfirmDialog(rootPane,"Are you sure you want to delete the user \"" 
                        + userName.getText() + "\" with User ID \"" +  userID.getText() + "\"\nfrom the system? This action cannot be reversed.", 
                        "Delete User", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if(result == JOptionPane.YES_OPTION){
                    
                    String query = "DELETE FROM users WHERE user_id =?";
                    pst = POS_source.mycon().prepareStatement(query);
                    pst.setString(1, userID.getText());
                    pst.executeUpdate();
                    
                    JOptionPane.showMessageDialog(rootPane, "Delete successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    pss.userDefault(userID, userPass, userName, userType, select_user);
                    userID_search.setText("");

                }else if (result == JOptionPane.NO_OPTION){
                    // DO NOTHING...
                } 
            }
        } catch (HeadlessException | SQLException x) {
            System.out.println(x.getMessage());
            try {
                logMessage.log("HeadlessException | SQLException: " + x.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }//GEN-LAST:event_jLabel69MouseClicked

    private void jLabel70MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel70MouseClicked

        // Adding new user to database
        try {

            if (userID.getText().isEmpty() || userPass.getText().isEmpty() || userName.getText().isEmpty()) {

                JOptionPane.showMessageDialog(rootPane, "Please make sure all fields under \"User Data\" have been filled.",
                    "Fill In All Fileds", JOptionPane.INFORMATION_MESSAGE);
                     
            } else {
                
                // Preventing error of length with User ID input - User ID maximum text length in databse is 10 characters
                if (userID.getText().length() <= 10){
                    
                    // Preventing administrator from accidentally creating a user with administrator privileges
                    if (userType.getSelectedItem().equals("admin")){

                        int result = JOptionPane.showConfirmDialog(rootPane, "You are going to create \"" + userName.getText() + "\" as an administrator."
                                + "\nAre you sure?","Warning", JOptionPane.WARNING_MESSAGE);

                        if (result == JOptionPane.YES_OPTION){

                            // SQL Query to add a user to the database
                            String query = "INSERT INTO `users`(`user_id`, `password`, `user_name`, `user_type`) VALUES (?,?,?,?)";
                            pst = POS_source.mycon().prepareStatement(query);
                            pst.setString(1, userID.getText());
                            pst.setString(2, userPass.getText());
                            pst.setString(3, userName.getText());
                            pst.setString(4, userType.getSelectedItem().toString());
                            pst.executeUpdate();

                            // Display message of successful creation of new user.
                            JOptionPane.showMessageDialog(rootPane, "New user \"" + userName.getText() + "\" has been created.", "Success", 
                                    JOptionPane.INFORMATION_MESSAGE);
                            pss.userDefault(userID, userPass, userName, userType, select_user); // Clear all input fileds
                            userID_search.setText("");

                        }
                    } else{

                        // Creating a user not to have administrator privileges
                         // SQL Query to add a user to the database
                        String query = "INSERT INTO `users`(`user_id`, `password`, `user_name`, `user_type`) VALUES (?,?,?,?)";
                        pst = POS_source.mycon().prepareStatement(query);
                        pst.setString(1, userID.getText());
                        pst.setString(2, userPass.getText());
                        pst.setString(3, userName.getText());
                        pst.setString(4, userType.getSelectedItem().toString());
                        pst.executeUpdate();

                        // Display message of successful creation of new user.
                        JOptionPane.showMessageDialog(rootPane, "New user \"" + userName.getText() + "\" has been created.", "Success", 
                                JOptionPane.INFORMATION_MESSAGE);
                        pss.userDefault(userID, userPass, userName, userType, select_user); // Clear all input fileds
                        userID_search.setText("");

                    }
                    
                } else{
                    JOptionPane.showMessageDialog(rootPane, "Please reduce the length of the User ID.", "User ID Too Long", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
                
            }

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(null, "It may be that a user with the User ID \"" + userID.getText() + "\" exists in the system already."
                    + "\nPlease use a different User ID on the \"User ID\" field. If the problem persists\ncheck your connection "
                    + "to your server else, contact the developer.",
                "Input Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Error" + ex.getMessage());
            try {
                logMessage.log("SQLException" + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }//GEN-LAST:event_jLabel70MouseClicked

    private void jLabel79MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel79MouseClicked
         // Updating user data on database
        
        try {
            
            if (userID_search.getText().isEmpty() || select_user.getSelectedItem().equals("Select a User")) {

                JOptionPane.showMessageDialog(rootPane, "Select / Search a user you want to update.", "Update User", JOptionPane.INFORMATION_MESSAGE);

            }
            
            else if (userID.getText().isEmpty() || userPass.getText().isEmpty() || userName.getText().isEmpty()) {

                JOptionPane.showMessageDialog(rootPane, "Please make sure all fields under \"User Data\" have been filled.",
                    "Fill In All Fileds", JOptionPane.INFORMATION_MESSAGE);
                     
            } else {
                
                // Preventing error of length with User ID input - User ID maximum text length in databse is 10 characters
                if (userID.getText().length() <= 10){
                    
                    // Preventing administrator from accidentally setting a user to have administrator privileges
                    if (userType.getSelectedItem().equals("admin")){

                        int result = JOptionPane.showConfirmDialog(rootPane, "You are going to set \"" + userName.getText() + "\" as an administrator."
                                + "\nAre you sure?","Warning", JOptionPane.WARNING_MESSAGE);

                        if (result == JOptionPane.YES_OPTION){

                            // SQL Query to update user data in the database
                            String query = "UPDATE `users` SET `user_id`=?, `password`=?, `user_name`=?, `user_type`=? WHERE user_id = \'" + userID_search.getText() + "\'";
                            pst = POS_source.mycon().prepareStatement(query);
                            pst.setString(1, userID.getText());
                            pst.setString(2, userPass.getText());
                            pst.setString(3, userName.getText());
                            pst.setString(4, userType.getSelectedItem().toString());
                            pst.executeUpdate();

                            // Display message of successful creation of new user.
                            JOptionPane.showMessageDialog(rootPane, "User \"" + userName.getText() + "\" data has been updated.", "Success", 
                                    JOptionPane.INFORMATION_MESSAGE);
                            pss.userDefault(userID, userPass, userName, userType, select_user); // Clear all input fileds
                            userID_search.setText("");

                        }
                    } else{

                        // Updating a user with no administrator privileges
                        // SQL Query to update user data in the database
                        String query = "UPDATE `users` SET `user_id`=?, `password`=?, `user_name`=?, `user_type`=? WHERE user_id = \'" + userID_search.getText() + "\'";
                        pst = POS_source.mycon().prepareStatement(query);
                        pst.setString(1, userID.getText());
                        pst.setString(2, userPass.getText());
                        pst.setString(3, userName.getText());
                        pst.setString(4, userType.getSelectedItem().toString());
                        pst.executeUpdate();

                        // Display message of successful creation of new user.
                        JOptionPane.showMessageDialog(rootPane, "User \"" + userName.getText() + "\" data has been updated.", "Success", 
                                JOptionPane.INFORMATION_MESSAGE);
                        pss.userDefault(userID, userPass, userName, userType, select_user); // Clear all input fileds
                        userID_search.setText("");

                    }
                    
                } else{
                    JOptionPane.showMessageDialog(rootPane, "Please reduce the length of the User ID.", "User ID Too Long", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
                
            }

        } catch (SQLException ex) {

//            JOptionPane.showMessageDialog(null, "It may be that a user with the User ID \"" + userID.getText() + "\" exists in the system already."
//                    + "\nPlease use a different User ID on the \"User ID\" field. If the problem persists\ncheck your connection "
//                    + "to your server else, contact the developer.",
//                "Input Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Error" + ex.getMessage());
            try {
                logMessage.log("SQLException" + ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }//GEN-LAST:event_jLabel79MouseClicked

    private void home_screenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_home_screenMouseEntered
        // Higlighting home screen as the active screen
        home.setForeground(Color.orange);
        pos.setForeground(Color.white);
        inventory.setForeground(Color.white);
        tools.setForeground(Color.white);
        settings.setForeground(Color.white);
    }//GEN-LAST:event_home_screenMouseEntered

    private void pos_screenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pos_screenMouseEntered
        // Higlighting pos screen as the active screen
        home.setForeground(Color.white);
        pos.setForeground(Color.orange);
        inventory.setForeground(Color.white);
        tools.setForeground(Color.white);
        settings.setForeground(Color.white);
    }//GEN-LAST:event_pos_screenMouseEntered

    private void inventory_screenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inventory_screenMouseEntered
        // Higlighting inventory screen as the active screen
        home.setForeground(Color.white);
        pos.setForeground(Color.white);
        inventory.setForeground(Color.orange);
        tools.setForeground(Color.white);
        settings.setForeground(Color.white);
    }//GEN-LAST:event_inventory_screenMouseEntered

    private void tools_screenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tools_screenMouseEntered
        // Higlighting tools screen as the active screen
        home.setForeground(Color.white);
        pos.setForeground(Color.white);
        inventory.setForeground(Color.white);
        tools.setForeground(Color.orange);
        settings.setForeground(Color.white);
    }//GEN-LAST:event_tools_screenMouseEntered

    private void settings_screenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settings_screenMouseEntered
        // Higlighting settings screen as the active screen
        home.setForeground(Color.white);
        pos.setForeground(Color.white);
        inventory.setForeground(Color.white);
        tools.setForeground(Color.white);
        settings.setForeground(Color.orange);
    }//GEN-LAST:event_settings_screenMouseEntered

    private void jLabel46MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel46MouseEntered
        // Highlighting button when mouse on top of button
        jLabel46.setForeground(Color.black);
    }//GEN-LAST:event_jLabel46MouseEntered

    private void jLabel46MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel46MouseExited
        // Removing highlight on button when mouse is not on top of button
        jLabel46.setForeground(Color.white);
    }//GEN-LAST:event_jLabel46MouseExited

    private void jLabel47MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel47MouseEntered
        // Highlighting button when mouse on top of button
        jLabel47.setForeground(Color.black);
    }//GEN-LAST:event_jLabel47MouseEntered

    private void jLabel47MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel47MouseExited
        // Removing highlight on button when mouse is not on top of button
        jLabel47.setForeground(Color.white);
    }//GEN-LAST:event_jLabel47MouseExited

    private void jLabel73MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel73MouseEntered
        // Highlighting button when mouse on top of button
        jLabel73.setForeground(Color.black);
    }//GEN-LAST:event_jLabel73MouseEntered

    private void jLabel73MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel73MouseExited
        // Removing highlight on button when mouse is not on top of button
        jLabel73.setForeground(Color.white);
    }//GEN-LAST:event_jLabel73MouseExited

    private void jLabel70MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel70MouseEntered
        // Highlighting button when mouse on top of button
        jLabel70.setForeground(Color.black);
    }//GEN-LAST:event_jLabel70MouseEntered

    private void jLabel70MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel70MouseExited
        // Removing highlight on button when mouse is not on top of button
        jLabel70.setForeground(Color.white);
    }//GEN-LAST:event_jLabel70MouseExited

    private void jLabel79MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel79MouseEntered
        // Highlighting button when mouse on top of button
        jLabel79.setForeground(Color.black);
    }//GEN-LAST:event_jLabel79MouseEntered

    private void jLabel79MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel79MouseExited
        // Removing highlight on button when mouse is not on top of button
        jLabel79.setForeground(Color.white);
    }//GEN-LAST:event_jLabel79MouseExited

    private void jLabel69MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel69MouseEntered
        // Highlighting button when mouse on top of button
        jLabel69.setForeground(Color.black);
    }//GEN-LAST:event_jLabel69MouseEntered

    private void jLabel69MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel69MouseExited
        // Removing highlight on button when mouse is not on top of button
        jLabel69.setForeground(Color.white);
    }//GEN-LAST:event_jLabel69MouseExited

    private void jLabel71MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel71MouseEntered
        // Highlighting button when mouse on top of button
        jLabel71.setForeground(Color.black);
    }//GEN-LAST:event_jLabel71MouseEntered

    private void jLabel71MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel71MouseExited
        // Removing highlight on button when mouse is not on top of button
        jLabel71.setForeground(Color.white);
    }//GEN-LAST:event_jLabel71MouseExited

    private void jLabel74MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel74MouseEntered
        // Highlighting button when mouse on top of button
        jLabel74.setForeground(Color.black);
    }//GEN-LAST:event_jLabel74MouseEntered

    private void jLabel74MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel74MouseExited
        // Removing highlight on button when mouse is not on top of button
        jLabel74.setForeground(Color.white);
    }//GEN-LAST:event_jLabel74MouseExited

    private void jLabel53MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel53MouseClicked

        /* Method to load data from a file to JTable */

                
        /* initiates import of new file process without warning if the price list has been saved or no price list has been made */
        
        if (saved){
            
            JFileChooser fileChooser =  new JFileChooser(); // Creating a file chooser object
            int result = fileChooser.showOpenDialog(null);
            
            if (result == JFileChooser.APPROVE_OPTION){

                // Selecting file
                File file = fileChooser.getSelectedFile();

                try {

                    List<String[]> data;
                    try (
                        // Load CSV data into a list of string arrays
                        CSVReader reader = new CSVReader(new FileReader(file))) {
                        data = reader.readAll();
                    }

                    // Convert the list of string arrays to a 2D array for the JTable
                    String[][] dataArray = new String[data.size()][];
                    data.toArray(dataArray);

                    // Setting header information on posTable
                    String[] header = {"Product ID", "Product Name", "Purchase Quantity", "Unit Price", "Tax Amount", "Sub Total"};

                    // Create a DefaultTableModel with the data and headers
                    DefaultTableModel model = new DefaultTableModel(dataArray, header);
                    posTable.setModel(model);

                } catch (IOException e) {
                    try {
                        logMessage.log("IOException: " + e.getMessage());
                    } catch (IOException ex) {
                        Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (CsvException ex) {
                    Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
                    try {
                        logMessage.log("CsvException: " + ex.getMessage());
                    } catch (IOException ex1) {
                        Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
            
            total_purchase(); // Calculating total from imported file
        
        /* Warns user of importing new file when the current price list on the screen has not been saved */
            
        } else {
            
            // Warning to user when about to import a new file without saving current price list
            int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to import a new file before saving current price list?"
                    + "\nClicking \"YES\" will erase exisitng POS price list on the screen after you import.", "Import Without Saving?", 
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (reply == JOptionPane.YES_OPTION) {
                
                JFileChooser fileChooser =  new JFileChooser(); // Creating a file chooser object
                int result = fileChooser.showOpenDialog(null);
                
                if (result == JFileChooser.APPROVE_OPTION){

                    // Selecting file
                    File file = fileChooser.getSelectedFile();

                    try {

                        List<String[]> data;
                        try (
                            // Load CSV data into a list of string arrays
                            CSVReader reader = new CSVReader(new FileReader(file))) {
                            data = reader.readAll();
                        }

                        // Convert the list of string arrays to a 2D array for the JTable
                        String[][] dataArray = new String[data.size()][];
                        data.toArray(dataArray);

                        // Setting header information on posTable
                        String[] header = {"Product ID", "Product Name", "Purchase Quantity", "Unit Price", "Tax Amount", "Sub Total"};

                        // Create a DefaultTableModel with the data and headers
                        DefaultTableModel model = new DefaultTableModel(dataArray, header);
                        posTable.setModel(model);

                    } catch (IOException e) {
                        try {
                            logMessage.log("IOException: " + e.getMessage());
                        } catch (IOException ex) {
                            Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (CsvException ex) {
                        Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
                        try {
                            logMessage.log("CsvException: " + ex.getMessage());
                        } catch (IOException ex1) {
                            Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }
                }
                
                total_purchase(); // Calculating total from imported file
                    
                } else {
                    // File not imported
                }
        }
        
    }//GEN-LAST:event_jLabel53MouseClicked

    private void jLabel80MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel80MouseClicked

        /* Method to save data from POS table to a file */

        if (posTable.getRowCount() == 0){
            // Displays message to user that there is no data to be saved
            JOptionPane.showMessageDialog(rootPane, "Add purchase items before you can save.", "Add to Cart", JOptionPane.INFORMATION_MESSAGE);
        } else{

            // Saving data from POS table to a file
            JFileChooser fileChooser =  new JFileChooser();
            int result = fileChooser.showSaveDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {

                try {
                    File selectedFile = fileChooser.getSelectedFile();

                    // Warning message to user when about to save a file with the an already existing file name
                    if (selectedFile.exists()) {
                        int option = JOptionPane.showConfirmDialog(null, "The file already exists. Do you want to overwrite it?",
                            "File Exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); // Warning message
                        if (option != JOptionPane.YES_OPTION) {
                            return; // User chose not to overwrite, so returns without saving.
                        }
                    }

                    // Saving data from POS Table to a file
                    try (FileWriter writer = new FileWriter(selectedFile)) {
                        for (int i = 0; i < posTable.getRowCount(); i++){ // getting number of rows
                            for (int j = 0; j < posTable.getColumnCount(); j++){ // getting number of columns
                                writer.write(posTable.getValueAt(i,j).toString()); // writing data to file
                                if (j < posTable.getColumnCount() - 1){
                                    writer.write(","); // delimitter to divide columns in pos table
                                }
                            }
                            writer.write("\n"); // used to go to newline for the next row in the pos Table containig data
                        }
                        writer.close();
                    }
                    
                    // Displays file saved message to user
                    JOptionPane.showMessageDialog(rootPane, "File saved Successfully.", "File Saved.",JOptionPane.INFORMATION_MESSAGE);
                    
                    // Updating Boolean value saved for when the user has saved the price list being worked on
                    saved = true;

                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                    try {
                        logMessage.log("IOException: " + e.getMessage());
                    } catch (IOException ex) {
                        Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }//GEN-LAST:event_jLabel80MouseClicked

    private void jLabel53MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel53MouseEntered
        jLabel53.setForeground(Color.blue);
    }//GEN-LAST:event_jLabel53MouseEntered

    private void jLabel53MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel53MouseExited
        jLabel53.setForeground(Color.white);
    }//GEN-LAST:event_jLabel53MouseExited

    private void jLabel80MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel80MouseEntered
        jLabel80.setForeground(Color.blue);
    }//GEN-LAST:event_jLabel80MouseEntered

    private void jLabel80MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel80MouseExited
        jLabel80.setForeground(Color.white);
    }//GEN-LAST:event_jLabel80MouseExited

    private void prodID_searchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_prodID_searchKeyPressed

        /* Action for searching database performed using the enter key */
        
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            
            // Searching for products in database using the product ID field          
            try {

                // Query used to get data using product id search from the database
                String query = "Select product_id, stock_level, price, product_name from inventory_data where product_id=?";
                pst = POS_source.mycon().prepareStatement(query);
                pst.setString(1, prodID_search.getText());
                rs = pst.executeQuery();   

                // Setting all the data gathered about the product in their respected places
                if (rs.next()) {
                    pos_prodID.setText(rs.getString("product_id"));
                    prodID_search.setText(rs.getString("product_id"));
                    avail_quantity.setText(rs.getString("stock_level"));
                    avail_stocks.setText(rs.getString("stock_level"));
                    unit_price.setText(rs.getString("price"));
                    select_prod.setSelectedItem(rs.getString("product_name"));

                }
         
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                try {
                    logMessage.log("SQLException: " + ex.getMessage());
                } catch (IOException ex1) {
                    Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
         
        }
        
    }//GEN-LAST:event_prodID_searchKeyPressed

    private void userID_searchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_userID_searchKeyPressed
        // Searching for users in database using the user ID field using Enter key/button         
         try {
                
                String query = "Select user_id, password, user_name, user_type from users where user_id=?";
                pst = POS_source.mycon().prepareStatement(query);
                pst.setString(1, userID_search.getText());
                rs = pst.executeQuery();   

                if (rs.next()) {

                    userID.setText(rs.getString("user_id"));
                    userPass.setText(rs.getString("password"));
                    userName.setText(rs.getString("user_name"));
                    userType.setSelectedItem(rs.getString("user_type"));
                    select_user.setSelectedItem(rs.getString("user_name"));

                }

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                try {
                    logMessage.log("SQLException: " + ex.getMessage());
                } catch (IOException ex1) {
                    Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
    }//GEN-LAST:event_userID_searchKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main_menu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new Main_menu().setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(Main_menu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField accountNumber_field;
    private javax.swing.JLabel add_inventory;
    private javax.swing.JLabel add_tools;
    private javax.swing.JPanel admin_settings;
    private javax.swing.JLabel avail_quantity;
    private javax.swing.JLabel avail_stocks;
    private javax.swing.JTextField bankBranch_field;
    private javax.swing.JTextField bankName_field;
    private javax.swing.JLabel change;
    private javax.swing.JLabel close;
    private javax.swing.JTextField companyAddress_field;
    private javax.swing.JTextField companyEmail_field;
    private javax.swing.JTextField companyName_field;
    private javax.swing.JTextField companyPO_field;
    private javax.swing.JTextField companyTel_field;
    private javax.swing.JTextField companyVat_field;
    private javax.swing.JTextField customerName;
    private javax.swing.JTextField customerTel;
    private javax.swing.JTextField customerVatNumber;
    private javax.swing.JLabel delete_inventory;
    private javax.swing.JLabel delete_tools;
    private javax.swing.JTextField discount;
    private javax.swing.JPanel general_settings;
    private javax.swing.JLabel home;
    private javax.swing.JLabel home_date;
    private javax.swing.JPanel home_screen;
    private javax.swing.JLabel home_time;
    private javax.swing.JLabel inventory;
    private javax.swing.JPanel inventory_panel;
    private javax.swing.JPanel inventory_screen;
    private javax.swing.JTable inventory_table;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel loggedIn;
    private javax.swing.JLabel logo3;
    private javax.swing.JLabel logo4;
    private javax.swing.JLabel logo5;
    private javax.swing.JLabel logo6;
    private javax.swing.JLabel minimize;
    private javax.swing.JPanel navigationBar;
    private javax.swing.JTextField paid;
    private javax.swing.JLabel pos;
    private static javax.swing.JTable posTable;
    private javax.swing.JLabel pos_date;
    private javax.swing.JPanel pos_panel;
    private javax.swing.JLabel pos_prodID;
    private javax.swing.JPanel pos_screen;
    private javax.swing.JLabel pos_time;
    private javax.swing.JTextField prodID_search;
    private javax.swing.JTextArea product_descr;
    private javax.swing.JTextField product_id;
    private javax.swing.JTextField product_name;
    private javax.swing.JTextField product_price;
    private javax.swing.JSpinner qty;
    private javax.swing.JLabel refresh_inventory;
    private javax.swing.JLabel refresh_tools;
    private javax.swing.JTextField search_inventory;
    private javax.swing.JTextField search_tools;
    private javax.swing.JComboBox<String> select_prod;
    private javax.swing.JComboBox<String> select_user;
    private javax.swing.JLabel settings;
    private javax.swing.JTextField settings_id;
    private javax.swing.JPanel settings_panel;
    private javax.swing.JPanel settings_screen;
    private javax.swing.JTextField settings_usrName;
    private javax.swing.JLabel showAll_inventory;
    private javax.swing.JLabel show_tools;
    private javax.swing.JTextField stock_level;
    private javax.swing.JLabel stock_status;
    private javax.swing.JTextField taxRate_field;
    private javax.swing.JTextArea tool_details;
    private javax.swing.JTextField tool_id;
    private javax.swing.JTextField tool_name;
    private javax.swing.JTextField tool_possession;
    private javax.swing.JTextField tool_quantity;
    private javax.swing.JLabel tools;
    private javax.swing.JPanel tools_panel;
    private javax.swing.JPanel tools_screen;
    private javax.swing.JTable tools_table;
    private javax.swing.JLabel totalPrice;
    private javax.swing.JLabel unit_price;
    private javax.swing.JLabel update_inventory;
    private javax.swing.JLabel update_tools;
    private javax.swing.JTextField userID;
    private javax.swing.JTextField userID_search;
    private javax.swing.JTextField userName;
    private javax.swing.JPasswordField userPass;
    private javax.swing.JComboBox<String> userType;
    private javax.swing.JCheckBox vat;
    // End of variables declaration//GEN-END:variables

}
