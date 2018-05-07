package com.tester.home.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tester.home.entity.AutoTestCase;
import com.tester.home.entity.RestResult;
import com.tester.home.repository.AutoTestCaseRepository;
import com.tester.home.service.AutoTestCaseService;
import com.tester.home.utils.ExcelUtil;
import com.tester.home.utils.ResultGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/autocase")
@Validated
public class AutoTestCaseController extends BaseController {
    @Autowired
    private Environment envConfig;

    private Logger logger = LoggerFactory.getLogger((this.getClass()));

    private final ResultGenerator generator;

    private static final long MAX_FILE_SIZE = 30*1024*1024;
    private AutoTestCaseService autoTestCaseService;

    private final AutoTestCaseRepository autoTestCaseRepository;

    @Autowired
    public AutoTestCaseController(ResultGenerator generator,AutoTestCaseService autoTestCaseService, AutoTestCaseRepository autoTestCaseRepository){
        this.generator = generator;
        this.autoTestCaseService = autoTestCaseService;
        this.autoTestCaseRepository = autoTestCaseRepository;
    }

    /**
     * 上传文件接口，目前支持excel格式
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public RestResult upload(@RequestParam("file") MultipartFile file){
        logger.info("Begin To Upload: ");

        if(!file.isEmpty()){
            String fileName = file.getName();
            String originFileName = file.getOriginalFilename();
            String fileNameNoEx = originFileName.substring(0, originFileName.lastIndexOf("."));
            String extension = originFileName.substring(originFileName.lastIndexOf("."));

            if(!extension.contains("xls")){
                return generator.getFailResult("文件类型错误，目前只支持excel");
            }

            long size = file.getSize();
            if(size == 0) {
                return generator.getFailResult("文件不能为空");
            }
            if(size > MAX_FILE_SIZE) {
                return generator.getFailResult("文件过大");
            }

            String UPLOAD_DIR = envConfig.getProperty("upload.dir");
            String filePath = UPLOAD_DIR + fileNameNoEx + String.valueOf(System.currentTimeMillis()) + extension;

            try {
                file.transferTo(new File(filePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 入库操作
            if(!autoTestCaseService.batchImport(originFileName ,filePath)){
                return generator.getFailResult("上传成功，入库失败，文件名需要包含‘普通‘或者’自动化’以方便分类入库;检查excel表格必填项是否为空");
            }
        }

        return generator.getSuccessResult("上传成功，入库成功");
    }

    /**
     * 获取所有自动化case
     * @param page
     * @param size
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public RestResult caseList(@RequestParam(value = "plat", defaultValue = "all") String plat,
                               @RequestParam(value = "feature", defaultValue = "all") String feature,
                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(page-1, size, sort);

        if(plat.equals("all")){
            return generator.getSuccessResult(autoTestCaseRepository.findAll(pageable));
        }

        Specification<AutoTestCase> spec = new Specification<AutoTestCase>() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<Predicate>();
                Path<Long> $plat = root.get("platInfo");
                Predicate _plat = criteriaBuilder.equal($plat, plat);
                predicateList.add(_plat);
                if(!feature.equals("all")){
                    Path<Long> $feature = root.get("featureModule");
                    Predicate _feature = criteriaBuilder.equal($feature, feature);
                    predicateList.add(_feature);
                }
                return criteriaBuilder.and(predicateList.toArray(new Predicate[] {} ));
            }
        };

        return generator.getSuccessResult(autoTestCaseRepository.findAll(spec, pageable));
    }

    /**
     * 获取侧边栏分类的列表
     * @return
     */
    @RequestMapping(value = "/sidebar", method = RequestMethod.GET)
    public RestResult sidebar(){
        JSONArray jsonArray = autoTestCaseRepository.findByPlatInfoAndFeatureModule();

        JSONObject resultObj = new JSONObject();

        for (int i=0; i<jsonArray.size(); i++){
            JSONArray array = jsonArray.getJSONArray(i);
            String plat = array.getString(0);
            JSONArray featureArray = new JSONArray();

            for (int j=0; j<jsonArray.size(); j++) {
                JSONArray arrayTemp = jsonArray.getJSONArray(j);
                if(plat.equals(arrayTemp.getString(0))){
                    featureArray.add(arrayTemp.getString(1));
                }
            }
            resultObj.put(plat, featureArray);
        }

        return generator.getSuccessResult(resultObj);
    }

    /**
     * 导出用例
     * @param plat
     * @param feature
     * @return
     */
    @RequestMapping(value = "export", method = RequestMethod.GET)
    public RestResult fileExport(@RequestParam(value = "plat", defaultValue = "all") String plat,
                                 @RequestParam(value = "feature", defaultValue = "all") String feature) {
        String filePath = envConfig.getProperty("export.dir");
        if(plat.equals("all")){
            ExcelUtil excelUtil = new ExcelUtil();
            List listData = autoTestCaseRepository.findAll();
            excelUtil.writeExcel(listData, filePath + "Autotest-" + String.valueOf(System.currentTimeMillis()) + ".xlsx");

            return generator.getSuccessResult("导出成功");
        }

        Specification<AutoTestCase> spec = new Specification<AutoTestCase>() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<Predicate>();
                Path<Long> $plat = root.get("platInfo");
                Predicate _plat = criteriaBuilder.equal($plat, plat);
                predicateList.add(_plat);
                if(!feature.equals("all")){
                    Path<Long> $feature = root.get("featureModule");
                    Predicate _feature = criteriaBuilder.equal($feature, feature);
                    predicateList.add(_feature);
                }
                return criteriaBuilder.and(predicateList.toArray(new Predicate[] {} ));
            }
        };


        ExcelUtil excelUtil = new ExcelUtil();
        List listData = autoTestCaseRepository.findAll(spec);

        excelUtil.writeExcel(listData, filePath + "Autotest-" + String.valueOf(System.currentTimeMillis()) + ".xlsx");

        return generator.getSuccessResult("导出成功！路径为: " + filePath);

    }

    /**
     * 增加一条自动化用例
     * @param testCase
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public RestResult addCase(@NotNull(message = "用例内容不能为空") @RequestBody AutoTestCase testCase) {
        AutoTestCase aCase = autoTestCaseService.hasCase(testCase);
        if(aCase != null) {
            return generator.getFailResult("用例已存在，请勿重复添加");
        }
        try {
            return generator.getSuccessResult("用例添加成功",autoTestCaseService.addCase(testCase));
        } catch (javax.validation.ConstraintViolationException e) {
            return generator.getFailResult("用例添加失败，请检查必填项是否为空");
        }
    }

    /**
     * 删除一条用例
     * @param testCase
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public RestResult deleteCase(@NotNull(message = "用例内容不能为空") AutoTestCase testCase){
//        AutoTestCase aCase = autoTestCaseService.hasCase(testCase);
        AutoTestCase aCase = autoTestCaseRepository.findByCaseNo(testCase.getCaseNo());
        logger.info("删除接口用例为：",testCase);
        if(aCase == null){
            return generator.getFailResult("用例不存在，请重新选择");
        }
        try {
            autoTestCaseRepository.delete(testCase.getId());
            return generator.getSuccessResult("删除用例成功", testCase);
        } catch (Exception e) {
            e.printStackTrace();
            return generator.getFailResult("用例删除失败，服务器内部错误");
        }
    }

    /**
     * 批量删除用例，根据ids=399,402,398,397
     * @return
     */
    @RequestMapping(value = "/del", method = RequestMethod.GET)
    public RestResult delCase(@RequestParam(value = "ids", defaultValue = "") String ids){
        String[] idsStr = ids.split(",");

        for (String id:idsStr) {
            try {
                autoTestCaseRepository.delete(Integer.parseInt(id));
            } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            }
        }

        return generator.getSuccessResult();
    }

    /**
     * 更新用例
     * @param testCase
     * @return
     */
    @RequestMapping(value = "/modify", method = RequestMethod.POST)
    public RestResult modifyCase(@RequestBody AutoTestCase testCase){
        try {
            return generator.getSuccessResult(autoTestCaseService.updateCase(testCase));
        } catch (Exception e){
            return generator.getFailResult("内部错误，修改失败");
        }
    }

    /**
     *
     * @param page
     * @param size
     * @return
     */
    @RequestMapping(value = "/cases", method = RequestMethod.GET)
    public RestResult findAllCase(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                  @RequestParam(value = "siez", defaultValue = "10") Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(page-1, size, sort);

        return generator.getSuccessResult(autoTestCaseRepository.findAll(pageable));
    }

}
