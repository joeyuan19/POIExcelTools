/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 * Copyright 2013 Joseph Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


package excelUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelUtils {
	public static boolean ROW_CREATE_NULL_AS_BLANK = true;
	public static boolean CELL_CREATE_NULL_AS_BLANK = true;

	public static void copySheetSection(Sheet originSheet,
			int originRowStart, int originRowEnd,
			int originColStart, int originColEnd,
			Sheet destinationSheet, int pasteLocation) {
		//Row r; Cell c;
		int i, j;
		for (i = originRowStart; i < originRowEnd; i++) {
			for (j = originRowStart; j < originRowEnd; j++) {
				copyCell();
			}
		}

	}
	public static void copyCell(Cell origin, Cell destination) {
		copyCell(origin,destination,true);
	}
	public static void copyCell(Cell origin, Cell destination, boolean copyAll) {
		int originType = origin.getCellType();
		switch (originType) {
		case Cell.CELL_TYPE_BLANK:
			destination.setCellType(originType);
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			destination.setCellType(originType);
			destination.setCellValue(origin.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_ERROR:
			destination.setCellType(originType);
			destination.setCellValue(origin.getErrorCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA:
			destination.setCellType(originType);
			try {
				destination.setCellFormula(origin.getCellFormula());
				destination.setCellValue(origin.getCellFormula());
			} catch (Exception e1) {
				destination.setCellType(Cell.CELL_TYPE_ERROR);
				destination.setCellErrorValue(FormulaError.NA.getCode());
			}
			break;
		case Cell.CELL_TYPE_NUMERIC:
			try {
				destination.setCellType(origin.getCellType());
				if ( DateUtil.isValidExcelDate(origin.getNumericCellValue()) && 
						( DateUtil.isCellDateFormatted(origin) || DateUtil.isCellInternalDateFormatted(origin) )) {
					Date originDate = origin.getDateCellValue();
					try {
						destination.setCellType(Cell.CELL_TYPE_NUMERIC);
						destination.setCellValue((int)DateUtil.getExcelDate(originDate));
						//destination.setCellStyle(dateCellStyle);
						break;
					} catch (Exception e) {
						e.printStackTrace();
						String new_date = (new SimpleDateFormat("M/d/yyyy")).format(originDate);
						destination.setCellType(Cell.CELL_TYPE_STRING);
						destination.setCellValue(new_date);
						break;
					}
				}
				try {
					destination.setCellValue(origin.getNumericCellValue());
				} catch (IllegalStateException e) {
					/* Handle problematic numeric cell value */

					/* Strategy below is to see if the value can be extracted by placing an 
					 * unlikely canary number and seeing if it comes out the other end */

					/*
					long cv = -1974621803; // large random pandigital number to assert error
					try {
						cv = (long)origin.getNumericCellValue();
					} catch (IllegalStateException e1) {
						cv = Long.parseLong(origin.getStringCellValue());
						destination.setCellType(Cell.CELL_TYPE_BLANK);
						destination.setCellValue((String)null);
					}
					if (cv == -1974621803) {
						// failed
						// pass
						destination.setCellType(Cell.CELL_TYPE_BLANK);
						destination.setCellValue((String)null);
					} else {
						destination.setCellValue(cv);
					}
					 */
				}
				/* Ensure the cell is properly formatted */
				//destination.setCellStyle(numberCellStyle);
			} catch (Exception e) {
				//e.printStackTrace();
			}
			break;
		case Cell.CELL_TYPE_STRING:
			destination.setCellType(originType);
			destination.setCellValue(origin.getStringCellValue());
			break;
		}
		if (copyAll) {
			try {
				// If there is a cell comment, copy
				if (origin.getCellComment() != null) {
					destination.setCellComment(origin.getCellComment());
				}
			} catch(Exception e) {
				try {
					destination.setCellComment(null);
				} catch (Exception e2) {
					// pass
				}
			}
			try {
				// If there is a cell hyperlink, copy
				if (origin.getHyperlink() != null) {
					destination.setHyperlink(origin.getHyperlink());
				}
			} catch(Exception e) {
				try {
					destination.setHyperlink(null);
				} catch (Exception e2) {
					// pass
				}
			}
			try {
				//copyCellStyle(origin,destination);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return;
	}
	public static void copyCell() {

	}
	public static Cell clearCell(Cell c) {
		c.setCellType(Cell.CELL_TYPE_BLANK);
		return c;
	}
	public static Row clearRow(Row r) {
		for (Cell c: r) {
			clearCell(c);
		}
		return r;
	}
	public static Sheet clearSheet(Sheet s) {
		for (Row r: s) {
			clearRow(r);
		}
		return s;
	}
	/* Row retrieval methods */
	// By index number
	public static Row getRow(Sheet s, int row) {
		return getRow(s,row,false);
	}
	public static Row getRow(Sheet s, int row, boolean create_null_as_blank) {
		if (s == null) return null;
		Row r = s.getRow(row);
		if (r == null && create_null_as_blank) {
			r = s.createRow(row);
		}
		return r;
	}
	public static Cell getCell(Sheet s, int row, int col) {
		return getCell(s,row,col,false);
	}
	public static Cell getCell(Sheet s, int row, int col, boolean create_null_as_blank) {
		if (s == null) return null;
		Row r = getRow(s,row,create_null_as_blank);
		if (r == null) return null; 
		return create_null_as_blank ? getCell(r,col,create_null_as_blank) : getCell(r,col);
	}
	public static Cell getCell(Row r, int col) {
		return getCell(r,col,false);
	}
	public static Cell getCell(Row r, int col, boolean create_null_as_blank) {
		if (r == null) return null;
		return create_null_as_blank ? r.getCell(col,Row.CREATE_NULL_AS_BLANK) : r.getCell(col);
	} 
	// By Excel index
	public static Row getRow(Sheet s, String excelIndex) throws ExcelException {
		int row = getExcelRow(excelIndex);
		return getRow(s,row,false);
	}
	public static Row getRow(Sheet s, String excelIndex, boolean create_null_as_blank) throws ExcelException {
		if (s == null) return null;
		int row = getExcelRow(excelIndex);
		Row r = s.getRow(row);
		if (r == null && create_null_as_blank) {
			r = s.createRow(row);
		}
		return r;
	}
	public static Cell getCell(Sheet s, String excelIndex) throws ExcelException {
		int row = getExcelRow(excelIndex), col = getExcelCol(excelIndex);
		return getCell(s,row,col,false);
	}
	public static Cell getCell(Sheet s, String excelIndex, boolean create_null_as_blank) throws ExcelException {
		int row = getExcelRow(excelIndex), col = getExcelCol(excelIndex);
		if (s == null) return null;
		Row r = getRow(s,row,create_null_as_blank);
		if (r == null) return null; 
		return create_null_as_blank ? r.getCell(col,Row.CREATE_NULL_AS_BLANK) : r.getCell(col);
	}
	public static Cell getCell(Row r, String excelIndex) throws ExcelException {
		int col = getExcelCol(excelIndex);
		return getCell(r,col,false);
	}
	public static Cell getCell(Row r, String excelIndex, boolean create_null_as_blank) throws ExcelException {
		int col = getExcelCol(excelIndex);
		if (r == null) return null;
		return create_null_as_blank ? r.getCell(col,Row.CREATE_NULL_AS_BLANK) : r.getCell(col);
	} 

	/* Search methods */
	// Search for First Occurrence
	// Search by Sheet
	public static Cell searchSheet(Sheet s, String value) {
		return searchSheetByRow(s,s.getFirstRowNum(),s.getLastRowNum(),false,value,false);
	}
	public static Cell searchSheet(Sheet s, String value, boolean matchPartial) {
		return searchSheetByRow(s,s.getFirstRowNum(),s.getLastRowNum(),false,value,matchPartial);
	}
	public static Cell searchSheet(Sheet s, boolean ignoreCase, String value) {
		return searchSheetByRow(s,s.getFirstRowNum(),s.getLastRowNum(),ignoreCase,value,false);
	}
	// Search Range of Rows
	public static Cell searchSheetByRow(Sheet s, int rowMin, int rowMax, String value) {
		return searchSheetByRow(s,rowMin,rowMax,false,value,false);
	}
	public static Cell searchSheetByRow(Sheet s, int rowMin, int rowMax, boolean ignoreCase, String value) {
		return searchSheetByRow(s,rowMin,rowMax,ignoreCase,value,false);
	}
	public static Cell searchSheetByRow(Sheet s, int rowMin, int rowMax, String value, boolean matchPartial) {
		return searchSheetByRow(s,rowMin,rowMax,false,value,matchPartial);
	}
	// Search Single row
	public static Cell searchSheetByRow(Sheet s, int row, String value) {
		return searchSheetByRow(s,row,row,false,value,false);
	}
	public static Cell searchSheetByRow(Sheet s, int row, String value, boolean matchPartial) {
		return searchSheetByRow(s,row,row,false,value,matchPartial);
	}
	public static Cell searchSheetByRow(Sheet s, int row, boolean ignoreCase, String value) {
		return searchSheetByRow(s,row,row,ignoreCase,value,false);
	}
	// Search a Row Explicitly
	public static Cell searchSheetByRow(Sheet s, int rowMin, int rowMax, boolean ignoreCase, String value, boolean matchPartial) {
		int i; Row r; Cell c;
		rowMin = rowMin < 0 ? 0 : rowMin;
		rowMin = rowMin > s.getLastRowNum() ? s.getLastRowNum() : rowMin;
		rowMax = rowMax < 0 ? 0 : rowMax;
		rowMax = rowMax > s.getLastRowNum() ? s.getLastRowNum() : rowMax;
		for (i = rowMin; i <= rowMax; i++) {
			r = getRow(s,i);
			if ((c = searchRow(r, r.getFirstCellNum(), r.getLastCellNum(), ignoreCase, value, matchPartial)) != null) {
				return c;
			}	
		}
		return null;
	}
	// Search Range of cols
	public static Cell searchSheetByCol(Sheet s, int colMin, int colMax, String value) {
		return searchSheetByCol(s,colMin,colMax,false,value,false);
	}
	public static Cell searchSheetByCol(Sheet s, int colMin, int colMax, boolean ignoreCase, String value) {
		return searchSheetByCol(s,colMin,colMax,ignoreCase,value,false);
	}
	public static Cell searchSheetByCol(Sheet s, int colMin, int colMax, String value, boolean matchPartial) {
		return searchSheetByCol(s,colMin,colMax,false,value,matchPartial);
	}
	// Search Single col
	public static Cell searchSheetByCol(Sheet s, int col, String value) {
		return searchSheetByCol(s,col,col,false,value,false);
	}
	public static Cell searchSheetByCol(Sheet s, int col, String value, boolean matchPartial) {
		return searchSheetByCol(s,col,col,false,value,matchPartial);
	}
	public static Cell searchSheetByCol(Sheet s, int col, boolean ignoreCase, String value) {
		return searchSheetByCol(s,col,col,ignoreCase,value,false);
	}
	// Search a Col Explicitly
	public static Cell searchSheetByCol(Sheet s, int colMin, int colMax, boolean ignoreCase, String value, boolean matchPartial) {
		int i; Row r; Cell c;
		colMin = colMin < 0 ? 0 : colMin;
		colMin = colMin > s.getLastRowNum() ? s.getLastRowNum() : colMin;
		colMax = colMax < 0 ? 0 : colMax;
		colMax = colMax > s.getLastRowNum() ? s.getLastRowNum() : colMax;
		for (i = s.getFirstRowNum(); i <= s.getLastRowNum(); i++) {
			r = getRow(s,i);
			if ((c = searchRow(r, colMin, colMax, ignoreCase, value, matchPartial)) != null) {
				return c;
			}	
		}
		return null;
	}
	// Search Section of Cells
	public static Cell searchSheetBySection(Sheet s, int rowMin, int rowMax, int colMin, int colMax, String value) {
		return searchSheetBySection(s,rowMin,rowMax,colMin,colMax,false,value,false);
	}
	public static Cell searchSheetBySection(Sheet s, int rowMin, int rowMax, int colMin, int colMax, boolean ignoreCase, String value) {
		return searchSheetBySection(s,rowMin,rowMax,colMin,colMax,ignoreCase,value,false);
	}
	public static Cell searchSheetBySection(Sheet s, int rowMin, int rowMax, int colMin, int colMax, String value, boolean matchPartial) {
		return searchSheetBySection(s,rowMin,rowMax,colMin,colMax,false,value,matchPartial);
	}
	// Search a Section of Sheet Explicitly
	public static Cell searchSheetBySection(Sheet s, int rowMin, int rowMax, int colMin, int colMax, boolean ignoreCase, String value, boolean matchPartial) {
		int i; Row r; Cell c;
		rowMin = rowMin < 0 ? 0 : rowMin;
		rowMin = rowMin > s.getLastRowNum() ? s.getLastRowNum() : rowMin;
		rowMax = rowMax < 0 ? 0 : rowMax;
		rowMax = rowMax > s.getLastRowNum() ? s.getLastRowNum() : rowMax;
		for (i = rowMin; i <= rowMax; i++) {
			r = getRow(s,i);
			if ((c = searchRow(r, colMin, colMax, ignoreCase, value, matchPartial)) != null) {
				return c;
			}
		}
		return null;
	}
	// Search Entire Row
	public static Cell searchRow(Row r, String value) {
		return searchRow(r,r.getFirstCellNum(),r.getLastCellNum(),false,value,false);
	}
	public static Cell searchRow(Row r, String value, boolean matchPartial) {
		return searchRow(r,r.getFirstCellNum(),r.getLastCellNum(),false,value,matchPartial);
	}
	public static Cell searchRow(Row r, boolean ignoreCase, String value) {
		return searchRow(r,r.getFirstCellNum(),r.getLastCellNum(),ignoreCase,value,false);
	}
	// Search Range of Row
	public static Cell searchRow(Row r, int colMin, int colMax, String value) {
		return searchRow(r,colMin,colMax,false,value,false);
	}
	public static Cell searchRow(Row r, int colMin, int colMax, boolean ignoreCase, String value) {
		return searchRow(r,colMin,colMax,ignoreCase,value,false);
	}
	public static Cell searchRow(Row r, int colMin, int colMax, String value, boolean matchPartial) {
		return searchRow(r,colMin,colMax,false,value,matchPartial);
	}
	// Search a Single Cell
	public static Cell searchRow(Row r, int col, String value) {
		return searchRow(r,col,col,false,value,false);
	}
	public static Cell searchRow(Row r, int col, boolean ignoreCase, String value) {
		return searchRow(r,col,col,false,value,false);
	}
	public static Cell searchRow(Row r, int col, String value, boolean matchPartial) {
		return searchRow(r,col,col,false,value,false);
	}
	// Search Row Explicitly
	public static Cell searchRow(Row r, int colMin, int colMax, boolean ignoreCase, String value, boolean matchPartial) {
		int j; Cell c;
		colMin = colMin < 0 ? 0 : colMin;
		colMin = colMin > r.getLastCellNum() ? r.getLastCellNum() : colMin;
		colMax = colMax < 0 ? 0 : colMax;
		colMax = colMax > r.getLastCellNum() ? r.getLastCellNum() : colMax;
		for (j = colMin; j <= colMax; j++) {
			c = getCell(r,j);
			if (checkCellValue(c,ignoreCase,value,matchPartial) ) {
				return c;
			}
		}
		return null;
	}

	// Search for multiple cells
	// Search Across Sheet
	public static ArrayList<Cell> searchSheetAll(Sheet s, String value) {
		return searchSheetByRowAll(s,s.getFirstRowNum(),s.getLastRowNum(),false,value,false);
	}
	public static ArrayList<Cell> searchSheetAll(Sheet s, String value, boolean matchPartial) {
		return searchSheetByRowAll(s,s.getFirstRowNum(),s.getLastRowNum(),false,value,matchPartial);
	}
	public static ArrayList<Cell> searchRowAll(Sheet s, boolean ignoreCase, String value) {
		return searchSheetByRowAll(s,s.getFirstRowNum(),s.getLastRowNum(),ignoreCase,value,false);
	}
	// Search Across Rows
	public static ArrayList<Cell> searchSheetByRowAll(Sheet s, int rowMin, int rowMax, String value) {
		return searchSheetByRowAll(s,rowMin,rowMax,false,value,false);
	}
	public static ArrayList<Cell> searchSheetByRowAll(Sheet s, int rowMin, int rowMax, boolean ignoreCase, String value) {
		return searchSheetByRowAll(s,rowMin,rowMax,ignoreCase,value,false);
	}
	public static ArrayList<Cell> searchSheetByRowAll(Sheet s, int rowMin, int rowMax, String value, boolean matchPartial) {
		return searchSheetByRowAll(s,rowMin,rowMax,false,value,matchPartial);
	}
	// Search Single Row
	public static ArrayList<Cell> searchSheetByRowAll(Sheet s, int row, String value) {
		return searchSheetByRowAll(s,row,row,false,value,false);
	}
	public static ArrayList<Cell> searchSheetByRowAll(Sheet s, int row, String value, boolean matchPartial) {
		return searchSheetByRowAll(s,row,row,false,value,matchPartial);
	}
	public static ArrayList<Cell> searchSheetByRowAll(Sheet s, int row, boolean ignoreCase, String value) {
		return searchSheetByRowAll(s,row,row,ignoreCase,value,false);
	}
	public static ArrayList<Cell> searchSheetByRowAll(Sheet s, int row, boolean ignoreCase, String value, boolean matchPartial) {
		return searchSheetByRowAll(s,row,row,ignoreCase,value,matchPartial);
	}
	// Search Rows Explicitly
	public static ArrayList<Cell> searchSheetByRowAll(Sheet s, int rowMin, int rowMax, boolean ignoreCase, String value, boolean matchPartial) {
		int i; Row r;
		rowMin = rowMin < 0 ? 0 : rowMin;
		rowMin = rowMin > s.getLastRowNum() ? s.getLastRowNum() : rowMin;
		rowMax = rowMax < 0 ? 0 : rowMax;
		rowMax = rowMax > s.getLastRowNum() ? s.getLastRowNum() : rowMax;
		ArrayList<Cell> cells = new ArrayList<Cell>();
		for (i = rowMin; i <= rowMax; i++) {
			r = getRow(s,i);
			cells.addAll(searchRowAll(r, r.getFirstCellNum(), r.getLastCellNum(), ignoreCase, value, matchPartial));	
		}
		return cells;
	}
	// Search Across a Single col
	public static ArrayList<Cell> searchSheetByColAll(Sheet s, int col, String value) {
		return searchSheetByColAll(s,col,col,false,value,false);
	}
	public static ArrayList<Cell> searchSheetByColAll(Sheet s, int col, String value, boolean matchPartial) {
		return searchSheetByColAll(s,col,col,false,value,matchPartial);
	}
	public static ArrayList<Cell> searchSheetByColAll(Sheet s, int col, boolean ignoreCase, String value) {
		return searchSheetByColAll(s,col,col,ignoreCase,value,false);
	}
	public static ArrayList<Cell> searchSheetByColAll(Sheet s, int col, boolean ignoreCase, String value, boolean matchPartial) {
		return searchSheetByColAll(s,col,col,ignoreCase,value,matchPartial);
	}
	// Search Across cols
	public static ArrayList<Cell> searchSheetByColAll(Sheet s, int colMin, int colMax, String value) {
		return searchSheetByColAll(s,colMin,colMax,false,value,false);
	}
	public static ArrayList<Cell> searchSheetByColAll(Sheet s, int colMin, int colMax, boolean ignoreCase, String value) {
		return searchSheetByColAll(s,colMin,colMax,ignoreCase,value,false);
	}
	public static ArrayList<Cell> searchSheetByColAll(Sheet s, int colMin, int colMax, String value, boolean matchPartial) {
		return searchSheetByColAll(s,colMin,colMax,false,value,matchPartial);
	}
	// Search Across cols Explicitly
	public static ArrayList<Cell> searchSheetByColAll(Sheet s, int colMin, int colMax, boolean ignoreCase, String value, boolean matchPartial) {
		int i; Row r;
		colMin = colMin < 0 ? 0 : colMin;
		colMin = colMin > s.getLastRowNum() ? s.getLastRowNum() : colMin;
		colMax = colMax < 0 ? 0 : colMax;
		colMax = colMax > s.getLastRowNum() ? s.getLastRowNum() : colMax;
		ArrayList<Cell> cells = new ArrayList<Cell>();
		for (i = s.getFirstRowNum(); i <= s.getLastRowNum(); i++) {
			r = getRow(s,i);
			cells.addAll(searchRowAll(r, colMin, colMax, ignoreCase, value, matchPartial));	
		}
		return cells;
	}
	// Search a Region of a Single row
	public static ArrayList<Cell> searchSheetByRowSectionAll(Sheet s, int row, int colMin, int colMax, String value) {
		return searchRowAll(s.getRow(row),colMin,colMax,false,value,false);
	}
	public static ArrayList<Cell> searchSheetByRowSectionAll(Sheet s, int row, int colMin, int colMax, boolean ignoreCase, String value) {
		return searchRowAll(s.getRow(row),colMin,colMax,ignoreCase,value,false);
	}
	public static ArrayList<Cell> searchSheetByRowSectionAll(Sheet s, int row, int colMin, int colMax, String value, boolean matchPartial) {
		return searchRowAll(s.getRow(row),colMin,colMax,false,value,matchPartial);
	}
	public static ArrayList<Cell> searchSheetByRowSectionAll(Sheet s, int row, int colMin, int colMax, boolean ignoreCase, String value, boolean matchPartial) {
		return searchRowAll(s.getRow(row),colMin,colMax,false,value,matchPartial);
	}
	// Search a Region of a Single col
	public static ArrayList<Cell> searchSheetByColSectionAll(Sheet s, int rowMin, int rowMax, int col, String value) {
		return searchSheetBySectionAll(s,rowMin,rowMax,col,col,false,value,false);
	}
	public static ArrayList<Cell> searchSheetByColSectionAll(Sheet s, int rowMin, int rowMax, int col, boolean ignoreCase, String value) {
		return searchSheetBySectionAll(s,rowMin,rowMax,col,col,ignoreCase,value,false);
	}
	public static ArrayList<Cell> searchSheetByColSectionAll(Sheet s, int rowMin, int rowMax, int col, String value, boolean matchPartial) {
		return searchSheetBySectionAll(s,rowMin,rowMax,col,col,false,value,matchPartial);
	}
	public static ArrayList<Cell> searchSheetBySectionAll(Sheet s, int rowMin, int rowMax, int col, boolean ignoreCase, String value, boolean matchPartial) {
		return searchSheetBySectionAll(s,rowMin,rowMax,col,col,ignoreCase,value,matchPartial);
	}
	// Search a section of rows x cols
	public static ArrayList<Cell> searchSheetBySectionAll(Sheet s, int rowMin, int rowMax, int colMin, int colMax, String value) {
		return searchSheetBySectionAll(s,rowMin,rowMax,colMin,colMax,false,value,false);
	}
	public static ArrayList<Cell> searchSheetBySectionAll(Sheet s, int rowMin, int rowMax, int colMin, int colMax, String value, boolean matchPartial) {
		return searchSheetBySectionAll(s,rowMin,rowMax,colMin,colMax,false,value,matchPartial);
	}
	public static ArrayList<Cell> searchSheetBySectionAll(Sheet s, int rowMin, int rowMax, int colMin, int colMax, boolean ignoreCase, String value) {
		return searchSheetBySectionAll(s,rowMin,rowMax,colMin,colMax,ignoreCase,value,false);
	}
	// Search Sheet section explicitly
	public static ArrayList<Cell> searchSheetBySectionAll(Sheet s, int rowMin, int rowMax, int colMin, int colMax, boolean ignoreCase, String value, boolean matchPartial) {
		int i; Row r;
		rowMin = rowMin < 0 ? 0 : rowMin;
		rowMin = rowMin > s.getLastRowNum() ? s.getLastRowNum() : rowMin;
		rowMax = rowMax < 0 ? 0 : rowMax;
		rowMax = rowMax > s.getLastRowNum() ? s.getLastRowNum() : rowMax;
		ArrayList<Cell> cells = new ArrayList<Cell>();
		for (i = rowMin; i <= rowMax; i++) {
			r = getRow(s,i);
			cells.addAll(searchRowAll(r, colMin, colMax, ignoreCase, value, matchPartial));
		}
		return cells;
	}
	// Search Entire row
	public static ArrayList<Cell> searchRowAll(Row r, String value) {
		return searchRowAll(r,r.getFirstCellNum(),r.getLastCellNum(),false,value,false);
	}
	public static ArrayList<Cell> searchRowAll(Row r, String value, boolean matchPartial) {
		return searchRowAll(r,r.getFirstCellNum(),r.getLastCellNum(),false,value,matchPartial);
	}
	public static ArrayList<Cell> searchRowAll(Row r, boolean ignoreCase, String value) {
		return searchRowAll(r,r.getFirstCellNum(),r.getLastCellNum(),ignoreCase,value,false);
	}
	public static ArrayList<Cell> searchRowAll(Row r, boolean ignoreCase, String value, boolean matchPartial) {
		return searchRowAll(r,r.getFirstCellNum(),r.getLastCellNum(),ignoreCase,value,matchPartial);
	}
	// Search Row Section
	public static ArrayList<Cell> searchRowAll(Row r, int colMin, int colMax, String value) {
		return searchRowAll(r,colMin,colMax,false,value,false);
	}
	public static ArrayList<Cell> searchRowAll(Row r, int colMin, int colMax, String value, boolean matchPartial) {
		return searchRowAll(r,colMin,colMax,false,value,matchPartial);
	}
	public static ArrayList<Cell> searchRowAll(Row r, int colMin, int colMax, boolean ignoreCase, String value) {
		return searchRowAll(r,colMin,colMax,ignoreCase,value,false);
	}
	// Search Row Explcitly
	public static ArrayList<Cell> searchRowAll(Row r, int colMin, int colMax, boolean ignoreCase, String value, boolean matchPartial) {
		int j; Cell c;
		colMin = colMin < 0 ? 0 : colMin;
		colMin = colMin > r.getLastCellNum() ? r.getLastCellNum() : colMin;
		colMax = colMax < 0 ? 0 : colMax;
		colMax = colMax > r.getLastCellNum() ? r.getLastCellNum() : colMax;
		ArrayList<Cell> cells = new ArrayList<Cell>();
		for (j = colMin; j <= colMax; j++) {
			c = getCell(r,j);
			if (checkCellValue(c,ignoreCase,value,matchPartial)) {
				cells.add(c);
			}
		}
		return cells;
	}


	/* Comparing Cell Values */
	// String: match whole string
	// Check Cell on a Sheet
	public static boolean checkCellValue(Sheet s, int row, int cell, String value) {
		return (value == null && s == null) ||
				(checkCellValue(s.getRow(row), cell, false, value, false));
	}
	public static boolean checkCellValue(Sheet s, int row, int cell, boolean ignoreCase, String value) {
		return checkCellValue(s.getRow(row), cell, ignoreCase, value, false);
	}
	public static boolean checkCellValue(Sheet s, int row, int cell, String value, boolean matchPartial) {
		return checkCellValue(s.getRow(row), cell, false, value, matchPartial);
	}
	public static boolean checkCellValue(Sheet s, int row, int cell, boolean ignoreCase, String value, boolean matchPartial) {
		return (value == null && s == null) ||
				(checkCellValue(s.getRow(row), cell, ignoreCase, value, matchPartial));
	}
	// Check Cell in a Row
	public static boolean checkCellValue(Row r, int cell, String value) {
		return checkCellValue(r,cell,false,value,false);
	}
	public static boolean checkCellValue(Row r, int cell, String value, boolean matchPartial) {
		return (value == null && r == null) ||
				(checkCellValue(r.getCell(cell), false, value, matchPartial));
	}
	public static boolean checkCellValue(Row r, int cell, boolean ignoreCase, String value) {
		return (value == null && r == null) ||
				(checkCellValue(r.getCell(cell), ignoreCase, value, false));
	}
	public static boolean checkCellValue(Row r, int cell, boolean ignoreCase, String value, boolean matchPartial) {
		return (value == null && r == null) ||
				(checkCellValue(r.getCell(cell), ignoreCase, value, matchPartial));
	}
	// Check Individual Cell
	public static boolean checkCellValue(Cell c, String value) {
		return checkCellValue(c,false,value,false);
	}
	public static boolean checkCellValue(Cell c, boolean ignoreCase, String value) {
		return checkCellValue(c,ignoreCase,value,false);
	}
	public static boolean checkCellValue(Cell c, String value, boolean matchPartial) {
		return checkCellValue(c,false,value,matchPartial);
	}
	public static boolean checkCellValue(Cell c, boolean ignoreCase, String value, boolean matchPartial) {
		if (value == null && checkCellType(c,Cell.CELL_TYPE_BLANK)) {
			return true; // Should match null strings to blank cells as well
		} else if (checkCellType(c,Cell.CELL_TYPE_STRING)) {
			if (ignoreCase) {
				return matchPartial ?
						c.getStringCellValue().toLowerCase().contains(value.toLowerCase()):
							c.getStringCellValue().toLowerCase().equals(value.toLowerCase());
			} else {
				return matchPartial ?
						c.getStringCellValue().contains(value):
							c.getStringCellValue().equals(value);
			}
		}
		return false;
	}
	// String: Match partial
	// Numeric
	public static boolean checkCellValue(Sheet s, int row, int cell, Number value) {
		return checkCellValue(s,row,cell,value);
	}
	public static boolean checkCellValue(Row r, int cell, Number value) {
		return checkCellValue(r,cell,value);
	}
	public static boolean checkCellValue(Cell c, Number value) {
		return checkCellType(c,Cell.CELL_TYPE_NUMERIC) && value.equals(c.getNumericCellValue()); 
	}
	// Boolean
	public static boolean checkCellValue(Sheet s, int row, int cell, boolean value) {
		return checkCellValue(s,row,cell,value);
	}
	public static boolean checkCellValue(Row r, int cell, boolean value) {
		return checkCellValue(r,cell,value);
	}
	public static boolean checkCellValue(Cell c, boolean value) {
		return checkCellType(c,Cell.CELL_TYPE_BOOLEAN) && value == c.getBooleanCellValue(); 
	}

	/* Checking cell types */
	public static boolean checkCellType(Sheet s, int row, int cell, int type) {
		if (s == null) {
			return type == Cell.CELL_TYPE_BLANK;
		} else {
			return checkCellType(s.getRow(row),cell,type);
		}
	}
	public static boolean checkCellType(Row r, int cell, int type) {
		if (r == null) { 
			return type == Cell.CELL_TYPE_BLANK;
		} else {
			return checkCellType(r.getCell(cell),type);
		}
	}
	public static boolean checkCellType(Cell c, int type) {
		return (type == Cell.CELL_TYPE_BLANK            // (Type is blank and cell 
				&&										//  AND
				(c == null || c.getCellType() == type)) //  is either null or blank)
				|| 										// OR
				(c != null && c.getCellType() == type); // Cell is not null
		// and matches type
	}
	/* Excel assertion methods */
	public static boolean isValidExcelIndex(String index) {
		char c; int i, L = index.length();
		String numberIndex = "";
		for (i = 0; i < L; i++) {
			c = index.charAt(i);
			if (isAlpha(c)) {
				if (numberIndex.length() != 0) {
					return false;
				}
			} else if (isNumeric(c)) {
				numberIndex += c;
			} else {
				return false;
			}
		}
		if (numberIndex.length() == 1) {
			return !(numberIndex.charAt(0) == '0');
		}
		return true;
	}
	private static boolean isUpperAlpha(char c) {
		return 'A' <= c && 'Z' >= c;
	}
	private static boolean isLowerAlpha(char c) {
		return 'a' <= c && 'z' >= c;
	}
	private static boolean isAlpha(char c) {
		return isUpperAlpha(c) || isLowerAlpha(c);
	}
	private static boolean isNumeric(char c) {
		return '0' <= c && '9' >= c;
	}
	/* retrieve Excel rows and cols */
	public static int getExcelRow(String excelIndex) throws ExcelException{
		String rowIndex = ""; char c; int i, L = excelIndex.length();
		for (i = 0; i < L; i++) {
			c = excelIndex.charAt(i);
			if (isAlpha(c) && rowIndex.length() > 0) { 
				throw new ExcelException("Invalid Excel Cell address Given: " + excelIndex +"."+
						"  Excel Indexes must have the letter column before any row number");
			}
			if (isNumeric(c)) {
				rowIndex += c;
			}
		}
		if (rowIndex.length() == 1 && rowIndex.charAt(0) == '0') {
			throw new ExcelException("Invalid Excel Cell address Given: " + excelIndex + "."+
					"  Rows on Excel Sheets begin at 1 and cannot be 0.");
		} else if (rowIndex.length() == 0) {
			return -1;
		}
		return convertRowToInt(rowIndex);
	}
	public static int getExcelCol(String excelIndex) throws ExcelException {
		if (!isValidExcelIndex(excelIndex)) throw new ExcelException("Invalid Excel Index: " + excelIndex);
		String colIndex = ""; char c; int i, L = excelIndex.length();
		for (i = 0; i < L; i++) {
			c = excelIndex.charAt(i);
			if (isAlpha(c)) { 
				colIndex += c;
			}
			if (isNumeric(c)) {
				break;
			}
		}
		if (colIndex.length() == 0) {
			return -1;
		}
		return convertColToInt(colIndex);
	}
	/* Convert between: row/col <-> numbers/letters */
	public static String convertToExcelIndex(int row, int col) {
		return convertIntToCol(col) + convertIntToRow(row);
	}
	// Returns as [row,col]
	public static int[] convertToIndices(String excelIndex) throws ExcelException {
		if (!isValidExcelIndex(excelIndex)) throw new ExcelException("Invalid Excel Index: " + excelIndex);
		int[] coords = {getExcelRow(excelIndex),getExcelCol(excelIndex)};
		return coords;
	}
	public static int convertRowToInt(String row) {
		return Integer.parseInt(row)-1;
	}
	public static String convertIntToRow(int row) {
		return ""+(row+1);
	}
	public static int convertColToInt(String col) {
		String name = col.toUpperCase();
		int number = 0;
		int pow = 1;
		for (int i = name.length() - 1; i >= 0; i--)
		{
			number += (name.charAt(i) - 'A' + 1) * pow;
			pow *= 26;
		}

		return number-1;
	}
	public static String convertIntToCol(int colNum) {
		int col = colNum + 1;
		String retVal = "";
		int x = 0;
		for (int n = (int)(Math.log(25*(col + 1))/Math.log(26)) - 1; n >= 0; n--)
		{
			x = (int)((Math.pow(26,(n + 1)) - 1) / 25 - 1);
			if (col > x)
				retVal += (char) ((int)((col - x - 1) / Math.pow(26, n)) % 26 + 65);
		}
		return retVal;
	}


	/* Refactor row formulas */
	// properly refactor an excel formulat on a row change
	public static String formulaRowRefactor(String formula, int sourceRow, int copyRow) {
		String buf = "";
		String new_formula = "";
		int i;
		char c;
		boolean skipNext = false, inParen = false;
		for (i = 0; i < formula.length(); i++) {
			c = formula.charAt(i);
			if (c == '\'') {
				if (buf.length() > 0 && buf.length() < 4 && i-buf.length()-1 >= 0 && isUpperAlpha(formula.charAt(i-buf.length()-1))) {
					if (!skipNext) {
						new_formula += carefulRowFormulaRefactorString(buf,sourceRow,copyRow);
						buf = "";
					} else {
						new_formula += buf;
						skipNext = false;
						buf = "";
					}
				} else {
					new_formula += buf;
					buf = "";
				}
				inParen = (inParen ? false : true);
				new_formula += c;
			} else if (!inParen) {
				if (c == '$') {
					if (buf.length() > 0 && buf.length() < 4 && i-buf.length()-1 >= 0 && isUpperAlpha(formula.charAt(i-buf.length()-1))) {
						if (!skipNext) {
							new_formula += carefulColFormulaRefactorString(buf,sourceRow,copyRow);
							buf = "";
						} else {
							new_formula += buf;
							skipNext = false;
							buf = "";
						}
					} else {
						new_formula += buf;
						buf = "";
					}
					skipNext = true;
					new_formula += c;
				} else if (skipNext) {
					if (!isNumeric(c)) {
						skipNext = false;
					}
					new_formula += c;
				} else {
					if (isNumeric(c)) {
						buf += c;
					} else {
						if (buf.length() > 0 && i-buf.length()-1 >= 0 && isUpperAlpha(formula.charAt(i-buf.length()-1))) {
							new_formula += carefulRowFormulaRefactorString(buf,sourceRow,copyRow);
							buf = "";
						} else {
							new_formula += buf;
							buf = "";
						}
						new_formula += c;
					}
				}
			} else {
				new_formula += c;
			}
		}
		if (!skipNext && !inParen && buf.length() > 0 && i-buf.length()-1 >= 0 && isUpperAlpha(formula.charAt(i-buf.length()-1))) {
			new_formula += carefulRowFormulaRefactorString(buf,sourceRow,copyRow);
			buf = "";
		} else {
			new_formula += buf;
			buf = "";
		}
		return new_formula;
	}
	public static int carefulRowFormulaRefactorString(String formula, int sourceRow, int copyRow) {
		return copyRow + (Integer.parseInt(formula) - sourceRow);
	}
	public static String formulaColumnRefactor(String formula, int sourceCol, int copyCol) {
		String buf = "";
		String new_formula = "";
		int i;
		char c;
		boolean skipNext = false, inParen = false;
		for (i = 0; i < formula.length(); i++) {
			c = formula.charAt(i);
			if (c == '\'') {
				if (buf.length() > 0 && buf.length() < 4) {
					if (!skipNext) {
						new_formula += carefulColFormulaRefactorString(buf,sourceCol,copyCol);
						buf = "";
					} else {
						new_formula += buf;
						skipNext = false;
						buf = "";
					}
				} else {
					new_formula += buf;
					buf = "";
				}
				inParen = (inParen ? false : true);
				new_formula += c;
			} else if (!inParen) {
				if (c == '$') {
					if (buf.length() > 0 && buf.length() < 4) {
						if (!skipNext) {
							new_formula += carefulColFormulaRefactorString(buf,sourceCol,copyCol);
							buf = "";
						} else {
							new_formula += buf;
							skipNext = false;
							buf = "";
						}
					} else {
						new_formula += buf;
						buf = "";
					}
					skipNext = true;
					new_formula += c;
				} else if (skipNext) {
					if (!isUpperAlpha(c)) {
						skipNext = false;
					}
					new_formula += c;
				} else {
					if (isUpperAlpha(c)) {
						buf += c;
					} else {
						if (buf.length() > 0 && buf.length() < 4) {
							new_formula += carefulColFormulaRefactorString(buf,sourceCol,copyCol);
							buf = "";
						} else {
							new_formula += buf;
							buf = "";
						}
						new_formula += c;
					}
				}
			} else {
				new_formula += c;
			}
		}
		if (!skipNext && !inParen && buf.length() > 0 && buf.length() < 4) {
			new_formula += carefulRowFormulaRefactorString(buf,sourceCol,copyCol);
			buf = "";
		} else {
			new_formula += buf;
			buf = "";
		}
		return new_formula;
	}
	public static String carefulColFormulaRefactorString(String formula, int sourceCol, int copyCol) {
		return convertIntToCol(copyCol + (convertColToInt(formula) - sourceCol));
	}

	/* Get a string of information about the given cell */
	public static String getCellInfo(Cell cell) {
		if (cell == null) return "Cell is null";
		return "Cell " + convertIntToCol(cell.getColumnIndex()) + cell.getRowIndex() +  " on sheet " + cell.getSheet().getSheetName();
	}

	/* Handle saving workbook */
	public static boolean saveWorkbook(Workbook wb, String filename, String filepath, boolean makeDirs, boolean overwrite) throws Exception {
		// if directories in the path do not exist, create them
		if (makeDirs) {
			( new File(filepath) ).mkdirs();
		} else {
			if (!( new File(filepath) ).exists()) {
				return false;
			}
		}
		// Ensure the directory path ends with a separator
		if (! filepath.endsWith(File.separator)) {
			filepath = filepath + File.separator;
		}
		if (!FileUtils.hasProperExt(filename)) {
			filename += ".xls";
		}
		File file = new File(filepath + filename);
		if (overwrite) {
			FileUtils.deleteFile(file);
		}
		file = new File(filepath + filename);
		FileOutputStream output = new FileOutputStream(file);
		try {
			wb.write(output);
			wb = null;
			output.close();
			return true;
		} catch (Exception e) {
			output.close();
			throw e;
		}
	}
	public static void main(String args[]) {
		Workbook wb = new org.apache.poi.hssf.usermodel.HSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = s.createRow(0);
		Cell c = r.createCell(0);
		c.setCellType(Cell.CELL_TYPE_NUMERIC);
		c.setCellValue(9.9999);

		System.out.println(checkCellValue(c,9.9999));
		System.out.println(checkCellValue(c,9.999));
		System.out.println(checkCellValue(c,9));
		System.out.println(checkCellValue(c,(long)9.9999));
		System.out.println(checkCellValue(c,(int)10));
		System.out.println(checkCellValue(c,(float)9.9999));
		System.out.println(checkCellValue(c,(double)9.9999));
	}
}