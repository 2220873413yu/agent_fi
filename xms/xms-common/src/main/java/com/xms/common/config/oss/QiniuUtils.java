package com.xms.common.config.oss;

import com.alibaba.fastjson.JSON;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.xms.common.utils.Kv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author MIER
 */
@Component
public class QiniuUtils {


	/**
	 * 在配置文件中配置存储空间的账户
	 */
	@Value("${OSS.accessKey}")
	private String accessKey;
	/**
	 * 在配置文件中配置存储空间的秘钥
	 */
	@Value("${OSS.secretKey}")
	private String secretKey;
	@Value("${OSS.bucketName}")
	private String bucketName;
	@Value("${OSS.path}")
	private String path;

	/**
	 * 是否启用
	 */
	@Value("${OSS.enabled}")
	private Boolean enabled;

	public String upload(MultipartFile file, String fileName) {
		if (!enabled) {
			return "1";
		}
		//构造一个带指定 Region 对象的配置类
		//这里是你创建存储空间时候选择的地区
		Configuration cfg = new Configuration(Region.huanan());
		//...其他参数参考类注释
		UploadManager uploadManager = new UploadManager(cfg);
		//...生成上传凭证，然后准备上传
		//默认不指定key的情况下，以文件内容的hash值作为文件名
		try {
			byte[] uploadBytes = file.getBytes();
			Auth auth = Auth.create(accessKey, secretKey);
			String upToken = auth.uploadToken(bucketName);
			Response response = uploadManager.put(uploadBytes, fileName, upToken);
			//解析上传成功的结果
			DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
			return this.getUrl(fileName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public String getUrl(String fileName) {
		return path + "/" + fileName;
	}

	public String getPath() {
		return path;
	}

	public Boolean getEnabled() {
		return enabled;
	}


	/**
	 * 获取token
	 *
	 * @return
	 */
	public Kv getQiNiuToken() {
		Auth auth = Auth.create(accessKey, secretKey);
		String token = auth.uploadToken(bucketName);
		return Kv.create().set("token", token).set("domain", path);
	}
}

