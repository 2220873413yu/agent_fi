package com.xms.app.entity.resp;

import lombok.Builder;
import lombok.Data;

/**
 * @author: renengadePISTA
 * @createDate: 2023/9/11
 */
@Data
@Builder
public class OssDoResp {
	/**
     * 文件全路径
	 */
	private  String url;
	/**
     * 文件相对路径
	 */
	private  String fileName;
	/**
     * 新文件名
	 */
	private  String newFileName;
	/**
     * 原始文件名
	 */
	private  String originalFilename;
}
