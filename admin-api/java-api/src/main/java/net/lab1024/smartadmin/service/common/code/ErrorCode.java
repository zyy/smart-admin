package net.lab1024.smartadmin.service.common.code;

/**
 * @author zhuoda
 * @Date 2021-09-27
 */
public interface ErrorCode {

    /**
     * 系统等级
     */
    String LEVEL_SYSTEM = "system";

    /**
     * 用户等级
     */
    String LEVEL_USER = "user";

    /**
     * 未预期到的等级
     */
    String LEVEL_UNEXPECTED = "unexpected";

    /**
     * 错误码
     *
     * @return
     */
    int getCode();

    /**
     * 错误消息
     *
     * @return
     */
    String getMsg();

    /**
     * 错误等级
     *
     * @return
     */
    String getLevel();


}
