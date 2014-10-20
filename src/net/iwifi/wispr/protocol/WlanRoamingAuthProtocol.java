package net.iwifi.wispr.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.iwifi.wispr.protocol.RequestUtils.ResponseResultInfos;
import net.iwifi.wispr.protocol.RequestUtils.ResponseResultState;
import net.iwifi.wispr.protocol.RequestUtils.RequestMethod;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.os.Handler;
import android.os.Message;


/**
 * Define Interfaces 
 * @author abc
 *
 */
public class WlanRoamingAuthProtocol {
	
	/**
	 *请求返回给用户的内容
	 * @author abc
	 *
	 */
	public class ResultInfos{
		//认证时输入的用户名
		public String userName;
		//认证请求成功后返回的logoffUrl
		public String logoffUrl;
		//请求失败后错误信息
		public String failInfo;
		//保留
		public Object reserve;
	}
	
	
	//request action type
	public final static int REQUEST_ACTION_AUTHENTICATION  = 0;
	public final static int REQUEST_ACTION_LOGOFF          = 1;
	
	//the result status of the response
	public final static int RESPONSE_RESULT_SUCCESS        = 0;
	public final static int RESPONSE_RESULT_FAILED         = 1;
	
	public final static String HTTP_HEADER_USE_AGENT       = "User-Agent";
	public final static String HTTP_HEADER_CONTENT_TYPE    = "Content-type";
	
	public String mLogoffUrl = "";
	private final static int HTTP_CONNET_TIMEOUT = 20000;
	private final static String DEFAULT_USER_AGENT = "CDMA+WLAN";
	private final static String URL = "http://www.baidu.com";
	private final static String BTN_IDENTIFY = "Login";
	private final static String FNAME = "0";
	private final static String ORIGINAL_SERVER = "http://www.baidu.com";
	private IWlanAuthenRequestCallback callback;
	
	/**
	 * Authentication steps  
	 */
	private final static int STEP_NONE                     = 0;
	private final static int STEP_GET_BRAS_ADR             = 1;
	private final static int STEP_GET_AUTHENTICATE_ADR     = 2;
	private final static int STEP_POST_AUTHENTICATE_REQ    = 3;
	private final static int STEP_GET_LOGOFF_REQ           = 100;
	//记录请求的过程的index
	private int auth_step_index = STEP_NONE;
	
	/**
	 * Messge types
	 */
	private final static int MSG_AUTHENTICATION_SUCC = 0;
	private final static int MSG_AUTHENTICATION_FAIL = 1;
	private final static int MSG_LOGOFF_SUCC         = 2;
	private final static int MSG_LOGOFF_FAIL         = 3;
	
	
	public void doAunthenticaterRequest(String username,String password,IWlanAuthenRequestCallback callback ){
		this.doAunthenticaterRequest(URL, username, password,BTN_IDENTIFY,FNAME,ORIGINAL_SERVER,callback);
	}

	/**
	 * 一键认证接口，返回CallBack
	 * @param request_url
	 * @param username
	 * @param password
	 * @param btn_identify
	 * @param fname
	 * @param org_server
	 * @param callback
	 * 
	 * Discroption: 
	 * 
	 * 需要四个步骤完成认证的过程
	 * step1：根据url先直接请求，将得到HTML的跳转页面，
	 * stop2：解析HTML得到需要跳转的url地址，再去GET请求，User-Agent = CDMA+WLAN
	 * step3: 请求成功后，响应报文中含有login 的url，向portal发起post请求，携带UserName，password等参数
	 * step4: 解析响应报文，成功后即表示认证成功
	 * 
	 */
	public void doAunthenticaterRequest(final String request_url,
			final String username,
			final String password,
			final String btn_identify,
			final String fname,
			final String org_server,
			IWlanAuthenRequestCallback callback){
		
		this.callback = callback;
		new Thread(){
			@Override
			public void run() {
				super.run();
				ResultInfos resInfos = new ResultInfos();
				resInfos.userName = username;
				try {
					//step1:根据url直接请求，得到HTML中转页面
					auth_step_index = STEP_GET_BRAS_ADR;
					UrlEncodedFormEntity entity = generateHttpEntity(username,password,btn_identify,fname,org_server);
					
					ResponseResultInfos request_result = HandleHttpRequest(request_url, entity, RequestMethod.GET);
					
					auth_step_index = STEP_NONE; // 
					sendMessageWithResult(REQUEST_ACTION_AUTHENTICATION,resInfos,request_result,null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					auth_step_index = STEP_NONE; 
					
					sendMessageWithResult(REQUEST_ACTION_AUTHENTICATION,resInfos,null,e);
					
				}
			}
		}.start();
	}
	
	/**
	 * Logoff请求
	 * 
	 * @param logoff_url
	 * @param callback
	 * Discroption:    直接向portal发起get logoff url的下线请求
	 */
	public void doLogOffRequest(final String logoff_url,final String username,IWlanAuthenRequestCallback callback){
		this.callback = callback;
		new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				ResultInfos resInfos = new ResultInfos();
				resInfos.userName = username;
				try {
					auth_step_index = STEP_GET_LOGOFF_REQ;
					ResponseResultInfos request_result = HandleHttpRequest(logoff_url, null, RequestMethod.GET);
					auth_step_index = STEP_NONE; 
					sendMessageWithResult(REQUEST_ACTION_LOGOFF,resInfos,request_result,null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					auth_step_index = STEP_NONE; 
					sendMessageWithResult(REQUEST_ACTION_LOGOFF,resInfos, null, e);
				}
			}
		}.start();		
	}
	
	Handler mhandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			ResultInfos result = (ResultInfos)msg.obj;			
			switch(msg.what)
			{
			case MSG_AUTHENTICATION_SUCC:
				callback.onAuthenticationSuccuess(result);
				break;
			case MSG_AUTHENTICATION_FAIL:
				callback.onAuthenticationFailed(result);
				break;
			case MSG_LOGOFF_SUCC:
				callback.onLogoffSuccess(result);
				break;
			case MSG_LOGOFF_FAIL:
				callback.onLogoffFailed(result);
				break;
			default:
				break;
			}
		}
    };
    
	/**
	 * 
	 * @param action
	 * @param sendInfos
	 * @param request_result
	 * @param ex
	 */
    private void sendMessageWithResult(int action,ResultInfos sendInfos,ResponseResultInfos request_result, Exception ex){
    	Message msg = mhandler.obtainMessage();
		
		if(ex != null){
			//发送Exception时
			switch(action){
			case REQUEST_ACTION_AUTHENTICATION:
				msg.what = MSG_AUTHENTICATION_FAIL;
				break;
			case REQUEST_ACTION_LOGOFF:
				msg.what = MSG_LOGOFF_FAIL;
				break;
			}
			sendInfos.failInfo = ex.toString();
		}
		else if(request_result != null)
		{
			if(request_result.process_result == ResponseResultState.RSP_POST_AUTH_SUCCESS){
				if(request_result.result_body instanceof AuthenticationReply)
				{
					AuthenticationReply auten_reply = (AuthenticationReply)request_result.result_body;
					if(50 == auten_reply.responseCode){
						msg.what = MSG_AUTHENTICATION_SUCC;
						sendInfos.logoffUrl = request_result.request_url;
						sendInfos.failInfo = null;
					}
					else{
						msg.what = MSG_AUTHENTICATION_FAIL;
						sendInfos.logoffUrl = null;
						sendInfos.failInfo = "Authenticate connection is ok, but response failed : " + auten_reply.responseCode + " message: " + auten_reply.reply_message;
					}
				}
				else{
					msg.what = MSG_AUTHENTICATION_FAIL;
					sendInfos.logoffUrl = null;
					sendInfos.failInfo = "Authenticate connection is ok, but request body whenever gone ";
					
				}
			}
			else if(request_result.process_result == ResponseResultState.RSP_GET_LOGOFF_SUCCESS){
				if(request_result.result_body instanceof LogoffReply)
				{
					LogoffReply auten_reply = (LogoffReply)request_result.result_body;
					if(150 == auten_reply.responseCode){
						msg.what = MSG_LOGOFF_SUCC;
						sendInfos.logoffUrl = null;
						sendInfos.failInfo = null;
					}
					else{
						msg.what = MSG_LOGOFF_FAIL;
						sendInfos.logoffUrl = null;
						sendInfos.failInfo = "Logoff connection is ok, but response tells it failed : " + auten_reply.responseCode;
					}
				}
				else{
					msg.what = MSG_LOGOFF_FAIL;
					sendInfos.logoffUrl = null;
					sendInfos.failInfo = "Logoff connection is ok, but request body whenever gone ";
				}
			}
			else{
				if(action == REQUEST_ACTION_AUTHENTICATION)
					msg.what = MSG_AUTHENTICATION_FAIL;
				else
					msg.what = MSG_LOGOFF_FAIL;
				sendInfos.logoffUrl = null;
				sendInfos.failInfo = "Authenticate/Lofgoff connection failed: " + request_result.process_result + " more: " + request_result.request_url;
			}
		}
		
		msg.obj = sendInfos;
		mhandler.sendMessage(msg);
    }
	
	/**
	 * Get Requst Operation
	 * @throws Exception 
	 */
	private HttpResponse httpGetRequest(RequestUtils request){
		HttpResponse response = null;
		HttpGet mHttpGet = new HttpGet(request.url);
		addHeader(mHttpGet, request.headers);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		//httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, HTTP_CONNET_TIMEOUT);
		
		try{
			response = httpClient.execute(mHttpGet);
		}catch(Exception e){
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * Post Requst Operation
	 * 
	 * @param request
	 * @return
	 */
	private HttpResponse httpPostRequest(RequestUtils request){
		HttpResponse response = null;
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(request.url);
		addHeader(post,request.headers);
		
		if(request.httpEntity == null){
			throw new IllegalStateException("you forget to set post content to the httpost");
		}
		else
		{
			post.setEntity(request.httpEntity);
		}
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;		
  }	


	/**
	 * 处理所有的HTTP请求，并解析请求结果
	 * @param request_url
	 * @param httpEntity
	 * @param requestMethod
	 * @return
	 * @throws Exception
	 */
    public ResponseResultInfos HandleHttpRequest(String request_url,HttpEntity httpEntity, RequestMethod requestMethod) throws Exception{
    	ResponseResultInfos rsp_info = null;
    	HttpResponse response = null;
		RequestUtils request = new RequestUtils(request_url, requestMethod);
		Map<String,String> headers = new HashMap<String, String>();
		headers.put(HTTP_HEADER_USE_AGENT, DEFAULT_USER_AGENT);
		request.headers = headers;
		if(httpEntity != null)
			request.httpEntity = httpEntity;
		
    	switch(requestMethod){
    	case GET:
    		response = httpGetRequest(request);
    		break;
    	case POST:
    		response = httpPostRequest(request);
    		break;
    	case DELETE:
    	case PUT:
    	default:
    		throw new IllegalStateException("you doesn't define this requestmethod : " + request.requestMethod);
    	}
    	
    	if(response != null){
    		try {
    			rsp_info = HandleParseHttpResponse(request.getResultInfos(),response);
    			//解析返回结果
    			switch(rsp_info.process_result){
				case RSP_GET_BRAS_SUCCESS:
					//Step2：请求获取认证地址
					auth_step_index = STEP_GET_AUTHENTICATE_ADR;
					String url = rsp_info.request_url;
					rsp_info = HandleHttpRequest(url,httpEntity,RequestMethod.GET);
					break;
				case RSP_GET_AUTH_SUCCESS:
					//step3: 获取成功后，开始认证请求
					auth_step_index = STEP_POST_AUTHENTICATE_REQ;
					String post_url = rsp_info.request_url;
					rsp_info = HandleHttpRequest(post_url,httpEntity,RequestMethod.POST);
					break;
				case RSP_POST_AUTH_SUCCESS:
					//请求认证成功
					break;
				case RSP_GET_LOGOFF_SUCCESS:
					//下线请求成功
					break;
				case RSP_GET_BRAS_FAILED:
				case RSP_GET_AUTH_FAILED:
				case RSP_POST_AUTH_FAILED:
				case RSP_GET_LOGOFF_FAILED:
				default:
					break;
				}    			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new Exception(e);
			}
    		//return rsp_info;
    	}else{
    		throw new IllegalStateException("HandleHttpRequest " + requestMethod + " no response ");
    	}
		return rsp_info;
    }
    
    
	/**
	 * 保存请求头信息
	 */
	private void addHeader(HttpUriRequest request, Map<String,String>headers){
		if(headers != null && headers.size() > 0){
			for(Entry<String,String>entry:headers.entrySet()){
				request.addHeader(entry.getKey(), entry.getValue());
			}
		}
	}
	
	private UrlEncodedFormEntity generateHttpEntity(String userName,
			String password,
			String btn_identify,
			String fname,
			String org_server){
		HashMap<String, String>postData = new HashMap<String, String>();
		postData.put("UserName", userName);
		postData.put("Password", password );
		postData.put("button", btn_identify );
		postData.put("FNAME", fname);
		postData.put("OriginatingServer", org_server);
		
		ArrayList<BasicNameValuePair>  pairList = new ArrayList<BasicNameValuePair>();
		for(Map.Entry<String, String> m : postData.entrySet()){
			pairList.add(new BasicNameValuePair(m.getKey(),m.getValue()));
		}
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
			return entity;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 统一处理Http请求返回的Response,根据wispr协议给出相应的反馈
	 * 
	 * @param resultInfos
	 * @param http_response
	 * @return
	 * @throws Exception
	 */
	private ResponseResultInfos HandleParseHttpResponse(ResponseResultInfos resultInfos, HttpResponse http_response)
			throws Exception
	{
		ResponseResultInfos  result = resultInfos;
		if(http_response == null)
			throw new Exception("HandleHttpResponse input http_response is null ");
		
		int status_code = http_response.getStatusLine().getStatusCode();
		result.http_request_status_code = status_code;
		
		if(status_code == 100 || status_code == 200)
		{
			String content_type = "";
			Header[] headers = http_response.getAllHeaders();

			//获取response的content-type
			for(Header head:headers){
				if(HTTP_HEADER_CONTENT_TYPE.equalsIgnoreCase(head.getName())){
					content_type = head.getValue();
					break;
				}
			}
			//解析HTML
			if(content_type.startsWith("text/html")){
				HttpEntity httpEntity = http_response.getEntity();
				InputStream inputStream = httpEntity.getContent();
				String parse_url = JSoupHTMLParse(inputStream);
				if(parse_url != null)
				{
					//Bras地址 获取成功
					result.process_result = ResponseResultState.RSP_GET_BRAS_SUCCESS;
					result.request_url = parse_url;
				}else{
					//Bras 请求失败
					result.process_result = ResponseResultState.RSP_GET_BRAS_FAILED;
					result.request_url = "Get Bras address failed because the reponse HTML file unrecognized";
				}
			}
			//解析XML
			else //if(content_type.startsWith("text/xml"))
			{
				HttpEntity httpEntity = http_response.getEntity();
				InputStream inputStream;
				XMLParseDefaultHandler xmlDefaultHandler = null;
				try {
					inputStream = httpEntity.getContent();
					xmlDefaultHandler = SAXParseXML(inputStream);
					inputStream.close();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					//	e.printStackTrace();
					throw new Exception("HandleHttpResponse xmlparse IllegalStateException exception : " + e);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					throw new Exception("HandleHttpResponse xmlparse IOException exception : " + e);
				}catch(Exception e){
					throw new Exception("HandleHttpResponse xmlparse other exception : " + e);
				}
				
				//xml解析完成，统计解析结果
				if(xmlDefaultHandler == null)
					throw new Exception("HandlerHttpResponse xmlparese SAXParseXML failed ");
				
				if(auth_step_index == STEP_GET_AUTHENTICATE_ADR && xmlDefaultHandler.mRedirect != null){
					//获取认证地址请求成功，具体是否获取到地址，需要解析mRedirect
					result.request_url = xmlDefaultHandler.mRedirect.login_url;
					result.process_result = ResponseResultState.RSP_GET_AUTH_SUCCESS;
					result.result_body = xmlDefaultHandler.mRedirect;
				}else if(auth_step_index == STEP_POST_AUTHENTICATE_REQ && xmlDefaultHandler.mAuthenticationReply != null){
					//认证请求成功，具体是否是认证成功，也需要通过解析mAuthenticationReply
					mLogoffUrl = result.request_url = xmlDefaultHandler.mAuthenticationReply.logoff_url;
					result.process_result = ResponseResultState.RSP_POST_AUTH_SUCCESS;
					result.result_body = xmlDefaultHandler.mAuthenticationReply;
				}else if(auth_step_index == STEP_GET_LOGOFF_REQ && xmlDefaultHandler.mLogoffReply != null){
					result.process_result = ResponseResultState.RSP_GET_LOGOFF_SUCCESS;
					result.result_body = xmlDefaultHandler.mLogoffReply;
					result.request_url = null;
				}
				else{
					result.result_body = null;
					if(auth_step_index == STEP_GET_AUTHENTICATE_ADR){
						result.request_url = "Get Authenticate address failed because fails to parse the response xml file";
						result.process_result = ResponseResultState.RSP_GET_AUTH_FAILED;
					}else if(auth_step_index == STEP_POST_AUTHENTICATE_REQ){
						result.request_url = "Post authenticate request failed because fails to parse the response xml file";
						result.process_result = ResponseResultState.RSP_POST_AUTH_FAILED;
					}else if(auth_step_index == STEP_GET_LOGOFF_REQ){
						result.request_url = "Logoff Request failed because fails to parse the response xml file";
						result.process_result = ResponseResultState.RSP_GET_LOGOFF_FAILED;
					}
				}
				
			}
//			//其他不识别类型
//			else{
//				//donot recognize this content-type, throw excptions
//				throw new Exception("HandleHttpResponse xmlparse UnkownException: donot recognize the response content-type :" + content_type);
//			}
		}
		return result;
	}
	
	
	/**
	 * 解析HTML数据流
	 * @throws IOException 
	 */
	public String JSoupHTMLParse(InputStream html_stream) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(html_stream));
		String response_html = "";
		String line = "";
		
		while(null != (line = reader.readLine()))
		{
			response_html += line;
		}
				
		Document doc = Jsoup.parse(response_html);
		String html_title = doc.title();
		 Element link = doc.getElementsByTag("a").first();
		 String linkHref = link.attr("href"); 
		 String linkText = link.text(); 
		 
		 if(html_title != null && html_title.equals("Redirect")){
			 html_stream.close();
			 reader.close();
			 return linkHref;
		 }
		 html_stream.close();
		 reader.close();
		 return null;
	}
	
	/**
	 * 解析XML数据流
	 * @param xml_stream: xml stream from Http response
	 */
	public static XMLParseDefaultHandler SAXParseXML(InputStream xml_stream){
    	try{
    		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    		SAXParser saxParser = saxFactory.newSAXParser();
    		XMLReader reader = saxParser.getXMLReader();
    		
    		XMLParseDefaultHandler xmlDefaultHandler = new XMLParseDefaultHandler();
    		reader.setContentHandler(xmlDefaultHandler);
    		
    		reader.parse(new InputSource(xml_stream));
    		xml_stream.close();
    		return xmlDefaultHandler;
    	}catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	return null;
    }
}
