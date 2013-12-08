package com.greenisland.taxi.gateway.push;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.bstek.dorado.core.Configure;
import com.greenisland.taxi.push.DefaultPushClient;
import com.greenisland.taxi.push.auth.PushCredentials;
import com.greenisland.taxi.push.model.DeviceType;
import com.greenisland.taxi.push.model.MessageType;
import com.greenisland.taxi.push.model.PushType;
import com.greenisland.taxi.push.request.PushMessageRequest;
import com.greenisland.taxi.push.response.PushResponse;

@Component
public class PushClient {
	private final static String API_KEY = Configure.getString("apiKey");
	private final static String SECRIT_KEY = Configure.getString("secretKey");

	/**
	 * 推送制定用户信息，ios
	 * 
	 * @param taxiPlateNumber
	 * @param driverName
	 * @param driverNumber
	 * @param company
	 * @param niceCount
	 * @param orderCount
	 * @return
	 */
	public boolean pushSingleUserIOS(String taxiPlateNumber, String driverName, String driverNumber, String company, int niceCount, int orderCount,
			String userId, String channelId) {
		DefaultPushClient client = new DefaultPushClient(new PushCredentials(API_KEY, SECRIT_KEY));
		PushMessageRequest request = new PushMessageRequest();
		StringBuilder message = new StringBuilder("{\"aps\":{\"alert\":");
		message.append("\"159打车：您的订单已被抢到\",\"sound\":\"\",\"badge\":\"\"},");
		message.append("\"taxiPlateNumber\":\"" + taxiPlateNumber + "\",");
		message.append("\"driverName\":\"" + driverName + "\",");
		message.append("\"driverNumber\":\"" + driverNumber + "\",");
		message.append("\"company\":\"" + company + "\",");
		message.append("\"niceCount\":\"" + niceCount + "\",");
		message.append("\"orderCount\":\"" + niceCount + "\"");
		message.append("}");
		request.setUserId(userId);
		request.setChannelId(channelId);
		request.setMessageType(MessageType.notify);
		request.setMessages(message.toString());
		request.setPushType(PushType.single_user);
		request.setDeviceTypes(Arrays.asList(DeviceType.iso));
		request.setMessageKeys(UUID.randomUUID().toString());
		request.setDeployStatus(Long.valueOf(1));// test need
		PushResponse<Integer> response = client.pushMessage(request);
		response.getResult();
		return true;
	}

	/**
	 * 推送制定用户信息，android
	 * 
	 * @param taxiPlateNumber
	 * @param driverName
	 * @param driverNumber
	 * @param company
	 * @param niceCount
	 * @param orderCount
	 * @param userId
	 * @param channelId
	 * @return
	 */
	public boolean pushSinglerUserAndroid(String taxiPlateNumber, String driverName, String driverNumber, String company, int niceCount,
			int orderCount, String userId, String channelId) {
		DefaultPushClient client = new DefaultPushClient(new PushCredentials(API_KEY, SECRIT_KEY));
		PushMessageRequest request = new PushMessageRequest();
		request.setMessageType(MessageType.message);
		StringBuilder message = new StringBuilder("{\"title\":\"%s\",\"description\":\"%s\",\"custom_content\":{");
		message.append("\"taxiPlateNumber\":\"" + taxiPlateNumber + "\",");
		message.append("\"driverName\":\"" + driverName + "\",");
		message.append("\"driverNumber\":\"" + driverNumber + "\",");
		message.append("\"company\":\"" + company + "\",");
		message.append("\"niceCount\":\"" + niceCount + "\",");
		message.append("\"orderCount\":\"" + orderCount + "\"}");
		message.append("}");
		request.setUserId(userId);
		request.setChannelId(channelId);
		request.setMessageType(MessageType.message);
		request.setMessages(message.toString());
		request.setPushType(PushType.single_user);
		request.setDeviceTypes(Arrays.asList(DeviceType.android));
		request.setMessageKeys(UUID.randomUUID().toString());
		request.setDeployStatus(Long.valueOf(1));// test need
		PushResponse<Integer> response = client.pushMessage(request);
		response.getResult();
		return true;
	}
}
