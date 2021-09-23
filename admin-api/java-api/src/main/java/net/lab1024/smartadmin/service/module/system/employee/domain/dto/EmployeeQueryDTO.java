package net.lab1024.smartadmin.service.module.system.employee.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.lab1024.smartadmin.service.common.domain.PageBaseDTO;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * 员工列表DTO
 *
 * @author 开云
 * @date 2017年12月21日上午09:09:31
 */
@Data
public class EmployeeQueryDTO extends PageBaseDTO {

    @ApiModelProperty("搜索词")
    @Length(max = 20, message = "搜索词最多20字符")
    private String keyword;

    @ApiModelProperty("部门id")
    private Long departmentId;

    @ApiModelProperty("是否禁用")
    private Boolean disabledFlag;

    @ApiModelProperty("员工id集合")
    @Size(max = 99, message = "最多查询99个员工")
    private List<Long> employeeIdList;

    @ApiModelProperty(hidden = true)
    private Boolean deletedFlag;
}