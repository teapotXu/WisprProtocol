package net.iwifi.wispr.protocol;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ClipData.Item;

public class XMLParseDefaultHandler extends DefaultHandler {
	
	public int http_response_type = 0;
	public boolean b_isRedirect = false;
	public boolean b_isAuthenticationReply = false;
	public boolean b_isLogoffReply = false;
	public Redirect mRedirect = null;
	public LogoffReply mLogoffReply = null;
	public AuthenticationReply mAuthenticationReply = null;
	private int cur_parse_item = 0;
	final private int ITEM_REDIRECT_LOCAL_INDENTY = 1;
	final private int ITEM_REDIRECT_LOCAL_NAME = 2;
	final private int ITEM_REDIRECT_LOGIN_URL = 3;
	final private int ITEM_REDIRECT_ABORT_LOGIN_URL = 4;
	final private int ITEM_REDIRECT_MESSAGE_TYPE = 5;
	final private int ITEM_REDIRECT_ACESS_PROCEDURE = 6;
	final private int ITEM_REDIRECT_RESPONSE = 7;
	
	final private int ITEM_LOGOFF_MESSAGE_TYPE = 8;
	final private int ITEM_LOGOFF_RESPONSE_CODE = 9;
	
	final private int ITEM_AUTHENTI_REPLY_MSG = 10;
	final private int ITEM_AUTHENTI_LOGIN_RES_URL = 11;
	final private int ITEM_AUTHENTI_LOGOFF_URL = 12;
	final private int ITEM_AUTHENTI_MSG_TYPE  = 13;
	final private int ITEM_AUTHENTI_RSP_CODE = 14;
	final private int ITEM_AUTHENTI_KEEP_ALIVE = 15;

	public XMLParseDefaultHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		if(localName.equals("Redirect")){
			http_response_type = 0;
			b_isRedirect = false;
		}
		else if(localName.equals("AuthenticationReply")){
			http_response_type = 0;
			b_isAuthenticationReply = false;
		}
		else if(localName.equals("LogoffReply")){
			http_response_type = 0;
			b_isLogoffReply = false;
		}
		else if(localName.equals("AccessProcedure")){
			cur_parse_item = 0;
		}
		else if(localName.equals("AccessLocation")){
			cur_parse_item = 0;
		}
		else if(localName.equals("LocationName")){
			cur_parse_item = 0;			
		}
		else if(localName.equals("LoginURL")){
			cur_parse_item = 0;
		}
		else if(localName.equals("AbortLoginURL")){
			cur_parse_item = 0;
		}
		else if(localName.equals("MessageType")){
			cur_parse_item = 0;
		}
		else if(localName.equals("ResponseCode")){
			cur_parse_item = 0;
		}
		else if(localName.equals("MessageType")){
			cur_parse_item = 0;
		}
		else if(localName.equals("ResponseCode")){
			cur_parse_item = 0;
		}
		else if(localName.equals("MessageType")){
			cur_parse_item = 0;
		}
		else if(localName.equals("ResponseCode")){
			cur_parse_item = 0;
		}
		else if(localName.equals("ReplyMessage")){
			cur_parse_item = 0;
		}
		else if(localName.equals("LoginResultsURL")){
			cur_parse_item = 0;
		}
		else if(localName.equals("LogoffURL")){
			cur_parse_item = 0;
		}
		else if(localName.equals("KeepAlive")){
			cur_parse_item = 0;
		}		
		super.endElement(uri, localName, qName);
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		super.startElement(uri, localName, qName, attributes);
		
		if(localName.equals("Redirect")){
			http_response_type = 1;
			b_isRedirect = true;
			mRedirect = new Redirect();
		}
		else if(localName.equals("AuthenticationReply")){
			mAuthenticationReply = new AuthenticationReply();
			b_isAuthenticationReply = true;
			http_response_type = 2;
		}
		else if(localName.equals("LogoffReply")){
			http_response_type = 3;
			b_isLogoffReply = true;
			mLogoffReply = new LogoffReply();
		}
		else if(localName.equals("AccessProcedure")){
			if(http_response_type == 1 && mRedirect != null)
			{
				cur_parse_item = ITEM_REDIRECT_ACESS_PROCEDURE;
			}
		}
		else if(localName.equals("AccessLocation")){
			if(http_response_type == 1 && mRedirect != null)
			{
				cur_parse_item = ITEM_REDIRECT_LOCAL_INDENTY;
			}
		}
		else if(localName.equals("LocationName")){
			if(http_response_type == 1 && mRedirect != null)
			{
				cur_parse_item = ITEM_REDIRECT_LOCAL_NAME;
			}
			
		}
		else if(localName.equals("LoginURL")){
			if(http_response_type == 1 && mRedirect != null)
			{
				cur_parse_item = ITEM_REDIRECT_LOGIN_URL;
			}
		}
		else if(localName.equals("AbortLoginURL")){
			if(http_response_type == 1 && mRedirect != null)
			{
				cur_parse_item = ITEM_REDIRECT_ABORT_LOGIN_URL;
			}
		}
		else if(localName.equals("MessageType")){
			if(http_response_type == 1 && mRedirect != null)
			{
				cur_parse_item = ITEM_REDIRECT_MESSAGE_TYPE;
			}
			if(http_response_type == 2 && mAuthenticationReply != null)
			{
				cur_parse_item = ITEM_AUTHENTI_MSG_TYPE;
			}
			if(http_response_type == 3 && mLogoffReply != null)
			{
				cur_parse_item = ITEM_LOGOFF_MESSAGE_TYPE;
			}
		}
		else if(localName.equals("ResponseCode")){
			if(http_response_type == 1 && mRedirect != null)
			{
				cur_parse_item = ITEM_REDIRECT_RESPONSE;
			}
			if(http_response_type == 2 && mAuthenticationReply != null)
			{
				cur_parse_item = ITEM_AUTHENTI_RSP_CODE;
			}
			if(http_response_type == 3 && mLogoffReply != null)
			{
				cur_parse_item = ITEM_LOGOFF_RESPONSE_CODE;
			}
		}
		else if(localName.equals("ReplyMessage")){
			if(http_response_type == 2 && mAuthenticationReply != null)
			{
				cur_parse_item = ITEM_AUTHENTI_REPLY_MSG;
			}
		}
		else if(localName.equals("LoginResultsURL")){
			if(http_response_type == 2 && mAuthenticationReply != null)
			{
				cur_parse_item = ITEM_AUTHENTI_LOGIN_RES_URL;
			}
		}
		else if(localName.equals("LogoffURL")){
			if(http_response_type == 2 && mAuthenticationReply != null)
			{
				cur_parse_item = ITEM_AUTHENTI_LOGOFF_URL;
			}
		}
		else if(localName.equals("KeepAlive")){
			if(http_response_type == 2 && mAuthenticationReply != null)
			{
				cur_parse_item = ITEM_AUTHENTI_KEEP_ALIVE;
			}
		}
		
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		super.characters(ch, start, length);
		String targetStr = new String(ch,start,length);
		if(b_isRedirect == true && mRedirect != null)
		{
			switch(cur_parse_item)
			{
			case ITEM_REDIRECT_LOCAL_INDENTY:
				mRedirect.location_indentifier = targetStr;
				break;
			case ITEM_REDIRECT_LOCAL_NAME:
				mRedirect.location_name = targetStr;
				break;
			case ITEM_REDIRECT_LOGIN_URL:
				mRedirect.login_url = targetStr;
				break;
			case ITEM_REDIRECT_ABORT_LOGIN_URL:
				mRedirect.abort_login_url = targetStr;
				break;
			case ITEM_REDIRECT_MESSAGE_TYPE:
				mRedirect.messageType = Integer.parseInt(targetStr);
				break;
			case ITEM_REDIRECT_ACESS_PROCEDURE:
				mRedirect.accessProcedure = Float.parseFloat(targetStr);
				break;
			case ITEM_REDIRECT_RESPONSE:
				mRedirect.response = Integer.parseInt(targetStr);
				break;
			default:
				break;
			}
		}
		else if(b_isAuthenticationReply == true && mAuthenticationReply != null)
		{
			switch(cur_parse_item)
			{
			case ITEM_AUTHENTI_REPLY_MSG:
				mAuthenticationReply.reply_message = targetStr;
				break;
			case ITEM_AUTHENTI_LOGIN_RES_URL:
				mAuthenticationReply.loginResult_url = targetStr;
				break;
			case ITEM_AUTHENTI_LOGOFF_URL:
				mAuthenticationReply.logoff_url = targetStr;
				break;
			case ITEM_AUTHENTI_MSG_TYPE  :
				mAuthenticationReply.messageType = Integer.parseInt(targetStr);
				break;
			case ITEM_AUTHENTI_RSP_CODE:
				mAuthenticationReply.responseCode = Integer.parseInt(targetStr);
				break;
			case ITEM_AUTHENTI_KEEP_ALIVE:
				mAuthenticationReply.keepAlive = Integer.parseInt(targetStr);
				break;
			default:
				break;
			}
		}
		else if(b_isLogoffReply == true && mLogoffReply != null)
		{
			switch(cur_parse_item)
			{
			case ITEM_LOGOFF_MESSAGE_TYPE:
				mLogoffReply.messageType = Integer.parseInt(targetStr);
				break;
			case ITEM_LOGOFF_RESPONSE_CODE:
				mLogoffReply.responseCode = Integer.parseInt(targetStr);
				break;
			default:
				break;
			}
		}

	}

}


class Redirect
{
	String location_indentifier;
	String location_name;
	String login_url;
	String abort_login_url;
	int messageType;
	float accessProcedure;
	int response;
}

class LogoffReply
{
	int messageType;
	int responseCode;
}

class AuthenticationReply
{
	String reply_message;
	String loginResult_url;
	String logoff_url;
	int messageType;
	int responseCode;
	int keepAlive;
}