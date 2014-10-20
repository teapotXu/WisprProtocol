package net.iwifi.wispr.protocol;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;


/**
 * This class defines Request infos
 * @author abc
 *
 */
public class RequestUtils {
	public enum RequestMethod{
		GET,
		POST,
		DELETE,
		PUT
	}
	
	/**
	 * 认证请求，下线请求过程中Response各个阶段状态
	 * @author abc
	 *
	 */
	public enum ResponseResultState{
		RSP_NONE,
		RSP_GET_BRAS_SUCCESS,
		RSP_GET_BRAS_FAILED,
		RSP_GET_AUTH_SUCCESS,
		RSP_GET_AUTH_FAILED,
		RSP_POST_AUTH_SUCCESS,
		RSP_POST_AUTH_FAILED,
		RSP_GET_LOGOFF_SUCCESS,
		RSP_GET_LOGOFF_FAILED
	}
	
	/**
	 * 认证请求,下线请求过程中返回的数据
	 * @author abc
	 *
	 */
	public class ResponseResultInfos{
		//HTTP 请求的status code
		int http_request_status_code;
		//认证请求过程的状态
		ResponseResultState process_result;
		//认证成功时为logoff_url,失败时包含失败信息
		String request_url;
		//成功时为 AuthenticationReply，下线成功为LogoffReply，失败时为NULL
		Object result_body;
	}
	
	
	RequestMethod requestMethod;
	
	/**
	 * HTTP请求数据的类型，包括表单，string，byte等
	 */
	public HttpEntity httpEntity;
	public Map<String,String> headers;
	public final String ENCODING = "UTF-8";
	public String url;

	public RequestUtils(String url, RequestMethod method){
		this.url = url;
		this.requestMethod = method;
	}	
	
	public void setEntity(ArrayList<NameValuePair> forms){
		try {
			httpEntity = new UrlEncodedFormEntity(forms, ENCODING);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setEntity(String postContent){
		try {
			httpEntity = new StringEntity(postContent, ENCODING);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setEntity(byte[] bytes){
		httpEntity = new ByteArrayEntity(bytes);
	}	
	
	public ResponseResultInfos getResultInfos(){
		return new ResponseResultInfos();
	}
}
