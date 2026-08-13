package cn.bugstack.api.request.crowdtags;

import cn.bugstack.api.request.page.PageRequest;
import lombok.Data;

@Data
public class CrowdTagsPageRequest extends PageRequest {

    private String tagId;

    private String tagName;
}
