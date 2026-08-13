package cn.bugstack.api.request.crowdtags;

import cn.bugstack.api.request.page.PageRequest;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CrowdTagsJobPageRequest extends PageRequest {

    private String tagId;

    private String batchId;

    private Integer tagType;

    private String tagRule;

}
