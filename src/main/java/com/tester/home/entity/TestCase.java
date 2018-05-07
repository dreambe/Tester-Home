package com.tester.home.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "test_case")
public class TestCase {
    @Id
    @GeneratedValue
    private Integer id;

    @NotNull(message = "用例类别不能为空")
    private Integer type;

    @NotNull(message = "用例内容不能为空")
    @Column(unique = true, nullable = false)
    private String testcase;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTestcase() {
        return testcase;
    }

    public void setTestcase(String testcase) {
        this.testcase = testcase;
    }
}
