/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.model;

import java.io.Serializable;

/**
 *
 * @author ganqing
 */
public class Ce_Knowledges implements Serializable {
    private String grade;
    private String subject;
    private Long toc_id;
    private String textbook;
    private String textbook_volume;
    private String order_num;

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Long getToc_id() {
        return toc_id;
    }

    public void setToc_id(Long toc_id) {
        this.toc_id = toc_id;
    }

    public String getTextbook() {
        return textbook;
    }

    public void setTextbook(String textbook) {
        this.textbook = textbook;
    }

    public String getTextbook_volume() {
        return textbook_volume;
    }

    public void setTextbook_volume(String textbook_volume) {
        this.textbook_volume = textbook_volume;
    }

    public String getOrder_num() {
        return order_num;
    }

    public void setOrder_num(String order_num) {
        this.order_num = order_num;
    }
    
    
}
