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
public class Ce_Subquizzes implements Serializable{
    private Long id;
    private Long target_id;
    private String target_type;
    private String sub_quiz_type;
    private Long quiz_id;
    private Integer point;
    private String order_num;
    private String group;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTarget_id() {
        return target_id;
    }

    public void setTarget_id(Long target_id) {
        this.target_id = target_id;
    }

    public String getTarget_type() {
        return target_type;
    }

    public void setTarget_type(String target_type) {
        this.target_type = target_type;
    }

    public String getSub_quiz_type() {
        return sub_quiz_type;
    }

    public void setSub_quiz_type(String sub_quiz_type) {
        this.sub_quiz_type = sub_quiz_type;
    }

    public Long getQuiz_id() {
        return quiz_id;
    }

    public void setQuiz_id(Long quiz_id) {
        this.quiz_id = quiz_id;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public String getOrder_num() {
        return order_num;
    }

    public void setOrder_num(String order_num) {
        this.order_num = order_num;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
    
}
