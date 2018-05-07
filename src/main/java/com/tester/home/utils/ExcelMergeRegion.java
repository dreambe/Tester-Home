package com.tester.home.utils;

public class ExcelMergeRegion {
    public boolean isMerged;
    public int startRow;
    public int endRow;
    public int startCol;
    public int endCol;

    public ExcelMergeRegion(boolean isMerged, int startRow, int endRow, int startCol, int endCol) {
        this.isMerged = isMerged;
        this.startRow = startRow;
        this.endRow = endRow;
        this.startCol = startCol;
        this.endCol = endCol;
    }
}
