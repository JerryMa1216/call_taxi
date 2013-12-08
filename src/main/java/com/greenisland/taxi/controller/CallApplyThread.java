package com.greenisland.taxi.controller;

import org.apache.log4j.Logger;

import com.greenisland.taxi.common.constant.ApplicationState;
import com.greenisland.taxi.common.constant.ResponseState;
import com.greenisland.taxi.domain.CallApplyInfo;
import com.greenisland.taxi.manager.CallApplyInfoService;

public class CallApplyThread extends Thread {
	private static Logger log = Logger.getLogger(CallApplyThread.class.getName());
	private CallApplyInfoService applyInfoService;

	private String applyId;

	public CallApplyThread() {
	}

	public CallApplyThread(CallApplyInfoService applyInfoService, String applyId) {
		super();
		this.applyInfoService = applyInfoService;
		this.applyId = applyId;
		new Thread(this).start();
	}

	@Override
	public void run() {
		super.run();
		try {
			log.info("===============开始倒计时任务==============");
			Thread.sleep(60000);
			System.out.println(applyId);
			CallApplyInfo applyInfo = this.applyInfoService.getCallApplyInfoById(applyId);
			String responseState = applyInfo.getResponseState();
			if (responseState.equals(ResponseState.NO_RESPONSE)) {
				applyInfo.setState(ApplicationState.INVALIDATION);
				applyInfo.setDeleteFlag("Y");
				this.applyInfoService.updateApplyInfo(applyInfo);
			}
		} catch (Exception e) {
			log.info("==============倒计时任务处理异常============");
			e.printStackTrace();
		}
	}
}
