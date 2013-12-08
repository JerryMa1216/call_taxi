package com.greenisland.taxi.controller;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.greenisland.taxi.common.constant.ApplicationState;
import com.greenisland.taxi.common.constant.CommentState;
import com.greenisland.taxi.common.constant.GPSCommand;
import com.greenisland.taxi.common.constant.ResponseState;
import com.greenisland.taxi.common.constant.TradeState;
import com.greenisland.taxi.common.utils.TCPUtils;
import com.greenisland.taxi.domain.CallApplyInfo;
import com.greenisland.taxi.domain.CommentInfo;
import com.greenisland.taxi.domain.LocationInfo;
import com.greenisland.taxi.domain.TaxiInfo;
import com.greenisland.taxi.domain.UserInfo;
import com.greenisland.taxi.gateway.gps.SyncClient;
import com.greenisland.taxi.gateway.gps.resolver.MessageHandler;
import com.greenisland.taxi.manager.CallApplyInfoService;
import com.greenisland.taxi.manager.CommentInfoService;
import com.greenisland.taxi.manager.LocationInfoService;
import com.greenisland.taxi.manager.TaxiInfoService;
import com.greenisland.taxi.manager.UserInfoService;

/**
 * 订单操作
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-30下午4:38:58
 */
@Controller
public class CallApplyController {
	private static Logger log = Logger.getLogger(CallApplyController.class.getName());
	@Resource
	private SyncClient syncClient;
	@Resource
	private UserInfoService userInfoService;
	@Resource
	private LocationInfoService locationInfoService;
	@Resource
	private CallApplyInfoService callApplyInfoService;
	@Resource
	private MessageHandler messageHandler;
	@Resource
	private CommentInfoService commentInfoService;
	@Resource
	private TaxiInfoService taxiInfoService;

	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 叫车请求
	 * 
	 * @param phoneNumber
	 * @param callTime
	 * @param callType
	 * @param callScope
	 * @param callDistance
	 * @param mechineType
	 *            设备类型 1，Andriod 2，ios
	 * @param sLoca
	 *            起点位置
	 * @param eLoca
	 *            重点位置
	 * @param longitude
	 * @param latitude
	 * @throws Exception
	 */
	@RequestMapping(value = "/call_taxi", method = RequestMethod.POST)
	public void callTaxi(@RequestParam String phoneNumber, @RequestParam String callTime, @RequestParam String callType,
			@RequestParam String callScope, @RequestParam String callDistance, @RequestParam String mechineType, @RequestParam String sLoca,
			@RequestParam String eLoca, @RequestParam String longitude, @RequestParam String latitude, @RequestParam String name,
			@RequestParam String age, HttpServletResponse response) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		Map<String, Object> mapCall = null;// 调用接口返回值
		// 根据用户手机号获取用户信息
		UserInfo userInfo = userInfoService.getUserInfoByPhoneNumber(phoneNumber);
		// 保存用户叫车位置，方便后台管理系统做数据分析
		LocationInfo location = new LocationInfo();
		location.setCreateDate(new Date());
		location.setGpsLatitude(latitude);
		location.setGpsLongitude(longitude);
		location.setGpsTime(new Date());
		location.setUserId(userInfo.getId());
		locationInfoService.saveLocationInfo(location);
		// 新增打车请求记录
		CallApplyInfo applyInfo = new CallApplyInfo();
		applyInfo.setUserId(userInfo.getId());
		applyInfo.setCallLength(Integer.parseInt(callDistance + "000"));// 叫车距离
		applyInfo.setCallScope(Integer.parseInt(callScope + "000"));// 叫车范围
		applyInfo.setCallTime(format.parse(callTime));// 叫车时间
		applyInfo.setCallType(callType);// 叫车类型 1即时叫车 2预约叫车
		applyInfo.setEndLocation(eLoca);// 目的地
		applyInfo.setStartLocation(sLoca);// 出发地
		applyInfo.setMechineType(mechineType);// 设备类型
		applyInfo.setCreateDate(new Date());
		applyInfo.setState(ApplicationState.VALIDATION);// 请求状态
		applyInfo.setIsGetOn("0");// 是否上车
		applyInfo.setResponseState(ResponseState.WAIT_RESPONSE);// 是否响应
		applyInfo.setTradeState(TradeState.WAIT_FINISH);// 交易状态
		applyInfo.setMonitorCount(0);// 监控次数
		applyInfo.setIsComment("0");// 是否评价
		applyInfo.setDeleteFlag("N");// 未删除
		String applyId = callApplyInfoService.saveCallApplyInfo(applyInfo);

		// 调用GPS平台接口发送打车请求，name为推送中的userId，age为推送中的channelId
		String requestMsg = TCPUtils.getCallApply(applyInfo, applyId + "-" + mechineType + "-" + name + "-" + age, location, userInfo);
		syncClient.sendMessage(requestMsg);
		// 获取GPS平台返回数据
		String responseData = syncClient.getResult();
		System.out.println(responseData);
		mapCall = messageHandler.handler(responseData);
		String returnData = (String) mapCall.get(GPSCommand.GPS_CALL_RESP + "");
		if (!returnData.equals("ER")) {
			// 叫车请求发送成功
			map.put("state", 0);
			map.put("message", "OK");
			map.put("date", new Date());
			map.put("data", returnData.substring(0, returnData.indexOf("-")));
		} else {
			CallApplyInfo apply = callApplyInfoService.getCallApplyInfoById(applyId);
			apply.setDeleteFlag("Y");
			apply.setState(ApplicationState.INVALIDATION);
			callApplyInfoService.updateApplyInfo(apply);
			map.put("state", 1);
			map.put("message", "ER");
			map.put("date", new Date());
			map.put("data", null);
		}

		try {
			response.reset();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/json");
			PrintWriter pw = response.getWriter();
			pw.write(objectMapper.writeValueAsString(map));
			pw.flush();
			pw.close();
		} catch (Exception e) {
			log.error("=================系统异常>>" + e.getMessage());
		}
		// 开启一个倒计时线程，时间到将新增申请置位无效
		new CallApplyThread(callApplyInfoService, applyId);
	}

	/**
	 * 手动取消即时叫车请求
	 * 
	 * @param applyId
	 * @param canncelReason
	 * @param uid
	 * @param response
	 */
	@RequestMapping(value = "/call_cancel", method = RequestMethod.POST)
	public void cancelCall(@RequestParam String applyId, @RequestParam String canncelReason, @RequestParam String uid, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		CallApplyInfo applyInfo = callApplyInfoService.getCallApplyInfoById(applyId);
		if (applyInfo != null) {
			applyInfo.setCanncelReason(canncelReason);
			applyInfo.setDeleteFlag("Y");
			applyInfo.setState(ApplicationState.INVALIDATION);
			callApplyInfoService.updateApplyInfo(applyInfo);
			UserInfo userInfo = userInfoService.getUserInfoById(uid);
			userInfo.setBreakPromissDate(new Date());
			int count = userInfo.getBreakPromiseCount();
			userInfo.setBreakPromiseCount(count++);
			userInfo.setUpdateDate(new Date());
			this.userInfoService.updateUserInfo(userInfo);
			map.put("state", 0);
			map.put("message", "OK");
			map.put("date", new Date());
			map.put("data", null);
		} else {
			map.put("state", 1);
			map.put("message", "ER");
			map.put("date", new Date());
			map.put("data", null);
		}
		try {
			response.reset();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/json");
			PrintWriter pw = response.getWriter();
			pw.write(objectMapper.writeValueAsString(map));
			pw.flush();
			pw.close();
		} catch (Exception e) {
			log.error("系统异常>>" + e.getMessage());
		}
	}

	/**
	 * 历史订单信息查询
	 * 
	 * @param uid
	 * @param response
	 */
	@RequestMapping(value = "/query_orders", method = RequestMethod.POST)
	public void queryHistoryOrder(@RequestParam String uid, HttpServletResponse response) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		List<CallApplyInfo> list = callApplyInfoService.queryApplyInfoByUid(uid);
		for (CallApplyInfo apply : list) {
			TaxiInfo taxi = taxiInfoService.getTaxiInfoById(apply.getTaxiId());
			apply.setTaxiPlateNumber(taxi.getTaxiPlateNumber());
			apply.setDriverName(taxi.getDriverName());
			apply.setDirverPhoneNumber(taxi.getDirverPhoneNumber());
		}
		map.put("state", 0);
		map.put("message", "OK");
		map.put("date", new Date());
		map.put("data", convertMap(list));
		try {
			response.reset();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/json");
			PrintWriter pw = response.getWriter();
			pw.write(objectMapper.writeValueAsString(map));
			pw.flush();
			pw.close();
		} catch (Exception e) {
			log.error("系统异常>>" + e.getMessage());
		}
	}

	/**
	 * 评价司机
	 * 
	 * @param applyId
	 * @param level
	 * @param content
	 * @param type
	 * @param response
	 */
	@RequestMapping(value = "/comment_driver", method = RequestMethod.POST)
	public void commentCall(@RequestParam String applyId, @RequestParam String level, @RequestParam String content, @RequestParam String type,
			HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		CallApplyInfo applyInfo = callApplyInfoService.getCallApplyInfoById(applyId);
		String commentId = null;
		if (applyInfo != null) {
			CommentInfo comment = applyInfo.getCommentInfo();
			if (comment != null) {
				comment.setLevel(Integer.parseInt(level));
				comment.setContent(content);
				comment.setUpdateDate(new Date());
				commentInfoService.updateCommentInfo(comment);
				commentId = comment.getId();
			} else {
				CommentInfo newComment = new CommentInfo();
				newComment.setContent(content);
				newComment.setLevel(Integer.parseInt(level));
				newComment.setCreateDate(new Date());
				commentId = commentInfoService.saveCommentInfo(newComment);
			}
			applyInfo.setIsGetOn(type);
			applyInfo.setIsComment(CommentState.COMMENT);
			applyInfo.setCommentId(commentId);
			callApplyInfoService.updateApplyInfo(applyInfo);
			map.put("state", 0);
			map.put("message", "OK");
			map.put("date", new Date());
			map.put("data", null);
		} else {
			map.put("state", 1);
			map.put("message", "ER");
			map.put("date", new Date());
			map.put("data", null);
		}
		try {
			response.reset();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/json");
			PrintWriter pw = response.getWriter();
			pw.write(objectMapper.writeValueAsString(map));
			pw.flush();
			pw.close();
		} catch (Exception e) {
			log.error("系统异常>>" + e.getMessage());
		}
	}

	private List<Map<String, Object>> convertMap(List<CallApplyInfo> list) {
		List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
		for (CallApplyInfo applyInfo : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", applyInfo.getId());
			map.put("startLocation", applyInfo.getStartLocation());
			map.put("endLocation", applyInfo.getEndLocation());
			map.put("callTime", applyInfo.getCallTime());
			map.put("tradeState", applyInfo.getTradeState());
			map.put("isComment", applyInfo.getIsComment());
			map.put("taxiId", applyInfo.getTaxiId());
			map.put("canncelReason", applyInfo.getCanncelReason());
			map.put("monitorCount", applyInfo.getMonitorCount());
			map.put("taxiPlateNumber", applyInfo.getTaxiPlateNumber());
			map.put("driverName", applyInfo.getDriverName());
			map.put("dirverPhoneNumber", applyInfo.getDirverPhoneNumber());
			returnList.add(map);
		}
		return returnList;
	}
}
