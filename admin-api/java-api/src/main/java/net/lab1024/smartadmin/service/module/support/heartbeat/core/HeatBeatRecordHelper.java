package net.lab1024.smartadmin.service.module.support.heartbeat.core;


import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;

/**
 * @Description: 心跳工具类
 * @Author: 1024lab
 * @Date: 2018/7/9 11:48
 */
public class HeatBeatRecordHelper {

    /**
     * 获取进程号
     *
     * @return
     */
    public static final Integer getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0])
                .intValue();
    }

    /**
     * 获取项目名称
     *
     * @return
     */
    public static final String getProjectPath() {
        return System.getProperty("user.dir");
    }

    /**
     * 获取进程启动时间
     *
     * @return
     */
    public static final Date getStartTime() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return new Date(runtimeMXBean.getStartTime());
    }


}