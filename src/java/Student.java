
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
@Named(value = "student")
@SessionScoped
@ManagedBean
public class Student implements Serializable {

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private int uid;
    
    private int teacher;

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
    public int getUid() {return uid;}
    public void setUid(int uid) {this.uid = uid;}
    public int getTeacher() {return teacher;}
    public void setTeacher(int teacher) {this.teacher = teacher;}
    
    public ArrayList<Misconception> getAllMisconceptions() throws SQLException{
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login login = (Login) elContext.getELResolver().getValue(elContext, null, "login");
        if(login.isStudent()){
            return getAllMisconceptions(login.getUid());
        }
        else{
            return getAllMisconceptions(Teacher.studentIdView);
        }
    }
    
    public ArrayList<Misconception> getAllMisconceptions(int student) throws SQLException{        
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps
                = con.prepareStatement(
                        "SELECT misconceptions.text, COUNT(answers.id) as times_missed "
                                + "FROM quizzes, quizrelations, questions, answers, misconceptionrelations, misconceptions "
                                + "WHERE  quizrelations.quiz = quizzes.id AND "
                                + "quizrelations.question = questions.id AND "
                                + "quizrelations.answer = answers.id AND "
                                + "questions.id = answers.question AND "
                                + "NOT answers.correct AND "
                                + "misconceptionrelations.answer = answers.id AND "
                                + "misconceptionrelations.misconception = misconceptions.id AND "
                                + "quizzes.student = ? "
                                + "GROUP BY misconceptions.id "
                                + "ORDER BY times_missed DESC");
        ps.setInt(1,student);
        System.out.println(ps);
        
        //get customer data from database
        ResultSet result = ps.executeQuery();

        ArrayList<Misconception> miscs = new ArrayList<>();

        while (result.next()) {
            Misconception m = new Misconception(result.getString("text"), result.getInt("times_missed"));
            miscs.add(m);
        }
        result.close();
        con.close();
        return miscs;
    }
    
    public String viewThisStudent(){
        Teacher.studentIdView = this.uid;
        return "success";
    }
}
