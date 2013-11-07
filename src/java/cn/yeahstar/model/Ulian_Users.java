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
public class Ulian_Users implements Serializable {
    private Long id;
    private String fullname;
    private String password;
    private String email;
    private Integer gender;
    private String school;
    private Integer grade;
    private String mobile;
    private String settings;
    private Integer timestamp;
    private Integer activation_time;
    private String activation_code;
    private Integer last_login_time;
    private Integer last_logout_time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getActivation_time() {
        return activation_time;
    }

    public void setActivation_time(Integer activation_time) {
        this.activation_time = activation_time;
    }

    public String getActivation_code() {
        return activation_code;
    }

    public void setActivation_code(String activation_code) {
        this.activation_code = activation_code;
    }

    public Integer getLast_login_time() {
        return last_login_time;
    }

    public void setLast_login_time(Integer last_login_time) {
        this.last_login_time = last_login_time;
    }

    public Integer getLast_logout_time() {
        return last_logout_time;
    }

    public void setLast_logout_time(Integer last_logout_time) {
        this.last_logout_time = last_logout_time;
    }

}
