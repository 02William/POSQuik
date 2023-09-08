package LadetechHoldings_System;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author WILLIAM OMOLADE
 */
public class GeneratePDF {

    static Date d = new Date();
    static SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy");
    static String date = s.format(d);
        
    static double taxRate = Main_menu.countryTax;
    static int invoiceNo = Main_menu.invoiceNo;
    static int quotationNo = Main_menu.quotationNo;
    
    // Genereating invoice PDF form
    public static void createInvoicePDF(DefaultTableModel model, String customer, String address, String vat, String tel, 
            String email, String total, String noVatTotal, String discount ) throws SQLException {
        
        // Path where invoice will be stored after being generated
        String desktopPath  = System.getProperty("user.home") + "/Desktop"; // Getting path to desktop
        String invoicePath = desktopPath  + "/Invoice's\\invoice -" + " " + String.format("%05d", invoiceNo) + ".pdf";
        
        // Calculating totaln tax and amount due
        Double tempTax = Double.parseDouble(noVatTotal) * taxRate;
        Double tempAmountDue = (Double.valueOf(noVatTotal) + tempTax) - Double.parseDouble(discount);
        
        // Changing format of values to currency format style
        String pdfDiscount = String.format("%,.2f", Double.valueOf(discount));
        String pdfTax = String.format("%,.2f", tempTax);   
        String pdfAmountDue = String.format("%,.2f", tempAmountDue);
        noVatTotal = String.format("%,.2f", Double.valueOf(noVatTotal));
        total = String.format("%,.2f", Double.valueOf(total));
        
        try {
            
            PdfWriter pdfWriter = null;
            pdfWriter = new PdfWriter(invoicePath);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            
            try (Document document = new Document(pdfDocument)) {
                pdfDocument.setDefaultPageSize(PageSize.A4);
                
                float sixcol = 190f;
                float twocol = 285f;
                float twocol150 = twocol + 150f;
                float twocolumnWidth[] = {twocol150, twocol};
                float sixcolumnWidth [] = {(sixcol/3) + 20, sixcol - 25, sixcol/3, (sixcol/3) + 10, (sixcol/4) + 10, sixcol - 105};
                float colWidthSign [] = {sixcol, sixcol, sixcol};
                float colDetails [] = {sixcol + 75, sixcol - 30, sixcol - 30};
                float fullWidth[] = {sixcol * 6};
                Paragraph onsp = new Paragraph("\n");  // Used for spacing
                
                Table dataTable = new Table(sixcolumnWidth);
                
                // The header on the PDF (on the right top corner)
                Table header = new Table(twocolumnWidth);
                header.addCell(new Cell().add("Invoice").setBold().setFontSize(25f).setBorder(Border.NO_BORDER));
                
                // The header on the PDF (on the left top corner) - Invoice details and salesperson
                Table nestedTable = new Table(new float[]{twocol/2, twocol/2});
                nestedTable.addCell(new Cell().add("Invoice No: ").setBold());
                nestedTable.addCell(new Cell().add(String.format("%05d", invoiceNo)));
                nestedTable.addCell(new Cell().add("Invoice Date: ").setBold());
                nestedTable.addCell(new Cell().add(date)); // Date for invoice
                nestedTable.addCell(new Cell().add("Salesperson: ").setBold());
                nestedTable.addCell(new Cell().add(login.user_name)); // Gets username for user logged in
                
                // First grey border after header information
                Border gb = new SolidBorder(com.itextpdf.kernel.color.Color.GRAY, 0.3f);
                Table divider = new Table(fullWidth);
                divider.setBorder(gb);
                
                // Heading information for company(Ladetech) and customer
                Table afterHeader = new Table(twocolumnWidth);
                afterHeader.addCell(new Cell().add("").setFontSize(13f).setBold().setBorder(Border.NO_BORDER));
                afterHeader.addCell(new Cell().add(Main_menu.companyName).setFontSize(13f).setBold().setBorder(Border.NO_BORDER));
                
                // Data for company(Ladetech) and customer
                Table afterHeader2 = new Table(twocolumnWidth);
                afterHeader2.addCell(new Cell().add("Customer").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("Details").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add(customer).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add(Main_menu.address + "\n" + Main_menu.poBox).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("Address: " + address).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("").setFontSize(10f).setBorder(Border.NO_BORDER)); // Space on information side for the company (Ladetech)
                afterHeader2.addCell(new Cell().add("VAT: " + vat).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("VAT: " + Main_menu.companyVatNo).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("TEL: " + tel).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("TEL: " + Main_menu.tel).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("Email: " + email).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("Email: " + Main_menu.email).setFontSize(10f).setBorder(Border.NO_BORDER));
                
                // Creating table heading
                Table purchaseTable = new Table(sixcolumnWidth);
                purchaseTable.setBackgroundColor(com.itextpdf.kernel.color.Color.GRAY, 0.5f);
                purchaseTable.addCell(new Cell().add("Product ID").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                purchaseTable.addCell(new Cell().add("Product Name").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                purchaseTable.addCell(new Cell().add("Quantity").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                purchaseTable.addCell(new Cell().add("Unit Price").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                purchaseTable.addCell(new Cell().add("Tax").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                purchaseTable.addCell(new Cell().add("Sub Total").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                
                // Iterates through the data in the POS and adds it to the product table pdf
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        String value = String.valueOf(model.getValueAt(i, j));
                        dataTable.addCell(new Cell().add(value).setFontSize(10f).setBorder(Border.NO_BORDER));
                    }
                }
                
                // Adding Name, Sign and Date at the bottom of the PDF
                Table dashSpace = new Table(colWidthSign);
                dashSpace.addCell(new Cell().add("Name: ___________________________").setFontSize(8f).setBorder(Border.NO_BORDER));
                dashSpace.addCell(new Cell().add("Sign: ___________________________").setFontSize(8f).setBorder(Border.NO_BORDER));
                dashSpace.addCell(new Cell().add("Date: ___________________________").setFontSize(8f).setBorder(Border.NO_BORDER));
                //dashSpace.setFixedPosition(page.getPageSize().getLeft() + 25f, page.getPageSize().getBottom() + 25f, page.getPageSize().getWidth()); // Adds table data to the bottom of the PDF
                
                Table detailsCol = new Table(colDetails);
                detailsCol.setBorder(Border.NO_BORDER); // Removing default border to add custom border
                detailsCol.setBorderBottom(new SolidBorder(com.itextpdf.kernel.color.Color.GRAY, 2)); // Adding borders around the cells
                detailsCol.setBorderTop(new SolidBorder(com.itextpdf.kernel.color.Color.GRAY, 2)); // Adding borders around the cells
                detailsCol.setBorderRight(new SolidBorder(com.itextpdf.kernel.color.Color.GRAY, 2)); // Adding borders around the cells
                detailsCol.setBorderLeft(new SolidBorder(com.itextpdf.kernel.color.Color.GRAY, 2)); // Adding borders around the cells
                detailsCol.addCell(new Cell().add("").setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("").setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Total(Excl) P:       " + noVatTotal ).setFontSize(9f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Terms & Conditions").setBold().setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Bank Details").setBold().setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Tax P:                  " + pdfTax).setFontSize(9f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("1. Goods not returned within 48 hrs, from the date of invoice,\nwill be subject to 15% handling fees.")
                        .setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("FNB Bank" + "\nAccount Number: " + "62079862051").setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Total(Inc) P:         " + total + "\nDiscount P:          " + pdfDiscount).setFontSize(9f)
                        .setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("2. 70% deposit along with the order, balance due upon arrival\nat " + Main_menu.companyName + ".\n"
                        + "3. Cash refunds attract 3% handling fee.\n"
                        + "4. All quoations are valid for ONLY 7 days after quotation date.\n"
                        + "5. All board cut to sizes according to customers approved cutting\nlist are not refundable or replaceable.\n"
                        + "6. All Terms & Conditions of " + Main_menu.companyName + ",\nremain valid & applicable.")
                        .setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("First Place Branch").setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Amount Due P:    " + pdfAmountDue).setBold().setFontSize(9f).setBorder(Border.NO_BORDER));
                //detailsCol.setFixedPosition(page.getPageSize().getLeft() + 25f, page.getPageSize().getBottom() + 50f, page.getPageSize().getWidth() - 53);
                
                // Adding Elements cretaed into the PDF file
                header.addCell(new Cell().add(nestedTable).setFontSize(10f).setBorder(Border.NO_BORDER)); // Adds invoice information on PDF
                document.add(header); // Adds "Invoice" text to PDF
                document.add(onsp); // Adds space between header and divider to PDF
                document.add(divider); // Adds first grey divider to PDF
                document.add(onsp); // Adds space between after divider to PDF
                document.add(afterHeader); // Adds heading information for company(Ladetech) and customer to PDF
                document.add(onsp); // Adds space between after heading information for company(Ladetech) and customer to PDF
                document.add(afterHeader2); // Adds data for company(Ladetech) and customer to PDF
                document.add(onsp); // Adds space before second divider
                document.add(purchaseTable); // Adds table heading for the data to PDF
                document.add(dataTable); // Adds the data genereated from the POS to PDF
                document.add(onsp);
                document.add(detailsCol); // Adds T&C's, Bank Details and price calculation to PDF.
                document.add(onsp);
                document.add(dashSpace); // Adds sign information to PDF
            }

            System.out.println("PDF created successfully.");
            
            // Opening pdf file after succeful generation
            File file = new File(invoicePath);
            
            try {
                Desktop.getDesktop().open(file);
            }catch (IOException e) {
                System.out.println(e.getMessage());
            } 
            
        } catch (IOException e) {
            System.out.println("Error creating PDF: " + e.getMessage());
        }

        // Updating invoice number count on database
        invoiceNo = invoiceNo + 1;
        Statement state = POS_source.mycon().createStatement();
        state.executeUpdate("UPDATE `company_data` SET `invoiceNo` = '" + invoiceNo + "'WHERE `company_data`.`companyVatNo` = 'C08427001112'");
        
        
    }
    
    // Generaing quotation PDF form
    public static void createQuotationPDF(DefaultTableModel model, String customer, String address, String vat, String tel, 
            String email, String total, String noVatTotal, String discount) throws SQLException{
        
        // Path where quotation will be stored after being generated
        String desktopPath  = System.getProperty("user.home") + "/Desktop"; // Getting path to desktop
        String quotationPath = desktopPath  + "/Quotation's\\quotation -" + " " + String.format("%05d", quotationNo) + ".pdf";
                                                
        // Calculating total tax and amount due
        Double tempTax = Double.parseDouble(noVatTotal) * taxRate;
        Double tempAmountDue = (Double.valueOf(noVatTotal) + tempTax) - Double.parseDouble(discount);
        
        // Changing format of values to currency format style
        String pdfDiscount = String.format("%,.2f", Double.valueOf(discount));
        String pdfTax = String.format("%,.2f", tempTax);   
        String pdfAmountDue = String.format("%,.2f", tempAmountDue);
        noVatTotal = String.format("%,.2f", Double.valueOf(noVatTotal));
        total = String.format("%,.2f", Double.valueOf(total));    
        
        
        try {
            
            PdfWriter pdfWriter = null;
            pdfWriter = new PdfWriter(quotationPath);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            try (Document document = new Document(pdfDocument)) {
                pdfDocument.setDefaultPageSize(PageSize.A4);
                
                float sixcol = 190f;
                float twocol = 285f;
                float twocol150 = twocol + 150f;
                float twocolumnWidth[] = {twocol150, twocol};
                float sixcolumnWidth [] = {(sixcol/3) + 20, sixcol - 25, sixcol/3, (sixcol/3) + 10, (sixcol/4) + 10, sixcol - 105};
                float colWidthSign [] = {sixcol, sixcol, sixcol};
                float colDetails [] = {sixcol + 75, sixcol - 30, sixcol - 30};
                float fullWidth[] = {sixcol * 6};
                Paragraph onsp = new Paragraph("\n");  // Used for spacing
                
                Table dataTable = new Table(sixcolumnWidth);
                
                // The header on the PDF (on the right top corner)
                Table header = new Table(twocolumnWidth);
                header.addCell(new Cell().add("Quotation").setBold().setFontSize(25f).setBorder(Border.NO_BORDER));
                
                // The header on the PDF (on the left top corner) - Quotation details and salesperson
                Table nestedTable = new Table(new float[]{twocol/2, twocol/2});
                nestedTable.addCell(new Cell().add("Quotation No: ").setBold());
                nestedTable.addCell(new Cell().add(String.format("%05d", quotationNo)));
                nestedTable.addCell(new Cell().add("Quotation Date: ").setBold());
                nestedTable.addCell(new Cell().add(date)); // Date for quotation
                nestedTable.addCell(new Cell().add("Salesperson: ").setBold());
                nestedTable.addCell(new Cell().add(login.user_name)); // Gets username for user logged in
                
                // First grey border after header information
                Border gb = new SolidBorder(com.itextpdf.kernel.color.Color.GRAY, 0.3f);
                Table divider = new Table(fullWidth);
                divider.setBorder(gb);
                
                // Heading information for company(Ladetech) and customer
                Table afterHeader = new Table(twocolumnWidth);
                afterHeader.addCell(new Cell().add("").setFontSize(13f).setBold().setBorder(Border.NO_BORDER));
                afterHeader.addCell(new Cell().add(Main_menu.companyName).setFontSize(13f).setBold().setBorder(Border.NO_BORDER));
                
                // Data for company(Ladetech) and customer
                Table afterHeader2 = new Table(twocolumnWidth);
                afterHeader2.addCell(new Cell().add("Customer").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("Details").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add(customer).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add(Main_menu.address + "\n" + Main_menu.poBox).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("Address: " + address).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("").setFontSize(10f).setBorder(Border.NO_BORDER)); // Space on information side for the company (Ladetech)
                afterHeader2.addCell(new Cell().add("VAT: " + vat).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("VAT: " + Main_menu.companyVatNo).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("TEL: " + tel).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("TEL: " + Main_menu.tel).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("Email: " + email).setFontSize(10f).setBorder(Border.NO_BORDER));
                afterHeader2.addCell(new Cell().add("Email: " + Main_menu.email).setFontSize(10f).setBorder(Border.NO_BORDER));
                
                // Creating table heading
                Table purchaseTable = new Table(sixcolumnWidth);
                purchaseTable.setBackgroundColor(com.itextpdf.kernel.color.Color.GRAY, 0.5f);
                purchaseTable.addCell(new Cell().add("Product ID").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                purchaseTable.addCell(new Cell().add("Product Name").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                purchaseTable.addCell(new Cell().add("Quantity").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                purchaseTable.addCell(new Cell().add("Unit Price").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                purchaseTable.addCell(new Cell().add("Tax").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                purchaseTable.addCell(new Cell().add("Sub Total").setFontSize(10f).setBold().setBorder(Border.NO_BORDER));
                
                // Iterates through the data in the POS and adds it to the product tabl pdf
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        String value = String.valueOf(model.getValueAt(i, j));
                        dataTable.addCell(new Cell().add(value).setFontSize(10f).setBorder(Border.NO_BORDER));
                    }
                }
                
                // Adding Name, Sign and Date at the bottom of the PDF
                Table dashSpace = new Table(colWidthSign);
                dashSpace.addCell(new Cell().add("Name: ___________________________").setFontSize(8f).setBorder(Border.NO_BORDER));
                dashSpace.addCell(new Cell().add("Sign: ___________________________").setFontSize(8f).setBorder(Border.NO_BORDER));
                dashSpace.addCell(new Cell().add("Date: ___________________________").setFontSize(8f).setBorder(Border.NO_BORDER));
                //dashSpace.setFixedPosition(page.getPageSize().getLeft() + 25f, page.getPageSize().getBottom() + 25f, page.getPageSize().getWidth()); // Adds table data to the bottom of the PDF
                
                Table detailsCol = new Table(colDetails);
                detailsCol.setBorder(Border.NO_BORDER); // Removing default border to add custom border
                detailsCol.setBorderBottom(new SolidBorder(com.itextpdf.kernel.color.Color.GRAY, 2)); // Adding borders around the cells
                detailsCol.setBorderTop(new SolidBorder(com.itextpdf.kernel.color.Color.GRAY, 2)); // Adding borders around the cells
                detailsCol.setBorderRight(new SolidBorder(com.itextpdf.kernel.color.Color.GRAY, 2)); // Adding borders around the cells
                detailsCol.setBorderLeft(new SolidBorder(com.itextpdf.kernel.color.Color.GRAY, 2)); // Adding borders around the cells
                detailsCol.addCell(new Cell().add("").setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("").setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Total(Excl) P:       " + noVatTotal ).setFontSize(9f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Terms & Conditions").setBold().setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Bank Details").setBold().setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Tax P:                  " + pdfTax).setFontSize(9f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("1. Goods not returned within 48 hrs, from the date of invoice,\nwill be subject to 15% handling fees.")
                        .setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("FNB Bank" + "\nAccount Number: " + "62079862051").setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Total(Inc) P:         " + total + "\nDiscount P:          " + pdfDiscount).setFontSize(9f)
                        .setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("2. 70% deposit along with the order, balance due upon arrival\nat " + Main_menu.companyName + ".\n"
                        + "3. Cash refunds attract 3% handling fee.\n"
                        + "4. All quoations are valid for ONLY 7 days after quotation date.\n"
                        + "5. All board cut to sizes according to customers approved cutting\nlist are not refundable or replaceable.\n"
                        + "6. All Terms & Conditions of " + Main_menu.companyName + ",\nremain valid & applicable.")
                        .setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("First Place Branch").setFontSize(8f).setBorder(Border.NO_BORDER));
                detailsCol.addCell(new Cell().add("Amount Due P:    " + pdfAmountDue).setBold().setFontSize(9f).setBorder(Border.NO_BORDER));
                //detailsCol.setFixedPosition(page.getPageSize().getLeft() + 25f, page.getPageSize().getBottom() + 50f, page.getPageSize().getWidth() - 53);
                
                // Adding Elements cretaed into the PDF file
                header.addCell(new Cell().add(nestedTable).setFontSize(10f).setBorder(Border.NO_BORDER)); // Adds quotation information on PDF
                document.add(header); // Adds "`Quotation" text to PDF
                document.add(onsp); // Adds space between header and divider to PDF
                document.add(divider); // Adds first grey divider to PDF
                document.add(onsp); // Adds space between after divider to PDF
                document.add(afterHeader); // Adds heading information for company(Ladetech) and customer to PDF
                document.add(onsp); // Adds space between after heading information for company(Ladetech) and customer to PDF
                document.add(afterHeader2); // Adds data for company(Ladetech) and customer to PDF
                document.add(onsp); // Adds space before second divider
                document.add(purchaseTable); // Adds table heading for the data to PDF
                document.add(dataTable); // Adds the data genereated from the POS to PDF
                document.add(onsp);
                document.add(detailsCol); // Adds T&C's, Bank Details and price calculation to PDF.
                document.add(onsp);
                document.add(dashSpace); // Adds sign information to PDF
            }

            System.out.println("PDF created successfully.");
            
            // Opening pdf file after succeful generation
            File file = new File(quotationPath);
            
            try {
                Desktop.getDesktop().open(file);
            }catch (IOException e) {
                System.out.println(e.getMessage());
            } 
            
        } catch (IOException e) {
            System.out.println("Error creating PDF: " + e.getMessage());
        }

        // Updating quotation number count on database
        quotationNo = quotationNo + 1;
        Statement state = POS_source.mycon().createStatement();
        state.executeUpdate("UPDATE `company_data` SET `quotationNo` = '" + quotationNo + "'WHERE `company_data`.`companyVatNo` = 'C08427001112'");
        
    }

}
