package net.lab1024.smartadmin.service.module.system.datascope.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * [ 数据范围与角色关系 ]
 *
 * @author 罗伊
 */
@Data
@TableName("t_role_data_scope")
public class DataScopeRoleEntity {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据范围id
     * {@link net.lab1024.smartadmin.service.module.system.datascope.constant.DataScopeTypeEnum}
     */
    private Integer dataScopeType;

    /**
     * 数据范围类型
     * {@link net.lab1024.smartadmin.service.module.system.datascope.constant.DataScopeViewTypeEnum}
     */
    private Integer viewType;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}