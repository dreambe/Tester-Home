package com.tester.home.controller;

import com.tester.home.entity.RestResult;
import com.tester.home.entity.TestCase;
import com.tester.home.repository.TestCaseRepository;
import com.tester.home.service.TestCaseService;
import com.tester.home.utils.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.io.File;

@RestController
@RequestMapping("/case")
@Validated
public class TestCaseController extends BaseController{
    @Autowired
    private Environment envConfig;

    private final TestCaseService caseService;

    private final ResultGenerator generator;

    private static final long MAX_FILE_SIZE = 30*1024*1024;

    @Autowired
    public TestCaseController(TestCaseService caseService, ResultGenerator generator){
        this.caseService = caseService;
        this.generator = generator;
    }
    @Autowired
    TestCaseRepository testCaseRepository;

    /**
     * 上传文件接口，目前支持excel格式
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public RestResult upload(@RequestParam("file") MultipartFile file){
        logger.info("Begin To Upload");

        if(!file.isEmpty()){
//            String contentType = file.getContentType();
//            if(!contentType.contains("excel")){
//                return generator.getFailResult("文件类型错误，目前只支持excel");
//            }

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
            boolean b = caseService.batchImport(originFileName ,filePath);
            if(!b){
                return generator.getFailResult("上传成功，入库失败，文件名需要包含‘普通‘或者’自动化’以方便分类入库");
            }
        }

        return generator.getSuccessResult("上传成功，入库成功");
    }

    /**
     * 分页获取用例
     * @param page
     * @param size
     * @return
     */
    @RequestMapping(value = "list")
    public RestResult list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                           @RequestParam(value = "size", defaultValue = "10") Integer size){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(page-1, size, sort);

        return generator.getSuccessResult(testCaseRepository.findAll(pageable));
    }

    /**
     * 获取用例条数
     */
    @RequestMapping(value = "/num")
    public RestResult num() {
        try {
            return generator.getSuccessResult(caseService.countCase().toMap());
        }
        catch (Exception e) {
            e.printStackTrace();
            return generator.getFailResult("服务器内部错误, 查询失败");
        }
    }

    /**
     * 增加case接口
     */
    @RequestMapping(value = "/addcase", method = RequestMethod.POST)
    public RestResult addCase(@NotNull(message = "用例内容不能为空") TestCase testCase, BindingResult bindingResult) {
        TestCase aCase = caseService.hasCase(testCase.getTestcase());
        if(aCase != null) {
            return generator.getFailResult("用例已存在，请勿重复添加");
        }
        return generator.getSuccessResult("用例添加成功", caseService.addCase(testCase));
    }

    /**
     * 删除case接口
     */
    @RequestMapping(value = "/delcase", method = RequestMethod.DELETE)
    public RestResult delCase(@NotNull(message = "用例内容不能为空") TestCase testCase, BindingResult bindingResult) {
        TestCase aCase = caseService.hasCase(testCase.getTestcase());
        logger.info("删除接口用例为：",testCase);
        if(aCase == null) {
            return generator.getFailResult("用例不存在，请重新选择");
        }
        try {
            caseService.delCase(testCase);
            return generator.getSuccessResult("删除用例成功", testCase);
        } catch (Exception e) {
            e.printStackTrace();
            return generator.getFailResult("用例删除失败，服务器内部错误");
        }
    }

    /**
     * 修改case接口
     * @param testCase
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/modify", method = RequestMethod.PUT)
    public RestResult modify(@NotNull(message = "用例内容不能为空") TestCase testCase, BindingResult bindingResult) {
        TestCase aCase = caseService.hasCase(testCase.getTestcase());
        logger.info("修改接口用例为：",testCase);
        if(aCase != null) {
            return generator.getFailResult("用例已存在，请重新修改");
        }
        return generator.getSuccessResult("用例修改成功", caseService.modifyCase(testCase));
    }

    /**
     * 查询case接口
     * @param keyWord
     * @return
     */
    @RequestMapping(value = "/search")
    public RestResult query(@NotNull(message = "查询内容不能为空") String keyWord) {
        return generator.getSuccessResult(caseService.queryCase(keyWord));
    }

    /**
     * 为参数验证添加异常处理器
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public RestResult handleConstraintViolationException(ConstraintViolationException cve) {
        //这里简化处理了，cve.getConstraintViolations 会得到所有错误信息的迭代，可以酌情处理
        String errorMessage = cve.getConstraintViolations().iterator().next().getMessage();
        return generator.getFailResult(errorMessage);
    }

    /**
     * 主键/唯一约束违反异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public RestResult handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        //如果两个相同的id这个异常
        return generator.getFailResult("用例已存在！请勿重复添加");
    }
}

