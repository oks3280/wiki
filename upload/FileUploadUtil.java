import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.util.LimitedInputStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUploadUtil {

	/**
	 * 대용량 첨부파일 업로드
	 *
	 * maxFileSize : 파일별 맥스 사이즈 = -1L 무제한 maxSize : request 컨텐츠 맥스 사이즈 = -1L 무제한
	 */
	public static List<Map<String, Object>> upload(FileItemIterator iter, String saveDirStr, String saveFilePrefix) throws IOException, FileUploadException {
		return upload(iter, saveDirStr, saveFilePrefix,-1L, -1L);
	}
	/**
	 * 대용량 첨부파일 업로드
	 *
	 * maxFileSize : 파일별 맥스 사이즈 = -1L 무제한 maxSize : request 컨텐츠 맥스 사이즈 = -1L 무제한
	 */
	public static List<Map<String, Object>> upload(FileItemIterator iter, String saveDirStr, String saveFilePrefix,
			long maxFileSize, long maxSize) throws IOException, FileUploadException {
		List<Map<String, Object>> uploadList = new ArrayList<Map<String, Object>>();

		File saveDir = new File(EgovWebUtil.filePathBlackList(saveDirStr));
		if (!saveDir.exists() || saveDir.isFile()) {
			saveDir.mkdirs();
		}

		boolean checkMaxSize = false;
		if (maxFileSize == -1L)
			maxFileSize = maxSize;
		if (maxSize != -1L)
			checkMaxSize = true;

		while (iter.hasNext()) {
			final FileItemStream item = iter.next();
			if (item.isFormField()) {
				continue;
			} else if (!StringUtils.isBlank(item.getName())) {
				File destination = null;
				InputStream ins = null;
				InputStream limitIns = null;
				FileOutputStream outs = null;
				Map<String, Object> hm = new HashMap<String, Object>();
				long alowedLimit = 0L;

				try {
					if (maxFileSize != -1L || checkMaxSize) {
						if (checkMaxSize) {
							alowedLimit = maxSize > maxFileSize ? maxFileSize : maxSize;
						} else {
							alowedLimit = maxFileSize;
						}

						long contentLength = getContentLength(item.getHeaders());

						if (contentLength != -1L && contentLength > alowedLimit) {
							throw new FileUploadIOException(new FileSizeLimitExceededException(
									"the field " + item.getFieldName() + " exceeds its maximum permitted size of "
											+ alowedLimit + " characters.(contentLength)",
									contentLength, alowedLimit));
						}

                		ins = item.openStream();

                		limitIns = new LimitedInputStream(ins,alowedLimit) {
							@Override
							protected void raiseError(long pSizeMax, long pCount) throws IOException {
								throw new FileUploadIOException(new FileSizeLimitExceededException(
										"the field " + item.getFieldName() + " exceeds its maximum permitted size of "
												+ pSizeMax + " characters.(fileLength)",
												pSizeMax, pCount));
							}
						};

						destination = new File(saveDir, EgovWebUtil.filePathBlackList(
								saveFilePrefix + UUID.randomUUID().toString().replaceAll("-", "").toUpperCase()));
						outs = new FileOutputStream(destination);

						IOUtils.copy(limitIns, outs);
						maxSize -= destination.length();
					} else {
						ins = item.openStream();
						destination = new File(saveDir, EgovWebUtil.filePathBlackList(
								saveFilePrefix + UUID.randomUUID().toString().replaceAll("-", "").toUpperCase()));
						outs = new FileOutputStream(destination);
						IOUtils.copy(ins, outs);
					}
				} finally {
					IOUtils.close(ins);
					IOUtils.close(limitIns);
					IOUtils.close(outs);
				}

				hm.put("orgFileName", item.getFieldName());
				hm.put("saveFileName", destination.getName());
				hm.put("saveFileFullPathName", destination.getPath());
				hm.put("contentType", MediaType.parseMediaType(item.getContentType()));
				hm.put("fileSize", destination.length());

				uploadList.add(hm);
			}
		}

		return uploadList;
	}

	private static long getContentLength(FileItemHeaders pHeader) {
		try {
			return Long.parseLong(pHeader.getHeader(FileUploadBase.CONTENT_LENGTH));
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * 일반 첨부파일 업로드
	 */
	public static List<Map<String, Object>> upload(Map<String, MultipartFile> files, String saveDirStr,
			String saveFilePrefix) throws Exception {

		File saveDir = new File(EgovWebUtil.filePathBlackList(saveDirStr));

		if (!saveDir.exists() || saveDir.isFile()) {
			if (saveDir.mkdirs()) {
				log.debug("[file.mkdirs] saveFolder : Creation Success ");
			} else {
				log.error("[file.mkdirs] saveFolder : Creation Fail ");
			}
		}

		Iterator<Entry<String, MultipartFile>> itr = files.entrySet().iterator();
		MultipartFile multipartFile;
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		while (itr.hasNext()) {
			Entry<String, MultipartFile> entry = itr.next();
			multipartFile = entry.getValue();

			if ("".equals(multipartFile.getOriginalFilename())) {
				continue;
			}

			File destination = new File(saveDir, EgovWebUtil.filePathBlackList(
					saveFilePrefix + UUID.randomUUID().toString().replaceAll("-", "").toUpperCase()));
			multipartFile.transferTo(destination);

			Map<String, Object> hm = new HashMap<String, Object>();

			hm.put("orgFileName", multipartFile.getOriginalFilename());
			hm.put("saveFileName", destination.getName());
			hm.put("saveFileFullPathName", destination.getPath());
			hm.put("contentType", multipartFile.getContentType());
			hm.put("fileSize", destination.length());
			hm.put("saveDirStr", saveDirStr);

			result.add(hm);
		}

		return result;
	}

}
