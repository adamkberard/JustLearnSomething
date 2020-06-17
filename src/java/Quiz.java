import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.annotation.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Named;
import java.util.Random;
import javax.el.ELContext;
import javax.faces.context.FacesContext;

@Named(value = "quiz")
@SessionScoped
@ManagedBean
public class Quiz implements Serializable {

    private DBConnect dbConnect = new DBConnect();
    private int quizId = 0;
    private int numQuestions = 100;
    private int studentId;
    private ArrayList<Question> quizQuestions = null;
    private Question currentQuestion = null;
    private int numCorrect;
    private static Quiz viewedQuiz;
    private static String viewedMisconception;
    private static ArrayList<Question> currentCreatedQuiz = new ArrayList<>();
    private static ArrayList<Question> allPossibleQuestions = null;
    private String quizToBeNamed;
    private String quizToBeTaken;
    private static String otherQuizToBeTaken;

    public int getQuizId() {return quizId;}
    public void setQuizId(int quizId) {this.quizId = quizId;}
    public int getNumQuestions() {return numQuestions;}
    public void setNumQuestions(int numQuestions) {this.numQuestions = numQuestions;}
    public int getStudentId() {return studentId;}
    public void setStudentId(int studentId) {this.studentId = studentId;}
    public Question getCurrentQuestion() {return currentQuestion;}
    public void setCurrentQuestion(Question currentQuestion) {this.currentQuestion = currentQuestion;}
    public ArrayList<Question> getQuizQuestions() throws SQLException {
        if(otherQuizToBeTaken != null){
            quizQuestions = getCustomQuestions();
            System.out.println("AT LEAST GOT HERE");
        }
        else{
            quizQuestions = createQuestions();
        }
        numQuestions = quizQuestions.size();
        return quizQuestions;
    }
    public static String getViewedMisconception() {return viewedMisconception;}
    public static void setViewedMisconception(String viewedMisconception) {Quiz.viewedMisconception = viewedMisconception;}
    public String getQuizToBeNamed() {return quizToBeNamed;}
    public void setQuizToBeNamed(String quizToBeNamed) {this.quizToBeNamed = quizToBeNamed;}
    public static void addQuestionToList(){
        
    }
    private ArrayList<Question> getCustomQuestions() throws SQLException{
        if(quizQuestions != null){
            return this.quizQuestions;
        }
        ArrayList<Question> returnable = new ArrayList<>();
        int customQuizId = Integer.valueOf(otherQuizToBeTaken.split(":")[0]);
        Connection con = dbConnect.getConnection();
        
        PreparedStatement ps = con.prepareStatement("SELECT questions.* FROM custom_quiz_relations, questions WHERE custom_quiz_relations.question = questions.id AND custom_quiz_relations.custom_quiz = ?");
        ps.setInt(1, customQuizId);
        ResultSet result = ps.executeQuery();
        
        while(result.next()){
            Question temp = new Question(result.getString("topic"), 
                                         result.getString("text"),
                                         result.getInt("id"));
            returnable.add(temp);
        }
        
        return returnable;
    }
    
    public String getQuizToBeTaken() {return quizToBeTaken;}
    public void setQuizToBeTaken(String quizToBeTaken) {this.quizToBeTaken = quizToBeTaken;}
    
    
    public ArrayList<Question> retrieveQuestions() throws SQLException{
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps
                = con.prepareStatement(
                        "SELECT questions.* FROM quizrelations INNER JOIN questions ON quizrelations.question = questions.id WHERE quizrelations.quiz = ?");
        ps.setInt(1, quizId);
        
        ResultSet result = ps.executeQuery();

        ArrayList<Question> questions = new ArrayList<>();

        while (result.next()) {
            Question q = new Question();
            System.out.println("QID:");
            System.out.println(result.getInt("id"));
            q.setQuestionId(result.getInt("id"));
            q.setQuestionTopic(result.getString("topic"));
            q.setQuestionText(result.getString("text"));
            questions.add(q);
        }
        result.close();
        con.close();
        
        return questions;
    }
    
    public int getNumCorrect() {return numCorrect;}
    public void setNumCorrect(int numCorrect) {this.numCorrect = numCorrect;}
    public Quiz getViewedQuiz() {return viewedQuiz;}
    public void setViewedQuiz(Quiz viewedQuiz) {this.viewedQuiz = viewedQuiz;}
    

    private ArrayList<Question> createQuestions() throws SQLException{
        ArrayList<Question> questions = new ArrayList<>();
        Random rand = new Random();
        int toDelete;
        
        if(quizQuestions != null){
            return this.quizQuestions;
        }

        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        /* Gets a list of all the quesitons */
        PreparedStatement ps
                = con.prepareStatement("SELECT * FROM questions ORDER BY RANDOM() LIMIT ?");
        ps.setInt(1, numQuestions);
        
        //get customer data from database
        ResultSet result = ps.executeQuery();

        while (result.next()) {
            Question temp = new Question(result.getString("topic"), 
                                         result.getString("text"),
                                         result.getInt("id"));
            questions.add(temp);
        }
        result.close();
        con.close();
        
        return questions;
    }
    
    public boolean isDone(){
        for (Question quizQuestion : quizQuestions) {
            if (quizQuestion.getChosenAns() == null) {
                return false;
            }
        }
        return true;
    }
    
    public String view(){
        viewedQuiz = this;
        return "toQuizView";
    }
    
    public ArrayList<Quiz> getQuizzes() throws SQLException{
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login login = (Login) elContext.getELResolver().getValue(elContext, null, "login");        
        
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps
                = con.prepareStatement(
                        "SELECT * FROM quizzes WHERE student = ?");
        ps.setInt(1, login.getUid());
        
        //get customer data from database
        ResultSet result = ps.executeQuery();

        ArrayList<Quiz> quizzes = new ArrayList<>();

        while (result.next()) {
            Quiz q = new Quiz();
            q.setQuizId(result.getInt("id"));
            q.setNumQuestions(result.getInt("num_questions"));
            q.setNumCorrect(result.getInt("num_correct"));
            quizzes.add(q);
        }
        result.close();
        con.close();
        return quizzes;
    }
    
    public String submit() throws SQLException{
        
        int correctCounter = 0;
        
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login login = (Login) elContext.getELResolver().getValue(elContext, null, "login");
        
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO quizzes (student, num_questions) VALUES (?, ?) RETURNING id");
        ps.setInt(1, login.getUid());
        ps.setInt(2, quizQuestions.size());
        
        ResultSet result = ps.executeQuery();
        
        result.next();
        quizId = result.getInt("id");
        
        for (Question quizQuestion : quizQuestions) {
            ps = con.prepareStatement(
                        "INSERT INTO quizrelations (quiz, question, answer) VALUES (?, ?, ?)");
            ps.setInt(1, quizId);
            ps.setInt(2, quizQuestion.getQuestionId());
            ps.setInt(3, quizQuestion.getChosenAns().getId());

            ps.executeUpdate();
            
            if(quizQuestion.getChosenAns().isCorrect()){
                correctCounter++;
            }
        }
        ps = con.prepareStatement(
                        "UPDATE quizzes SET num_correct = ? WHERE id = ?");
        ps.setInt(1, correctCounter);
        ps.setInt(2, quizId);

        ps.executeUpdate();
        
        numCorrect = correctCounter;
        this.view();
        otherQuizToBeTaken = null;
        return "submitted";
    }
    
    public String done(){
        this.quizQuestions = null;
        return "done";
    }
    
    public ArrayList<Misconception> getALlMisconceptions() throws SQLException{
        return getALlMisconceptions(this.quizId);
    }
    
    public ArrayList<Misconception> getALlMisconceptions(int quizId) throws SQLException{
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login login = (Login) elContext.getELResolver().getValue(elContext, null, "login");        
        
        Connection con = dbConnect.getConnection();

        if (con == null) {throw new SQLException("Can't get database connection");}

        System.out.println("LOOKING FOR MISCONCPETIONS FOR " + quizId);
        PreparedStatement ps
                = con.prepareStatement(
                        "SELECT misconceptions.text, COUNT(answers.id) as times_missed "
                                + "FROM quizrelations, questions, answers, misconceptionrelations, misconceptions "
                                + "WHERE  quizrelations.question = questions.id AND "
                                + "         quizrelations.answer = answers.id AND "
                                + "         questions.id = answers.question AND "
                                + "         NOT answers.correct AND "
                                + "         misconceptionrelations.answer = answers.id AND "
                                + "         misconceptionrelations.misconception = misconceptions.id AND "
                                + "         quizrelations.quiz = ? "
                                + "         GROUP BY misconceptions.id"
                                + "         ORDER BY times_missed DESC");
        ps.setInt(1,quizId);
        
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
    
    public ArrayList<Question> getAllPossibleQuestions() throws SQLException{
        ArrayList<Question> returnable = new ArrayList<>();
        Connection con = dbConnect.getConnection();
        if(allPossibleQuestions != null){
            return allPossibleQuestions;
        }
        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps = con.prepareStatement("SELECT * FROM questions");
        ResultSet result = ps.executeQuery();
        
        while(result.next()){
            returnable.add(new Question(result.getString("topic"), result.getString("text"), result.getInt("id")));
        }
        allPossibleQuestions = returnable;
        return returnable;
    }
    
    public ArrayList<Question> getAllIncludedQuestions(){
        return currentCreatedQuiz;
    }
    
    public String addQuestionToCreatedQuiz(int id) throws SQLException{
        ArrayList<Question> returnable = new ArrayList<>();
        
        Connection con = dbConnect.getConnection();
        if (con == null) {throw new SQLException("Can't get database connection");}

        PreparedStatement ps = con.prepareStatement("SELECT * FROM questions WHERE id = ?");
        System.out.println("ID IS: " + id);
        ps.setInt(1, id);
        ResultSet result = ps.executeQuery();
        
        while(result.next()){
            currentCreatedQuiz.add(new Question(result.getString("topic"), 
                                                result.getString("text"), 
                                                result.getInt("id")));
        }
        return "refresh";
    }
    
    public String removeQuestionToCreatedQuiz(int id) throws SQLException{
        ArrayList<Question> returnable = new ArrayList<>();
        
        for(Question q : currentCreatedQuiz){
            if(q.getQuestionId() == id){
                currentCreatedQuiz.remove(q);
                return "refresh";
            }
        }

        return "refresh";
    }
    
    public String createCustomQuiz() throws SQLException{
        Connection con = dbConnect.getConnection();
        if (con == null) {throw new SQLException("Can't get database connection");}
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login login = (Login) elContext.getELResolver().getValue(elContext, null, "login");  

        PreparedStatement ps = con.prepareStatement("INSERT INTO custom_quizzes (name, teacher) VALUES (?, ?) RETURNING id");
        ps.setString(1, quizToBeNamed);
        ps.setInt(2, login.getUid());
        ResultSet result = ps.executeQuery();
        result.next();
        int quizId = result.getInt("id");
        
        for(Question q : currentCreatedQuiz){
            ps = con.prepareStatement("INSERT INTO custom_quiz_relations (custom_quiz, question) VALUES (?, ?)");
            ps.setInt(1, quizId);
            ps.setInt(2, q.getQuestionId());
            ps.executeUpdate();
        }

        return "success";
    }
    
    public ArrayList<String> getCustomQuizzes() throws SQLException{
        ArrayList<String> returnable = new ArrayList<>();
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Login login = (Login) elContext.getELResolver().getValue(elContext, null, "login");
        Connection con = dbConnect.getConnection();
        if (con == null) {throw new SQLException("Can't get database connection");}
        
        PreparedStatement ps = con.prepareStatement("SELECT teacher FROM users WHERE uid = ?");
        ps.setInt(1, login.getUid());
        ResultSet result = ps.executeQuery();
        result.next();
        int teacher = result.getInt("teacher");
        
        ps = con.prepareStatement("SELECT * FROM custom_quizzes WHERE teacher = ?");
        ps.setInt(1, teacher);
        
        result = ps.executeQuery();
 
        String fancy;
        while(result.next()){
            fancy = String.valueOf(result.getInt("id")) + ": " + result.getString("name");
            returnable.add(fancy);
        }
        return returnable;
    }
    
    public String goToCustomQuiz(){
        otherQuizToBeTaken = quizToBeTaken;
        quizQuestions = null;
        return "success";
    }
}
