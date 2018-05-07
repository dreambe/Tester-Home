package com.tester.home.repository;

import com.tester.home.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {
    TestCase findTestCaseById(Integer id);

    TestCase findTestCaseByTestcaseEquals(String testCase);

    List<TestCase> findTestCaseByTestcaseContaining(String keyWord);

    Integer countByType(Integer type);

//    Page<TestCase> findByNo(Integer pageno, Pageable pageable);
}
