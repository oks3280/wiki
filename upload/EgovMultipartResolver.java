import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EgovMultipartResolver extends CommonsMultipartResolver {
    public EgovMultipartResolver() {
    }

    @Override
    public boolean isMultipart(HttpServletRequest request) {

    	if("1".equals(request.getHeader("X-Large-Upload"))||"1".equals(this.getQueryParams(request.getQueryString(),"XLargeUpload"))) {
    		return false;
    	}
    	return super.isMultipart(request);
    }

    /**
     * 첨부파일 처리를 위한 multipart resolver를 생성한다.
     *
     * @param servletContext
     */
    public EgovMultipartResolver(ServletContext servletContext) {
	super(servletContext);
    }

    /**
     * multipart에 대한 parsing을 처리한다.
     */
    @SuppressWarnings("rawtypes")
	@Override
    protected MultipartParsingResult parseFileItems(List fileItems, String encoding) {

    //스프링 3.0변경으로 수정한 부분
    MultiValueMap<String, MultipartFile> multipartFiles = new LinkedMultiValueMap<String, MultipartFile>();
	Map<String, String[]> multipartParameters = new HashMap<String, String[]>();

	// Extract multipart files and multipart parameters.
	for (Iterator<?> it = fileItems.iterator(); it.hasNext();) {
	    FileItem fileItem = (FileItem)it.next();

	    if (fileItem.isFormField()) {

		String value = null;
		if (encoding != null) {
		    try {
			value = fileItem.getString(encoding);
		    } catch (UnsupportedEncodingException ex) {
			if (logger.isWarnEnabled()) {
			    logger.warn("Could not decode multipart item '" + fileItem.getFieldName() + "' with encoding '" + encoding
				    + "': using platform default");
			}
			value = fileItem.getString();
		    }
		} else {
		    value = fileItem.getString();
		}
		String[] curParam = multipartParameters.get(fileItem.getFieldName());
		if (curParam == null) {
		    // simple form field
		    multipartParameters.put(fileItem.getFieldName(), new String[] { value });
		} else {
		    // array of simple form fields
		    String[] newParam = StringUtils.addStringToArray(curParam, value);
		    multipartParameters.put(fileItem.getFieldName(), newParam);
		}
	    } else {

		if (fileItem.getSize() > 0) {
		    // multipart file field
		    CommonsMultipartFile file = new CommonsMultipartFile(fileItem);


		    //스프링 3.0 업그레이드 API변경으로인한 수정
		    List<MultipartFile> fileList = new ArrayList<MultipartFile>();
		    fileList.add(file);


		    if (multipartFiles.put(fileItem.getName(), fileList) != null) { // CHANGED!!
			throw new MultipartException("Multiple files for field name [" + file.getName()
				+ "] found - not supported by MultipartResolver");
		    }
		    if (logger.isDebugEnabled()) {
			logger.debug("Found multipart file [" + file.getName() + "] of size " + file.getSize() + " bytes with original filename ["
				+ file.getOriginalFilename() + "], stored " + file.getStorageDescription());
		    }
		}

	    }
	}

	return new MultipartParsingResult(multipartFiles, multipartParameters, null);
    }
    
	/**
	 * querystring에서 지정된 파라메터를 추출
	 */
	public static String getQueryParams(String queryParams,String getparam){
		if(StringUtils.isNotBlank(queryParams)) {
			MultiValueMap<String,String> m = UriComponentsBuilder.fromUriString("http://temp.com?"+queryParams).build().getQueryParams();

			if(null != m) return null!=m.get(getparam)?m.get(getparam).get(0):null;
		}
		return null;
	}
}
