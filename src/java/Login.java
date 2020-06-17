
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.ManagedBean;
import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
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
@Named(value = "login")
@SessionScoped
@ManagedBean
public class Login implements Serializable {    
    private DBConnect dbConnect = new DBConnect();
   
    private UIInput loginUI;
    
    private String username = "";
    private String password = "";
    private String firstName = "";
    private String lastName = "";
    private String role = "";
    private int uid = 0;
    
    /* Getters and setters */
    public boolean isStudent() {
        return this.role.equals("student");}
    public boolean isTeacher(){return this.role.equals("teacher") || this.role.equals("admin");}
    public boolean isAdmin() {return this.role.equals("admin");}
    public UIInput getLoginUI() {return loginUI;}
    public void setLoginUI(UIInput loginUI) {this.loginUI = loginUI;}
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
    public int getUid() {return uid;}
    

    public void validate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException, SQLException {
        username = loginUI.getLocalValue().toString();
        password = value.toString();
        
        Connection con = dbConnect.getConnection();

        if (con == null) {
            throw new SQLException("Can't get database connection");
        }

        PreparedStatement ps
                = con.prepareStatement(
                        "SELECT * FROM users WHERE username = ? AND password = ?");
        ps.setString(1, username);
        ps.setString(2, password);
        //get user data from database
        
        ResultSet result = ps.executeQuery();
        
        if(result.next()) {
            uid = result.getInt("uid");
            firstName = result.getString("first_name");
            lastName = result.getString("last_name");
            role = result.getString("role");
            result.close();
            con.close();
        }
        else{
            FacesMessage errorMessage = new FacesMessage("Wrong login/password");
            throw new ValidatorException(errorMessage);
        }
    }

    public String go() {
        return "success";
    }
     
    public String register() {
        return "register";
    }
    
    public String logout() {
        Util.invalidateUserSession();
        return "logout";
    }

}
