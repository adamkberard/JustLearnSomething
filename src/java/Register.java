
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.annotation.ManagedBean;
import javax.el.ELContext;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIInput;
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
 * @author stanchev
 */
@Named(value = "register")
@SessionScoped
@ManagedBean
public class Register implements Serializable {

    private String username;
    private String password;
    private String firstName;
    private String lastName;

    private String passwordErrorMessage;
    private String usernameErrorMessage;
    
    private DBConnect dbConnect = new DBConnect();  
    private UIInput loginUI;

    /* So many stupid getters and setters. I really should go through and clean these up */
    public void setLastName(String lastName) {this.lastName = lastName;}
    public String getFirstName() {return firstName;}
    public void setFirstName(String firstName) {this.firstName = firstName;}
    public String getLastName() {return lastName;}
    public UIInput getLoginUI() {return loginUI;}
    public void setLoginUI(UIInput loginUI) {this.loginUI = loginUI;}
    public String getUsername() {return username;}
    public void setUsername(String login) {this.username = login;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
    public String getPasswordErrorMessage() {return passwordErrorMessage;}
    public void setPasswordErrorMessage(String passwordErrorMessage) {this.passwordErrorMessage = passwordErrorMessage;}
    public String getUsernameErrorMessage() {return usernameErrorMessage;}
    public void setUsernameErrorMessage(String usernameErrorMessage) {this.usernameErrorMessage = usernameErrorMessage;}

    public String go() throws ValidatorException, SQLException {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login login = (Login) elContext.getELResolver().getValue(elContext, null, "login");
        
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users (username, password, role, first_name, last_name) VALUES (?, ?, ?, ?, ?)");
        ps.setString(1, username);
        ps.setString(2, password);
        
        ps.setString(3, "student");
        ps.setString(4, firstName);
        ps.setString(5, lastName);
        
        
        ps.executeUpdate();
        con.close();
        
        return "registered";
    }
    
    public String update() throws ValidatorException, SQLException {
        Connection con = dbConnect.getConnection();
       
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login outerLogin = (Login) elContext.getELResolver().getValue(elContext, null, "login");
       
        if (con == null) {
            throw new SQLException("Can't get database connection");
        }

        PreparedStatement ps
                = con.prepareStatement(
                        "UPDATE employee SET username = ?, password = ? WHERE employee.id = ?");
        ps.setString(1, outerLogin.getUsername());
        ps.setString(2, outerLogin.getPassword());
        ps.setInt(3, outerLogin.getUid());
        
        ps.executeUpdate();
        
        con.close();
        
        return "success";
    }
    
}
