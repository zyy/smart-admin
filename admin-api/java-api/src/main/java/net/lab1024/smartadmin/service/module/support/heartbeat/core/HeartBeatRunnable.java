package net.lab1024.smartadmin.service.module.support.heartbeat.core;

import net.lab1024.smartadmin.service.common.util.SmartIPUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * [  ]
 *
 * @author 罗伊
 * @date 2021/9/23 18:52
 */
public class HeartBeatRunnable implements Runnable {

    /**
     * 项目路径
     */
    private String projectPath;
    /**
     * 服务器ip（多网卡）
     */
    private List<String> serverIps;
    /**
     * 进程号
     */
    private Integer processNo;
    /**
     * 进程开启时间
     */
    private LocalDateTime processStartTime;

    private IHeartBeatRecordHandler recordHandler;

    public HeartBeatRunnable(IHeartBeatRecordHandler recordHandler) {
        this.recordHandler = recordHandler;
        this.initServerInfo();
    }

    /**
     * 初始化心跳相关信息
     */
    private void initServerInfo(){
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
       this.projectPath = System.getProperty("user.dir");
       this.serverIps = SmartIPUtil.getLocalHostIPList();
       this.processNo = Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
       this.processStartTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(runtimeMXBean.getStartTime()), ZoneId.systemDefault());
    }


    @Override
    public void run() {
        HeartBeatRecord heartBeatRecord = new HeartBeatRecord();
        heartBeatRecord.setProjectPath(this.projectPath);
        heartBeatRecord.setServerIp(StringUtils.join(this.serverIps, ";"));
        heartBeatRecord.setProcessNo(this.processNo);
        heartBeatRecord.setProcessStartTime(this.processStartTime);
        heartBeatRecord.setHeartBeatTime(LocalDateTime.now());
        recordHandler.handler(heartBeatRecord);
    }
}
