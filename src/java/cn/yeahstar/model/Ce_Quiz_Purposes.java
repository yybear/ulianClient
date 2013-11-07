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
public class Ce_Quiz_Purposes implements Serializable{
    private String target_type;
    private Long target_id;
    private Long toc_id;
    private Integer purpose;
    private String description;

    public String getTarget_type() {
        return target_type;
    }

    public void setTarget_type(String target_type) {
        this.target_type = target_type;
    }

    public Long getTarget_id() {
        return target_id;
    }

    public void setTarget_id(Long target_id) {
        this.target_id = target_id;
    }

    public Long getToc_id() {
        return toc_id;
    }

    public void setToc_id(Long toc_id) {
        this.toc_id = toc_id;
    }

    public Integer getPurpose() {
        return purpose;
    }

    public void setPurpose(Integer purpose) {
        this.purpose = purpose;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
