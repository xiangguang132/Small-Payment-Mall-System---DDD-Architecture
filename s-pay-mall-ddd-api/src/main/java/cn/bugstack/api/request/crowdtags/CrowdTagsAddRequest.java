package cn.bugstack.api.request.crowdtags;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CrowdTagsAddRequest {

    @NotBlank(message = "人群标签ID不能为空")
    @Size(max = 32, message = "人群标签ID长度不能超过32")
    private String tagId;

    @NotBlank(message = "人群标签名称不能为空")
    @Size(max = 64, message = "人群标签名称长度不能超过64")
    private String tagName;

    @Size(max = 256, message = "人群标签描述长度不能超过256")
    private String tagDesc;
}
