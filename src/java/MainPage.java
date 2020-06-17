/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.el.ELContext;
import javax.inject.Named;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author stanchev
 */
@Named(value = "mainPage")
@ManagedBean
@SessionScoped
public class MainPage implements Serializable {

    private List<String> choices;
    private DBConnect dbConnect = new DBConnect();
    private String choice;

    public String[] getChoices() {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login login = (Login) elContext.getELResolver().getValue(elContext, null, "login");
        choices = new ArrayList<>();
        
        if(login.isStudent()){
            choices.add("Start Random Quiz");
            choices.add("Start Custom Quiz");
            choices.add("View Results");
            choices.add("View Lifetime Stats");
        }
        if(login.isTeacher()){
            /*choices.add("View Students Quizzes");*/
            choices.add("Create Question");
            choices.add("View Students");
            choices.add("Add Student");
            choices.add("Create Custom Quiz");
        }
        if(login.isAdmin()){
            choices.add("Create teacher.");
        }
        
        String[] temp = new String[choices.size()];
        List<String> condenser = choices;
        for(int i = 0; i < condenser.size(); i++){
            temp[i] = condenser.get(i);
        }
        return temp;
    }

    public void setChoices(String[] choices) {
        List<String> temp = new ArrayList<>();
        for(int i = 0; i < choices.length; i++){
            temp.add(choices[i]);
        }
        this.choices = temp;
    }

    public String getChoice() throws SQLException {return choice;}

    public void setChoice(String choice) {
        this.choice = choice;
    }
    
    public String logout(){
        return Util.invalidateUserSession();
    }

    public String transition() {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login login = (Login) elContext.getELResolver().getValue(elContext, null, "login");
        switch (choice) {
            case "Start Random Quiz":
                return "startRandQuiz";
            case "View Results":
                return "viewResults";
            case "View Students Quizzes":
                return "viewQuizzes";
            case "Create Question":
                return "createQuestion";
            case "Create teacher.":
                return "createTeacher";
            case "Add Student":
                return "addStudent";
            case "View Students":
                return "viewStudents";
            case "Create Custom Quiz":
                return "createCustomQuiz";
            case "Start Custom Quiz":
                return "startCustomQuiz";
            case "View Lifetime Stats":
                return "viewLifeStats";
            default:
                return null;
        }
    }
}
