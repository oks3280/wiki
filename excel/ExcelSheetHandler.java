import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.binary.XSSFBSheetHandler.SheetContentsHandler;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class ExcelSheetHandler implements SheetContentsHandler {

    private int currentCol = -1;
    private int currRowNum = 0;
    private String sheetName = "";

    String filePath = "";

    private List<List<String>> rows   = new ArrayList<>();    // 실제 엑셀을 파싱해서 담아지는 데이터
    private List<String>       row    = new ArrayList<>();
    private List<String>       header = new ArrayList<>();

    /**
     * 전체 시트 조회
     * */
    public static List<ExcelSheetHandler> readSheets( InputStream inputStream ) throws Exception {
    	return readSheets(OPCPackage.open(inputStream), new String[] {});
    }
    
    /**
     * 전체 시트 조회
     * */
    public static List<ExcelSheetHandler> readSheets( String filePath ) throws Exception {
    	return readSheets(OPCPackage.open(filePath), new String[] {});
    }
    
    /**
     * 전체 시트 조회
     * */
    public static List<ExcelSheetHandler> readSheets( File file ) throws Exception {
    	return readSheets(OPCPackage.open(file), new String[] {});
    }
    
    /**
     * 전체 시트 조회
     * */
    public static List<ExcelSheetHandler> readSheets( ZipEntrySource zipEntrySource ) throws Exception {
    	return readSheets(OPCPackage.open(zipEntrySource), new String[] {});
    }
    
    /**
     * 지정 시트 조회[시트명]
     * */
    public static List<ExcelSheetHandler> readSheets(InputStream inputStream, String ...sheetName ) throws Exception {
        return readSheets(OPCPackage.open(inputStream), sheetName);
    }
    
    /**
     * 지정 시트 조회[시트명]
     * */
    public static List<ExcelSheetHandler> readSheets(String filePath, String ...sheetName ) throws Exception {
    	return readSheets(OPCPackage.open(filePath), sheetName);
    }
    
    /**
     * 지정 시트 조회[시트명]
     * */
    public static List<ExcelSheetHandler> readSheets(File file, String ...sheetName ) throws Exception {
    	return readSheets(OPCPackage.open(file), sheetName);
    }
    
    /**
     * 지정 시트 조회[시트명]
     * */
    public static List<ExcelSheetHandler> readSheets(ZipEntrySource zipEntrySource, String ...sheetName ) throws Exception {
    	return readSheets(OPCPackage.open(zipEntrySource), sheetName);
    }
    
    /**
     * 지정 시트 조회[시트명]
     * */
    public static List<ExcelSheetHandler> readSheets( OPCPackage opc, String ...sheetName ) throws Exception {

        List<ExcelSheetHandler> sheetHandlers = new ArrayList<>();
        try {
            // org.apache.poi.xssf.eventusermodel.XSSFReader
            XSSFReader xssfReader = new XSSFReader(opc);

            // org.apache.poi.xssf.model.StylesTable
            StylesTable styles = xssfReader.getStylesTable();

            // org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opc);

//            ExcelSheetHandler        sheetHandler = null;
            InputStream              inputStream  = null;
            InputSource              inputSource  = null;
            ContentHandler           handle       = null;
            XSSFReader.SheetIterator sheets       = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

            while (sheets.hasNext()) {            	
                inputStream = sheets.next();
                
                if(sheetName.length==0||StringUtils.containsAny(sheets.getSheetName(), sheetName)) {
                	ExcelSheetHandler sheetHandler = new ExcelSheetHandler();
                    sheetHandler.setSheetName(sheets.getSheetName());
                    
                    // org.xml.sax.InputSource
                    inputSource = new InputSource(inputStream);

                    // org.xml.sax.Contenthandler
                    handle = new XSSFSheetXMLHandler(styles, strings, sheetHandler, false);

                    // XMLReader xmlReader = SAXHelper.newXMLReader(); // deprecated
                    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                    saxParserFactory.setNamespaceAware(true);
                    SAXParser parser    = saxParserFactory.newSAXParser();
                    XMLReader xmlReader = parser.getXMLReader();
                    xmlReader.setContentHandler(handle);

                    xmlReader.parse(inputSource);
                    inputStream.close();      
                    sheetHandlers.add(sheetHandler);
                }
            }

            opc.close();

        } catch (Exception e) {
            // 에러 발생했을때 하시고 싶은 TO-DO
        }

        return sheetHandlers;

    }// readExcel - end
    
    
    /**
     * 지정 시트 조회[인덱스]
     * */
    public static List<ExcelSheetHandler> readSheets(InputStream inputStream, int ...sheetIndex ) throws Exception {
    	return readSheets(OPCPackage.open(inputStream), sheetIndex);
    }
    
    /**
     * 지정 시트 조회[인덱스]
     * */
    public static List<ExcelSheetHandler> readSheets(String filePath, int ...sheetIndex ) throws Exception {
    	return readSheets(OPCPackage.open(filePath), sheetIndex);
    }
    
    /**
     * 지정 시트 조회[인덱스]
     * */
    public static List<ExcelSheetHandler> readSheets(File file, int ...sheetIndex ) throws Exception {
    	return readSheets(OPCPackage.open(file), sheetIndex);
    }
    
    /**
     * 지정 시트 조회[인덱스]
     * */
    public static List<ExcelSheetHandler> readSheets(ZipEntrySource zipEntrySource, int ...sheetIndex ) throws Exception {
    	return readSheets(OPCPackage.open(zipEntrySource), sheetIndex);
    }
    
    /**
     * 지정 시트 조회[인덱스]
     * */
    public static List<ExcelSheetHandler> readSheets( OPCPackage opc, int ...sheetIndex ) throws Exception {
    	
    	List<ExcelSheetHandler> sheetHandlers = new ArrayList<>();
    	try {
    		// org.apache.poi.xssf.eventusermodel.XSSFReader
    		XSSFReader xssfReader = new XSSFReader(opc);
    		
    		// org.apache.poi.xssf.model.StylesTable
    		StylesTable styles = xssfReader.getStylesTable();
    		
    		// org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
    		ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opc);
    		
//            ExcelSheetHandler        sheetHandler = null;
    		InputStream              inputStream  = null;
    		InputSource              inputSource  = null;
    		ContentHandler           handle       = null;
    		XSSFReader.SheetIterator sheets       = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
    		
    		int i=0;
    		while (sheets.hasNext()) {            	
    			inputStream = sheets.next();
    			
    			if(Arrays.binarySearch(sheetIndex, i)>=0) {
    				ExcelSheetHandler sheetHandler = new ExcelSheetHandler();
    				sheetHandler.setSheetName(sheets.getSheetName());
    				
    				// org.xml.sax.InputSource
    				inputSource = new InputSource(inputStream);
    				
    				// org.xml.sax.Contenthandler
    				handle = new XSSFSheetXMLHandler(styles, strings, sheetHandler, false);
    				
    				// XMLReader xmlReader = SAXHelper.newXMLReader(); // deprecated
    				SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    				saxParserFactory.setNamespaceAware(true);
    				SAXParser parser    = saxParserFactory.newSAXParser();
    				XMLReader xmlReader = parser.getXMLReader();
    				xmlReader.setContentHandler(handle);
    				
    				xmlReader.parse(inputSource);
    				inputStream.close();      
    				sheetHandlers.add(sheetHandler);
    			}
    			i++;
    		}
    		
    		opc.close();
    		
    	} catch (Exception e) {
    		// 에러 발생했을때 하시고 싶은 TO-DO
    	}
    	
    	return sheetHandlers;
    	
    }// readExcel - end

    public List<List<String>> getRows() {
        return rows;
    }

    @Override
    public void startRow( int arg0 ) {
        this.currentCol = -1;
        this.currRowNum = arg0;
    }

    @Override
    public void cell( String columnName, String value, XSSFComment var3 ) {
        int iCol     = (new CellReference(columnName)).getCol();
        int emptyCol = iCol - currentCol - 1;

        for ( int i = 0; i < emptyCol; i++ ) {
            row.add("");
        }
        currentCol = iCol;
        row.add(value);
    }

    @Override
    public void headerFooter( String arg0, boolean arg1, String arg2 ) {
        // 사용안합니다.
    }

    @Override
    public void endRow( int rowNum ) {
        if ( rowNum == 0 ) {
            header = new ArrayList(row);
        } else {
            if ( row.size() < header.size() ) {
                for ( int i = row.size(); i < header.size(); i++ ) {
                    row.add("");
                }
            }
            rows.add(new ArrayList(row));
        }
        row.clear();
    }

    @Override
    public void hyperlinkCell( String arg0, String arg1, String arg2, String arg3, XSSFComment arg4 ) {
        // TODO Auto-generated method stub

    }

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
}
