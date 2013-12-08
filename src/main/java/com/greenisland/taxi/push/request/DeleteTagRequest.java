package com.greenisland.taxi.push.request;

import com.greenisland.taxi.push.http.HttpMethodName;
import com.greenisland.taxi.push.util.Constants;

/**
 * @author liyazhou@baidu.com
 *
 */
public class DeleteTagRequest  extends BaiduPushRequest{
	/**
	 * 标签名，最长128字节。
	 */
	private String tag;
	
	public DeleteTagRequest(String tag) {
		super.setHttpMethod(HttpMethodName.POST);
		super.setMethodName(Constants.delete_tag);
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}


