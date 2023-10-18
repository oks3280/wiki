

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class excelLoadTest {

	@Test
	public void test()  throws Exception{
		//해당 파일은 업로드파일
        String             filePath          = "c:/test.xlsx";
        File               file              = new File( filePath );
        
        long start = System.currentTimeMillis(); //시작하는 시점 계산
        
//        지정 시트 조회[시트명]
//        List<ExcelSheetHandler>  excelSheetHandlers = ExcelSheetHandler.readSheets( file,new String[] {"시트3","시트1"} );
//        지정 시트 조회[인덱스]
        List<ExcelSheetHandler>  excelSheetHandlers = ExcelSheetHandler.readSheets( file,new int[] {1,2} );
//        전체 시트 조회
//        List<ExcelSheetHandler>  excelSheetHandlers = ExcelSheetHandler.readSheets( file );
        
        for(ExcelSheetHandler sheet: excelSheetHandlers){
        	System.out.println("시트명 ::"+sheet.getSheetName());
        	List<List<String>> sheetDatas        = sheet.getRows();
        	for ( List<String> dataRow : sheetDatas ) {
                System.out.println(dataRow);
            }
        }
        
        long end = System.currentTimeMillis(); //프로그램이 끝나는 시점 계산
        System.out.println( "실행 시간 : " + ( end - start ) / 1000.0 + "초" ); //실행 시간 계산 및 출력
	}

}
