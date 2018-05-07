package com.tester.home.service;

import com.tester.home.entity.TestCase;
import com.tester.home.repository.TestCaseRepository;
import com.tester.home.utils.ExcelUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用例服务接口实现类
 */
@Service("TestCaseService")
public class TestCaseServiceImpl implements TestCaseService{
    @Autowired
    TestCaseRepository testCaseRepository;

    /**
     * 增加一条数据
     * @param testCase
     * @return
     */
    @Override
    public TestCase addCase(TestCase testCase) {
        return testCaseRepository.save(testCase);
//        if(testCaseRepository.exists(testCase.getId())){
//            return ;
//        }
    }

    @Override
    public void delCase(TestCase testCase) {
        testCaseRepository.delete(testCase);
        testCaseRepository.flush();
    }

    @Override
    public TestCase modifyCase(TestCase testCase) {
        return testCaseRepository.save(testCase);
    }
//    @Modifying
//    @Query("update TestCase t set t.testCase = ?1, t.type = ?2 where t.id = ?3")
//    public boolean modifyCases(String testCase, Integer type, Integer id){
//        return true;
//    }

    /**
     * 根据任意关键字查询用例
     * @param keyWord
     * @return
     */
    @Override
    public List<TestCase> queryCase(String keyWord) {
        return testCaseRepository.findTestCaseByTestcaseContaining(keyWord);
    }
//
//    @Override
//    public  Page<TestCase> findAllByLikeTestCase(final String keyWord, Pageable pageable) {
//        Page<TestCase> testCases = new TestCaseRepository.findAll(new Specification <TestCase>() {
//            @Override
//            public Predicate toPredicate(Root<TestCase> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
//                Predicate predicate = criteriaBuilder.conjunction();
//                List<Expression<Boolean>> expressions = predicate.getExpressions();
//
//
//                Predicate _key = criteriaBuilder.like(root.get("keyWord"), "%" + keyWord + "%");
//                expressions.add(_key);
////                expressions.add(criteriaBuilder.equal((root.<>)))?
//                return predicate;
//            }
//        },pageable);
//        return testCases;
//    }

    /**
     * 判断数据库是否存在用例数据，并返回数据
     * @param testCase
     * @return
     */
    @Override
    public TestCase hasCase(String testCase){
        return testCaseRepository.findTestCaseByTestcaseEquals(testCase);
    }

    /**
     * 批量导入数据库，并返回执行结果
     * @param fileName
     * @param filePath
     * @return
     */
    @Override
    public boolean batchImport(String fileName, String filePath) {
        ExcelUtil excelUtil = new ExcelUtil();

        int numInsert = 0;
        int numUnInsert = 0;

        List<TestCase> caseList = excelUtil.getCaseInfo(fileName, filePath);

        if(caseList == null) {
            return false;
        }
//         批量导入方法
//        testCaseRepository.save(caseList);
//        return true;

        if(fileName.contains("普通")){
            addCaseWithType(caseList, 1);
        }
        else if (fileName.contains("自动化")) {
            addCaseWithType(caseList, 2);
        }
        else {
            return false;
        }

        return true;
    }

    @Override
    public JSONObject countCase() {
        int generalCaseNum = testCaseRepository.countByType(1);
        int autoTestCaseNum = testCaseRepository.countByType(2);

        JSONObject data = new JSONObject();
        data.put("generalCaseNum" , generalCaseNum);
        data.put("autoTestCaseNum", autoTestCaseNum);

        return data;
    }

    private boolean addCaseWithType(List<TestCase> caseList, int type){
        // 插入之前判断，数据库是否存在已有用例数据
        for (TestCase testCase:caseList){
            if(hasCase(testCase.getTestcase()) == null){
                try {
                    testCase.setType(type);
                    testCaseRepository.save((testCase));
                } catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            }
        }

        return true;
    }
}
