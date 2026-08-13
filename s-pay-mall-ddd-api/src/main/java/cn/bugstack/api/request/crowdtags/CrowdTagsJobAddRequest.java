package cn.bugstack.api.request.crowdtags;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CrowdTagsJobAddRequest {

    @NotBlank(message = "人群标签ID不能为空")
    @Size(max = 32, message = "人群标签ID长度不能超过32")
    private String tagId;

    @NotNull(message = "标签类型不能为空")
    @Min(value = 0, message = "标签类型不正确")
    @Max(value = 1, message = "标签类型不正确")
    private Integer tagType;

    @NotBlank(message = "打标规则不能为空")
    @Size(max = 256, message = "打标规则长度不能超过256")
    private String tagRule;
}
