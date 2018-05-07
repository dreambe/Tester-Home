package com.tester.home.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "auto_test_case")
//@Table(name = "shark_dev_env")
public class AutoTestCase {
    @Id
    @GeneratedValue
    private Integer id;

    @NotNull(message = "三端类别不能为空(APP,Web,H5)")
    private String platInfo;

    @NotNull(message = "业务功能模块不能为空")
    private String featureModule;


    @NotNull(message = "用例编号不能为空")
    @Column(unique = true, nullable = false)
    private String  caseNo;

    @NotNull(message = "用例描述不能为空")
    private String description;

    @NotNull(message = "接口API不能为空")
    @Column(unique = true, nullable = false)
    private String apiInfo;

    @NotNull(message = "接口请求方式不能为空")
    private String method;

    private String paramsType;

    private String params;

    private String userInfo;

    private String cookie;

    private String token;

    private String mysql;

    private String mongodb;

    private String redis;

    private String expectMysql;

    private String expectResponse;

    private String clearMysql;

    private String clearRedis;

    private String isIgnore;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlatInfo() {
        return platInfo;
    }

    public void setPlatInfo(String platInfo) {
        this.platInfo = platInfo;
    }

    public String getFeatureModule() {
        return featureModule;
    }

    public void setFeatureModule(String featureModule) {
        this.featureModule = featureModule;
    }

    public String getCaseNo() {
        return caseNo;
    }

    public void setCaseNo(String caseNo) {
        this.caseNo = caseNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApiInfo() {
        return apiInfo;
    }

    public void setApiInfo(String apiInfo) {
        this.apiInfo = apiInfo;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParamsType() {
        return paramsType;
    }

    public void setParamsType(String paramsType) {
        this.paramsType = paramsType;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMysql() {
        return mysql;
    }

    public void setMysql(String mysql) {
        this.mysql = mysql;
    }

    public String getMongodb() {
        return mongodb;
    }

    public void setMongodb(String mongodb) {
        this.mongodb = mongodb;
    }

    public String getRedis() {
        return redis;
    }

    public void setRedis(String redis) {
        this.redis = redis;
    }

    public String getExpectMysql() {
        return expectMysql;
    }

    public void setExpectMysql(String expectMysql) {
        this.expectMysql = expectMysql;
    }

    public String getExpectResponse() {
        return expectResponse;
    }

    public void setExpectResponse(String expectResponse) {
        this.expectResponse = expectResponse;
    }

    public String getClearMysql() {
        return clearMysql;
    }

    public void setClearMysql(String clearMysql) {
        this.clearMysql = clearMysql;
    }

    public String getClearRedis() {
        return clearRedis;
    }

    public void setClearRedis(String clearRedis) {
        this.clearRedis = clearRedis;
    }

    public String getIsIgnore() {
        return isIgnore;
    }

    public void setIsIgnore(String isIgnore) {
        this.isIgnore = isIgnore;
    }
}
