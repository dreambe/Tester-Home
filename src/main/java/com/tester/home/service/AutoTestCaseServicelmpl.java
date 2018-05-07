package com.tester.home.service;

import com.tester.home.entity.AutoTestCase;
import com.tester.home.repository.AutoTestCaseRepository;
import com.tester.home.utils.ExcelUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shark
 * @date 2018/03/08
 */
@Service("AutoTestCaseService")
public class AutoTestCaseServicelmpl implements AutoTestCaseService{
//    @Autowired
    private AutoTestCaseRepository autoTestCaseRepository;

    public AutoTestCaseServicelmpl(AutoTestCaseRepository autoTestCaseRepository){
        this.autoTestCaseRepository = autoTestCaseRepository;
    }

    @Override
    public boolean batchImport(String fileName, String filePath){
        ExcelUtil excelUtil = new ExcelUtil();

        int numInsert = 0;
        int numUnInsert = 0;

        List caseList = excelUtil.getCaseInfo(fileName, filePath);

        if(caseList == null) {
            return false;
        }

        return (fileName.contains("自动化") && addCaseList(caseList));
    }

    /**
     * 插入case
     * @param caseList
     * @return
     */
    private boolean addCaseList(List<AutoTestCase> caseList){
        // 插入之前判断，数据库是否存在已有用例数据
        for (AutoTestCase aCase:caseList){
//            if(hasCase(aCase) != null) {
//                try {
//                    updateCase(aCase);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            }else
            if(hasCase(aCase) == null) {
                try {
                    autoTestCaseRepository.save(aCase);
                }  catch (javax.validation.ConstraintViolationException e) {
                    e.printStackTrace();
                    return false;
                }  catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    @Override
    public AutoTestCase addCase(AutoTestCase aCase){
        return autoTestCaseRepository.save(aCase);
    }

    /**
     * 判断数据库是否存在用例数据，并返回数据
     * @param aCase
     * @return
     */
    public AutoTestCase hasCase(AutoTestCase aCase){
        return autoTestCaseRepository.findByCaseNo(aCase.getCaseNo());
    }

    /**
     * 更新部分字段
     * @param aCase
     * @return
     */
    @Override
    public AutoTestCase updateCase(AutoTestCase aCase){
        Map<String, Object> map = convertObj2Map(aCase);
        StringBuilder str = new StringBuilder("");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if(entry.getValue() == null){
                str.append(entry.getKey() + "&");
            }
        }
        String[] ignoreProperties = str.toString().split("&");

        AutoTestCase originCase = autoTestCaseRepository.findOne(aCase.getId());
        BeanUtils.copyProperties(aCase, originCase, ignoreProperties);

        return autoTestCaseRepository.saveAndFlush(originCase);
    }

    /**
     * 将Objective对象转成Map对象
     * @param obj
     * @return
     */
    private Map convertObj2Map(Object obj){
        if(obj == null){
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                if(!key.equals("class")) {
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);

                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            System.out.println("convertObj2Map Error: " + e);
            return null;
        }

        return map;
    }
}
