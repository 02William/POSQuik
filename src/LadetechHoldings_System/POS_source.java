
package LadetechHoldings_System;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author WILLIAM OMOLADE
 */
public class POS_source {
    
    public static Connection mycon() {
        
        Connection con = null;
        
        try {
            
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ladetech_pos_db", "root", ""); // Local Test Connection
//            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ladetech_pos_db", "root", "23|@detechP@@s"); // Local Test Connection on site
//            con = DriverManager.getConnection("jdbc:mysql://---:--/ladetech_pos_db", "ladetech_usr", "23|@detechP@@s"); // Remote Connection (Not Working)
            return con;

        } catch (ClassNotFoundException | SQLException x) {
            
            System.out.println(x);
            JOptionPane.showMessageDialog(null, "No connection to database. Check server connection.", "Database Error!", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    public void clearInventory(JTextField text1, JTextField text2, JTextField text3, JTextField text4, JTextArea text5) {
        
        text1.setText("");
        text2.setText("");
        text3.setText("");
        text4.setText("");
        text5.setText("");

    }
    
    public void clearTools(JTextField text1, JTextField text2, JTextField text3, JTextField text4, JTextArea text5) {
        
        text1.setText("");
        text2.setText("");
        text3.setText("");
        text4.setText("");
        text5.setText("");

    }
    
    public void clearAddInventory(JTextField text1, JTextField text2, JTextField text3, JTextArea text4) {
        
        text1.setText("");
        text2.setText("");
        text3.setText("");
        text4.setText("");

    }
    
    public void clearAddTools(JTextField text1, JTextField text2, JTextField text3, JTextArea text4) {
        
        text1.setText("");
        text2.setText("");
        text3.setText("");
        text4.setText("");

    }
    
    public void posDefault(JLabel text1, JTextField text2, JLabel text3, JLabel text4, JLabel text5, JLabel text6, JLabel text7, JTextField text8, JTextField text9) {
        
        text1.setText("0");
        text2.setText("");
        text3.setText("0");
        text4.setText("0");
        text5.setText("0.0");
        text6.setText("");
        text7.setText("0.00");
        text8.setText("0");
        text9.setText("0");
        
    }
    
    public void userDefault(JTextField text1, JTextField text2, JTextField text3, JComboBox text4, JComboBox text5) {
        
        text1.setText("");
        text2.setText("");
        text3.setText("");
        text4.setSelectedIndex(0);
        text5.setSelectedIndex(0);
        
    }
    
}
