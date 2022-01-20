package net.lab1024.smartadmin.service.module.system.employee.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工实体类
 *
 * @author 李开云
 * @date 2017年12月19日下午1:34:48
 */
@Data
@TableName("t_employee")
public class EmployeeEntity {

    @TableId(type = IdType.AUTO)
    private Long employeeId;

    /**
     * 登录账号
     */
    private String loginName;

    /**
     * 登录密码
     */
    private String loginPwd;

    /**
     * 员工名称
     */
    private String actualName;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 部门id
     */
    private Long departmentId;

    /**
     * 是否为超级管理员: 0 不是，1是
     */
    private Boolean administratorFlag;

    /**
     * 是否被禁用 0否1是
     */
    private Boolean disabledFlag;

    /**
     * 是否删除0否 1是
     */
    private Boolean deletedFlag;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;


}
