package net.lab1024.smartadmin.service.common.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 分页基础参数
 *
 * @author 善逸
 * @Date Created in 2017/10/28 16:19
 */
@Data
public class PageBaseDTO {

    @ApiModelProperty(value = "页码(不能为空)", required = true, example = "1")
    @NotNull(message = "分页参数不能为空")
    @Min(value = 1, message = "分页参数最小1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页数量(不能为空)", required = true, example = "10")
    @NotNull(message = "每页数量不能为空")
    @Range(min = 1, max = 200, message = "每页数量1-200")
    private Integer pageSize;

    @ApiModelProperty("排序字段集合")
    @Size(max = 10, message = "排序字段最多10")
    @Valid
    private List<SortItemDTO> sortItemList;

    /**
     * 排序DTO类
     */
    @Data
    public static class SortItemDTO {

        @ApiModelProperty("true正序|false倒序")
        @NotNull(message = "排序规则不能为空")
        private Boolean isAsc;

        @ApiModelProperty(value = "排序字段")
        @NotBlank(message = "排序字段不能为空")
        @Length(max = 30, message = "排序字段最多30")
        private String column;
    }
}
