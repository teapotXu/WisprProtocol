package net.iwifi.wispr.protocol;

import net.iwifi.wispr.protocol.WlanRoamingAuthProtocol.ResultInfos;


public interface IWlanAuthenRequestCallback {
//	void onHttpWisprRequestCallback();
	void onAuthenticationSuccuess(ResultInfos result);
	void onAuthenticationFailed(ResultInfos result);
	void onLogoffSuccess(ResultInfos result);
	void onLogoffFailed(ResultInfos result);
}
