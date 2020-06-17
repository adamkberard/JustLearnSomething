import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIInput;
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
@Named(value = "answer")
@SessionScoped
@ManagedBean
public class Answer implements Serializable {

    private int id;
    private String text;
    private boolean correct;
    private Question question;
    private static String viewedMisconception;
    
    private DBConnect dbConnect = new DBConnect();
    private UIInput loginUI;

    /* So many stupid getters and setters. I really should go through and clean these up */
    public UIInput getLoginUI() {return loginUI;}
    public void setLoginUI(UIInput loginUI) {this.loginUI = loginUI;}
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getText() {return text;}
    public void setText(String text) {this.text = text;}
    public boolean isCorrect() {return correct;}
    public void setCorrect(boolean correct) {this.correct = correct;}
    public Question getQuestion() {return question;}
    public void setQuestion(Question question) {this.question = question;}
    public static String getViewedMisconception() {return viewedMisconception;}
    public static void setViewedMisconception(String viewedMisconception) {Answer.viewedMisconception = viewedMisconception;}
    
    public Answer(){}
    
    public Answer(int id, String text, boolean correct){
        this.id = id;
        this.text = text;
        this.correct = correct;
    }
    
    public String selectedAnswer(){
        question.setChosenAns(this);
        return "answered";
    }
    
    public String viewMisconception() throws SQLException{
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps
                = con.prepareStatement(
                        "SELECT misconceptions.text FROM misconceptions, misconceptionrelations WHERE misconceptions.id = misconceptionrelations.misconception AND misconceptionrelations.answer = ?;");
        ps.setInt(1, id);
        
        //get customer data from database
        ResultSet result = ps.executeQuery();

        if(result.next()) {
            viewedMisconception = result.getString("text"); 
        }
        else{
            viewedMisconception = "There wasn't a common misconception for this answer. Hit the books again!";
        }
        result.close();
        con.close();
            
        return "viewMisconception";
    }
    
    public String getCorrectness(){
        if(correct){
            return "Correct";
        }
        return "Incorrect";
    }
}
