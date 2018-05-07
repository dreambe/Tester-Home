package com.tester.home.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tester.home.entity.AutoTestCase;
import com.tester.home.entity.TestCase;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

@Component
public class ExcelUtil {
    private int totalRows = 0;

    private int totalCells = 0;

    private String errorMsg;

    public ExcelUtil(){}

    public int getTotalRows() {return totalRows;}

    public int getTotalCells() {return totalCells;}

    public String getErrorMsg() {return errorMsg;}

    public Logger logger = LoggerFactory.getLogger((this.getClass()));

    /**
     * 校验是否为excel格式
     * @param filePath
     * @return
     */
    public boolean validteExcel(String filePath) {
        if (filePath == null || !(WDWUtil.isExcel2003(filePath) || WDWUtil.isExcel2007(filePath))) {
            errorMsg = "非excel格式，请上传excel格式";
            return false;
        }
        return true;
    }

    /**
     * 读取excel内容的每一行并返回List数组
     * @param fileName
     * @param filePath
     * @return
     */
    public List getCaseInfo(String fileName, String filePath) {
        List caseList = new ArrayList();
        InputStream stream = null;
        boolean isAutoTestCase = false;
        try {
            if(!validteExcel(fileName)) {
                return null;
            }
            boolean isExcel2003 = true;
            if(WDWUtil.isExcel2007(filePath)) {
                isExcel2003 = false;
            }

            if(fileName.contains("自动")){
                isAutoTestCase = true;
            }

            stream = new FileInputStream(filePath);

            try {
                Workbook wb = null;
                if(isExcel2003){
                    wb = new HSSFWorkbook(stream);
                }
                else {
                    wb = new XSSFWorkbook(stream);
                }

                List<Sheet> sheets = getSheetsFromExcel(wb);
                for (Sheet sheet:sheets){
                    List tempList = readExcelValue(sheet, isAutoTestCase);
                    caseList.addAll(tempList);
                }
//                caseList = readExcelValue(wb, isAutoTestCase);

            } catch (IOException e) {
                e.printStackTrace();
            }

            stream.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(stream != null){
                try {
                    stream.close();
                } catch (IOException e) {
                    stream = null;
                    e.printStackTrace();
                }
            }
        }

        return caseList;
    }

    /**
     * 写入Excel
     * @param listData
     * @param filePath
     * @return
     */
    public Boolean writeExcel(List listData, String filePath) {
        try {
            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("sheet1");

            // 做一次转换
            JSONArray jsonArray = new JSONArray();
            for (Object objs : listData){
                jsonArray.add(objs);
            }

            // 写表头
            Row titleRow = sheet.createRow(0);
            int column = 0;
            JSONObject first = jsonArray.getJSONObject(0);
            Iterator iterator = first.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String)iterator.next();

                titleRow.createCell(column).setCellValue(key);
                column++;
            }

            // 写数据
            for (int i=1; i<jsonArray.size(); i++){
                JSONObject item = jsonArray.getJSONObject(i);
                iterator = item.keySet().iterator();
                column = 0;
                Row currentRow = sheet.createRow(i);
                while (iterator.hasNext()){
                    String key = (String)iterator.next();
                    String value = item.getString(key);

                    currentRow.createCell(column).setCellValue(value);
                    column++;
                }
            }

            // 保存文件
            FileOutputStream fos = new FileOutputStream(filePath);
            wb.write(fos);
            fos.close();

            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private List<Sheet> getSheetsFromExcel(Workbook wb) {
        List<Sheet> sheets = new ArrayList<>();
        int numOfSheet = wb.getNumberOfSheets();
        for(int i=0; i<numOfSheet; i++){
            Sheet sheet = wb.getSheetAt(i);
            if(sheet.getLastRowNum() != 0){
                sheets.add(sheet);
            }
        }

        return sheets;
    }

    /**
     * 循环读取每一行的每一列单元格。并判断是否为合并单元格，不同处理，最后拼接字符串，写入数据库
     * @param sheet
     * @return
     */
    private List readExcelValue(Sheet sheet, boolean isAutoTestCase) {
//        Sheet sheet = wb.getSheetAt(0);

        this.totalRows = sheet.getPhysicalNumberOfRows();

        if(totalRows >= 1 && sheet.getRow(0) != null) {
            this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
        }

        List caseList = new ArrayList();
        TestCase testCase;
        AutoTestCase autoTestCase;

        // 自动化用例读法
        if(isAutoTestCase) {
//            autoTestCase = new AutoTestCase();
//            Map<String, Integer> titleRowMap = getTitleRow(sheet);
//            logger.info(titleRowMap);
//            System.out.println(titleRowMap);

            for(int i=1; i<=totalRows; i++){
                autoTestCase = new AutoTestCase();
                Row row = sheet.getRow(i);
                if(row == null) {
                    continue;
                }

//                autoTestCase.setPlatInfo(row.getCell(titleRowMap.get("PlatInfo")).toString().trim());
                autoTestCase.setPlatInfo(row.getCell(0).toString().trim());
                autoTestCase.setFeatureModule(row.getCell(1).toString().trim());
                autoTestCase.setCaseNo(row.getCell(2).toString().trim());
                autoTestCase.setDescription(row.getCell(3).toString().trim());
                autoTestCase.setApiInfo(row.getCell(4).toString().trim());
                autoTestCase.setMethod(row.getCell(5).toString().trim());
                autoTestCase.setParamsType(row.getCell(6).toString().trim());
                autoTestCase.setParams(row.getCell(7).toString().trim());
                autoTestCase.setUserInfo(row.getCell(8).toString().trim());
                autoTestCase.setCookie(row.getCell(9).toString().trim());
                autoTestCase.setToken(row.getCell(10).toString().trim());
                autoTestCase.setMysql(((row.getCell(11).toString().trim()).replace("'", "\"")).replaceAll("\\s*|\t|\r|\n",""));
                autoTestCase.setMongodb(row.getCell(12).toString().trim());
                autoTestCase.setRedis(row.getCell(13).toString().trim());
                autoTestCase.setExpectMysql(row.getCell(14).toString().trim());
                autoTestCase.setExpectResponse(row.getCell(15).toString().trim());
                autoTestCase.setClearMysql(row.getCell(16).toString().trim());
                autoTestCase.setClearRedis(row.getCell(17).toString().trim());
                autoTestCase.setIsIgnore(row.getCell(18).toString().trim());

                caseList.add(autoTestCase);
            }

        } else {
            // 普通用例读法
            // 循环读取行 第一行是标题不读取（要求Excle表格标准一些~，不标准打屁股）
            testCase = new TestCase();

            for(int i=1; i<=totalRows; i++){
                Row row = sheet.getRow(i);
                if(row == null) {
                    continue;
                }
                testCase = new TestCase();

                StringBuilder tempStr = new StringBuilder("测试用例 : ");
                // 循环当前行的每一列
                for(int j=0; j<this.totalCells; j++){
                    String value = null;
                    Cell cell = row.getCell(j);
                    if(cell == null) break;

                    if(isMergedRegion(sheet, i, j).isMerged){
                        int mergeRegionStartRow = isMergedRegion(sheet, i, j).startRow;
                        int mergeRegionStartCol = isMergedRegion(sheet, i, j).startCol;
                        Cell mergeCell = (sheet.getRow(mergeRegionStartRow)).getCell(mergeRegionStartCol);

                        value = getCellValue(mergeCell);
                    }
                    else {
                        value = getCellValue(cell);
                    }

                    tempStr.append(value);
                    // 最后一行不加 “==>>|” 拼接
                    if(this.totalCells-j > 1) {
                        tempStr.append(" ==>>| ");
                    }

                }
                // 插入用例
                testCase.setTestcase(tempStr.toString());
                caseList.add(testCase);
                tempStr = new StringBuilder("");
            }
        }

        return caseList;
    }

    /**
     * 获取表头，并将表头Map返回
     * @return
     */
    private Map getTitleRow(Sheet sheet){
        Map<String, Integer> titleMap = new HashMap<>();
        int columnCount = sheet.getRow(0).getLastCellNum();
        Row titleRow = sheet.getRow(0);

        for (int i = 0; i < columnCount; i++) {
            titleMap.put(titleRow.getCell(i).getStringCellValue() ,i);
        }

        return titleMap;
    }

    /**
     * 直接通过表头的字段获取当前行的内容
     * @return
     */
    private String getCellValueFromTitle(Map<String, Integer> titleRowMap,Row row, String titleName){
        return row.getCell(titleRowMap.get(titleName)).toString().trim();
    }

    /**
     * 获取单元格的值
     * @param cell
     * @return
     */
    private String getCellValue(Cell cell){
        if(cell == null) return "";
        if(cell.getCellType() == Cell.CELL_TYPE_STRING){
            return cell.getStringCellValue();
        }else if(cell.getCellType() == Cell.CELL_TYPE_BOOLEAN){
            return String.valueOf(cell.getBooleanCellValue());
        }else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA){
            return cell.getCellFormula() ;
        }else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
            return String.valueOf(cell.getNumericCellValue());
        }
        return "";
    }

    /**
     * 判断当前单元格是否为合并单元格，并返回开始、结束行列的实例
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    private ExcelMergeRegion isMergedRegion(Sheet sheet, int row, int column){
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if(row >= firstRow && row <= lastRow){
                if(column >= firstColumn && column <= lastColumn){
                    return new ExcelMergeRegion(true,firstRow,lastRow,firstColumn,lastColumn);
                }
            }
        }

        return new ExcelMergeRegion(false,0,0,0,0);
    }
}
