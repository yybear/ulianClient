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
public class Ce_Quiz_Tips implements Serializable {
    private Long id;
    private Long quiz_id;
    private String tips;
    private String order_num;
    private Integer rate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuiz_id() {
        return quiz_id;
    }

    public void setQuiz_id(Long quiz_id) {
        this.quiz_id = quiz_id;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getOrder_num() {
        return order_num;
    }

    public void setOrder_num(String order_num) {
        this.order_num = order_num;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }
}
