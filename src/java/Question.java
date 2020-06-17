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
@Named(value = "question")
@SessionScoped
@ManagedBean
public class Question implements Serializable {

    private final String[] topics = new String[]{"Sequence Convergence/Divergence",
                                                 "Infinte Series Convergence/Divergence",
                                                 "Convergence/Divergence", 
                                                 "Evaluating Series", 
                                                 "Convergence Areas", 
                                                 "Sequence Convergence/Divergence"};
    private final String[] answerChoices = new String[]{"True", "False"};
    private String questionTopic;
    private String questionText;
    private int questionId;
    private ArrayList<Answer> answers;
    private Answer chosenAns = null;
    private String answerOneText;
    private String answerTwoText;
    private String answerThreeText;
    private String answerFourText;
    private boolean answerOneCorrect;
    private boolean answerTwoCorrect;
    private boolean answerThreeCorrect;
    private boolean answerFourCorrect;

    private DBConnect dbConnect = new DBConnect();
    private UIInput loginUI;

    /* So many stupid getters and setters. I really should go through and clean these up */
    public UIInput getLoginUI() {return loginUI;}
    public void setLoginUI(UIInput loginUI) {this.loginUI = loginUI;}
    public String[] getTopics() {return topics;}
    public String[] getAnswerChoices() {return answerChoices;}
    public String getQuestionTopic() {return questionTopic;}
    public void setQuestionTopic(String questionTopic) {this.questionTopic = questionTopic;}
    public String getQuestionText() {return questionText;}
    public void setQuestionText(String questionText) {this.questionText = questionText;}
    public int getQuestionId() {return questionId;}
    public void setQuestionId(int questionId) {this.questionId = questionId;}
    public Question(){}
    public Question(String aQuestionTopic, String aQuestionText, int aId){
        this.questionTopic = aQuestionTopic;
        this.questionText = aQuestionText;
        this.questionId = aId;
    }
    public String getAnswerOneText() {return answerOneText;}
    public void setAnswerOneText(String answerOneText) {this.answerOneText = answerOneText;}
    public String getAnswerTwoText() {return answerTwoText;}
    public void setAnswerTwoText(String answerTwoText) {this.answerTwoText = answerTwoText;}
    public String getAnswerThreeText() {return answerThreeText;}
    public void setAnswerThreeText(String answerThreeText) {this.answerThreeText = answerThreeText;}
    public String getAnswerFourText() {return answerFourText;}
    public void setAnswerFourText(String answerFourText) {this.answerFourText = answerFourText;}
    public boolean isAnswerOneCorrect() {return answerOneCorrect;}
    public void setAnswerOneCorrect(boolean answerOneCorrect) {this.answerOneCorrect = answerOneCorrect;}
    public boolean isAnswerTwoCorrect() {return answerTwoCorrect;}
    public void setAnswerTwoCorrect(boolean answerTwoCorrect) {this.answerTwoCorrect = answerTwoCorrect;}
    public boolean isAnswerThreeCorrect() {return answerThreeCorrect;}
    public void setAnswerThreeCorrect(boolean answerThreeCorrect) {this.answerThreeCorrect = answerThreeCorrect;}
    public boolean isAnswerFourCorrect() {return answerFourCorrect;}
    public void setAnswerFourCorrect(boolean answerFourCorrect) {this.answerFourCorrect = answerFourCorrect;}
    
    public String go() throws ValidatorException, SQLException {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login login = (Login) elContext.getELResolver().getValue(elContext, null, "login");
        
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO questions (text, topic) VALUES (?, ?) RETURNING id");
        ps.setString(1, questionText);
        ps.setString(2, questionTopic);
        
        ResultSet result = ps.executeQuery();
        
        result.next();
        questionId = result.getInt("id");
        
        ps = con.prepareStatement(
                "INSERT INTO answers (text, correct, question) VALUES (?, ?, ?)");
        ps.setString(1, answerOneText);
        ps.setBoolean(2, answerOneCorrect);
        ps.setInt(3, questionId);
        ps.executeUpdate();
        
        ps.setString(1, answerTwoText);
        ps.setBoolean(2, answerTwoCorrect);
        ps.setInt(3, questionId);
        ps.executeUpdate();
        
        ps.setString(1, answerThreeText);
        ps.setBoolean(2, answerThreeCorrect);
        ps.setInt(3, questionId);
        ps.executeUpdate();
        
        ps.setString(1, answerFourText);
        ps.setBoolean(2, answerFourCorrect);
        ps.setInt(3, questionId);
        ps.executeUpdate();
        
        con.close();
        
        questionText = null;
        questionTopic = null;
        answerOneText = null;
        answerOneCorrect = false;
        answerTwoText = null;
        answerTwoCorrect = false;
        answerThreeText = null;
        answerThreeCorrect = false;
        answerFourText = null;
        answerFourCorrect = false;
        
        return "created";
    }

    public String goToQuestion(){
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Quiz quiz = (Quiz) elContext.getELResolver().getValue(elContext, null, "quiz");
        quiz.setCurrentQuestion(this);
        return "goToQuestion";
    }
    
    public ArrayList<Answer> getAnswers() throws SQLException{
        if(answers != null){
            return answers;
        }
        
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps
                = con.prepareStatement(
                        "SELECT * FROM answers WHERE question = ?");
        ps.setInt(1, questionId);
        
        //get customer data from database
        ResultSet result = ps.executeQuery();

        ArrayList<Answer> list = new ArrayList<>();

        while (result.next()) {
            Answer ans = new Answer(result.getInt("id"), result.getString("text"), result.getBoolean("correct"));
            ans.setQuestion(this);
            list.add(ans);
        }
        result.close();
        con.close();
        
        answers = list;
        return answers;
    }

    public Answer getChosenAns() {
        return chosenAns;
    }

    public void setChosenAns(Answer chosenAns) {
        this.chosenAns = chosenAns;
    }

    public Answer getCorrectAnswer() throws SQLException{
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps
                = con.prepareStatement(
                        "SELECT * FROM answers WHERE correct AND question = ?");
        ps.setInt(1, questionId);
        
        //get customer data from database
        ResultSet result = ps.executeQuery();

        if(result.next()) {
            Answer a = new Answer();
            a.setId(result.getInt("id"));
            a.setText(result.getString("text"));
            a.setCorrect(result.getBoolean("correct"));
            result.close();
            con.close();
            return a;
        }
        else{
            result.close();
            con.close();
            return null;
        }
    }
    
    public Answer retrieveAnswer(int quizId) throws SQLException{
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps
                = con.prepareStatement(
                        "SELECT answers.* FROM quizrelations INNER JOIN answers ON quizrelations.answer = answers.id WHERE quizrelations.quiz = ? AND quizrelations.question = ?");
        ps.setInt(1, quizId);
        ps.setInt(2, questionId);
        
        //get customer data from database
        ResultSet result = ps.executeQuery();

        if(result.next()) {
            Answer a = new Answer();
            a.setId(result.getInt("id"));
            a.setText(result.getString("text"));
            a.setCorrect(result.getBoolean("correct"));
            result.close();
            con.close();
            return a;
        }
        else{
            result.close();
            con.close();
            return null;
        }
    }
}
