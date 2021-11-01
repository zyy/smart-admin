package net.lab1024.smartadmin.service.module.system.role.domain.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.lab1024.smartadmin.service.common.domain.PageParam;

/**
 * [  ]
 *
 * @author yandanyang
 * @version 1.0
 * @company 1024lab.net
 * @copyright (c) 2019 1024lab.netInc. All rights reserved.
 * @date
 * @since JDK1.8
 */
@Data
public class RoleEmployeeQueryForm extends PageParam {

    @ApiModelProperty("关键字")
    private String keywords;

    @ApiModelProperty("角色id")
    private String roleId;
}
