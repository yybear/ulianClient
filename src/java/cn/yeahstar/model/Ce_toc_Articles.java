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
public class Ce_toc_Articles implements Serializable {
    private Long toc_id;
    private Long article_id;
    private String orderNum;

    public Long getToc_id() {
        return toc_id;
    }

    public void setToc_id(Long toc_id) {
        this.toc_id = toc_id;
    }

    public Long getArticle_id() {
        return article_id;
    }

    public void setArticle_id(Long article_id) {
        this.article_id = article_id;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }
}
