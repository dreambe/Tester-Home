package com.tester.home.service;


import com.tester.home.entity.AutoTestCase;

public interface AutoTestCaseService {
    boolean batchImport(String fileName, String filePath);

    AutoTestCase hasCase(AutoTestCase aCase);

    AutoTestCase addCase(AutoTestCase aCase);

    AutoTestCase updateCase(AutoTestCase aCase);
}
