package com.tester.home.service;

import com.tester.home.entity.TestCase;
import org.json.JSONObject;

import java.util.List;


/**
 * 用例服务接口
 */
public interface TestCaseService {
    TestCase addCase(TestCase testCase);

    void delCase(TestCase testCase);

    TestCase modifyCase(TestCase testCase);

    List<TestCase> queryCase(String keyWord);

//    Page<TestCase> findAllContaining(String keyWord, int page, int size);

    TestCase hasCase(String testCase);

//    List findAll();

    public boolean batchImport(String fileName, String filePath);

    public JSONObject countCase();
}
