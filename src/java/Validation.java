
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import javax.annotation.ManagedBean;
import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aberard
 */
@Named(value = "validation")
@SessionScoped
@ManagedBean
public class Validation {
    
    private DBConnect dbConnect = new DBConnect();
    private String passwordErrorMessage;
    private String usernameErrorMessage;

    public String getPasswordErrorMessage() {return passwordErrorMessage;}
    public String getUsernameErrorMessage() {return usernameErrorMessage;}
    
    public void validateUsername(FacesContext context, UIComponent component, Object value) throws SQLException{
        Connection con = dbConnect.getConnection();
        String username = value.toString();
        
        if (con == null) {
            throw new SQLException("Can't get database connection");
        }
        PreparedStatement ps = con.prepareStatement(
                        "SElECT uid FROM users WHERE username = ?");
        ps.setString(1, username);
        
        ResultSet result = ps.executeQuery();

        if(result.next()) {
            result.close();
            con.close();
            usernameErrorMessage = "Username is already taken.";
            FacesMessage errorMessage = new FacesMessage(usernameErrorMessage);
            throw new ValidatorException(errorMessage);
        }
        
        if(username.replace(" ", "").length() != username.length()){
            usernameErrorMessage = "No spaces allowed in usernames you weirdo.";
            FacesMessage errorMessage = new FacesMessage(usernameErrorMessage);
            throw new ValidatorException(errorMessage);
        }
    }
    
    public void validatePassword(FacesContext context, UIComponent component, Object value) throws SQLException{
        String password = value.toString();
        
        if(password.length() < 6){
            passwordErrorMessage = "Password must be six characters long,";
            FacesMessage errorMessage = new FacesMessage(passwordErrorMessage);
            throw new ValidatorException(errorMessage);
        }
        if(!(Validation.containsDigit(password))){
            passwordErrorMessage = "Password must contain at least one number.";
            FacesMessage errorMessage = new FacesMessage(passwordErrorMessage);
            throw new ValidatorException(errorMessage);
        }        
    }
        
    public static boolean containsDigit(final String aString){
        return aString != null && !aString.isEmpty() && aString.chars().anyMatch(Character::isDigit);
    }
    
    public static Calendar getCalObj(String date_string){
        String[] parts = date_string.split("/");
        
        int month = Integer.parseInt(parts[0]);
        int day = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        
        Calendar date = Calendar.getInstance();
        date.set(year, month - 1, day);
        return date;
    }

    public static String validDate(String date_string){
        int day;
        int month;
        int year;
        
        System.out.println("Validating date.");

        String[] parts = date_string.split("/");
        
        /* Checks if there are non number values other than the '/' */
        String check = date_string.replaceAll("/", "");
        if(!(check.matches("[0-9]+"))){
            return "Date is not all digits.";
        }
        
        /* Checks to make sure there is a year month and day */
        if(parts.length != 3){
            return "Incorrect date format. Must be mm/dd/yyyy";
        }
        
        /* Checks to make sure the numbers are two digits for month and day and four digits for year */
        if(parts[0].length() != 2 || parts[1].length() != 2 || parts[2].length() != 4){
            return "Incorrect date format. Must be mm/dd/yyyy";
        }
        
        /* Check if day is valid meaning non negative. Doesn't check for like Feb 31st though. */
        month = Integer.parseInt(parts[0]);
        day = Integer.parseInt(parts[1]);
        year = Integer.parseInt(parts[2]);
        
        if(month < 1 || month > 12 || day < 1 || day > 31 ){
            return "Incorrect date.";
        }
        
        /* Now make sure it is in the future or on the current day */
        Calendar today = Calendar.getInstance();
        Calendar date = Validation.getCalObj(date_string);
        
        if(date.compareTo(today) < 0){
            return "Date is in the past.";
        }
        return "valid";
    }
    
}
