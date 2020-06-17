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
@Named(value = "misconception")
@SessionScoped
@ManagedBean
public class Misconception implements Serializable {

    private int id, count;
    private String text;
    
    private DBConnect dbConnect = new DBConnect();
    
    public Misconception(){}
    public Misconception(String text, int count){
        this.text = text;
        this.count = count;
    }
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public int getCount() {return count;}
    public void setCount(int count) {this.count = count;}
    public String getText() {return text;}
    public void setText(String text) {this.text = text;}   
}
