package com.tester.home.entity;

/**
 * 用例类型枚举
 */
public enum TestCaseType {
    GENERAL_CASE(0,"普通测试用例"),
    AUTOTEST_CASE(1,"自动化测试用例");

    TestCaseType(int type, String message){
        this.type = type;
    }
    private int type;
//
//    public static TestCaseTypeEnum getTestCaseTypeEnumByType(int type){
//        for (TestCaseTypeEnum e : TestCaseTypeEnum.values()){
//            if (e.getCodeType().equals(type)){
//                return e;
//            }
//        }
//
//        return null;
//    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
