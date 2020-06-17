
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
@Named(value = "teacher")
@SessionScoped
@ManagedBean
public class Teacher implements Serializable {

    private String username;
    private String password;
    private String firstName;
    private String lastName;

    private String passwordErrorMessage;
    private String usernameErrorMessage;
    private String studentToBeAdded;
    private String studentToBeViewed;
    public static int studentIdView;
    
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
    public String getStudentToBeAdded() {return studentToBeAdded;}
    public void setStudentToBeAdded(String studentToBeAdded) {this.studentToBeAdded = studentToBeAdded;}
    public String getStudentToBeViewed() {return studentToBeViewed;}
    public void setStudentToBeViewed(String studentToBeViewed) {this.studentToBeViewed = studentToBeViewed;}

    
    
    public String go() throws ValidatorException, SQLException {
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps
                = con.prepareStatement(
                        "INSERT INTO users (username, password, role, first_name, last_name) VALUES (?, ?, ?, ?, ?)");
        ps.setString(1, username);
        ps.setString(2, password);
        ps.setString(3, "teacher");
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
    
    public ArrayList<String> getAvailableStudents() throws SQLException{
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM users WHERE role = 'student' AND teacher IS NULL");
        
        ResultSet result = ps.executeQuery();
        
        ArrayList<String> returnable = new ArrayList<>();
        int tempId;
        String tempUName, tempFName, tempLName, fancyStr;
        
        while(result.next()){
            tempId = result.getInt("uid");
            tempUName = result.getString("username");
            tempFName = result.getString("first_name");
            tempLName = result.getString("last_name");
            fancyStr = String.valueOf(tempId) + ": " + tempFName + " " + tempLName + " - " + tempUName;
            returnable.add(fancyStr);
        }
        result.close();
        con.close();
        return returnable;
    }
    
    public String addStudent() throws SQLException{
        Connection con = dbConnect.getConnection();
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login outerLogin = (Login) elContext.getELResolver().getValue(elContext, null, "login");

        if (con == null) {throw new SQLException("Can't get database connection");}

        int studentId = Integer.valueOf(studentToBeAdded.split(":")[0]);
        
        PreparedStatement ps = con.prepareStatement(
                        "UPDATE users set teacher = ? WHERE uid = ?");
        ps.setInt(1, outerLogin.getUid());
        ps.setInt(2, studentId);
        
        ps.executeUpdate();
        return "done";
    }
    
    public String viewStudent(){
        int studentId = Integer.valueOf(studentToBeAdded.split(":")[0]);
        Teacher.studentIdView = studentId;
        return "success";
    }
    
    public ArrayList<Student> getStudents() throws SQLException{
        Connection con = dbConnect.getConnection();
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login outerLogin = (Login) elContext.getELResolver().getValue(elContext, null, "login");

        if (con == null) {throw new SQLException("Can't get database connection");}
        
        PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM users WHERE teacher = ?");
        ps.setInt(1, outerLogin.getUid());
        
        ResultSet result = ps.executeQuery();
        
        ArrayList<Student> returnable = new ArrayList<>();
        Student tempStudent;
        
        while(result.next()){
            tempStudent = new Student();
            tempStudent.setFirstName(result.getString("first_name"));
            tempStudent.setLastName(result.getString("last_name"));
            tempStudent.setUid(result.getInt("uid"));
            tempStudent.setUsername(result.getString("username"));
            returnable.add(tempStudent);
        }
        result.close();
        con.close();
        return returnable;
    }
}
