package com.tester.home.repository;

import com.alibaba.fastjson.JSONArray;
import com.tester.home.entity.AutoTestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * @author shark
 * @date 2018/03/08
 */
public interface AutoTestCaseRepository extends JpaRepository<AutoTestCase, Integer>, JpaSpecificationExecutor<AutoTestCase> {

    AutoTestCase findByDescriptionLike(String description);

    AutoTestCase findById(Integer id);

    AutoTestCase findByCaseNo(String caseNo);

    @Query(value = "SELECT distinct plat_info,feature_module FROM auto_test_case", nativeQuery = true)
    JSONArray findByPlatInfoAndFeatureModule();
}
