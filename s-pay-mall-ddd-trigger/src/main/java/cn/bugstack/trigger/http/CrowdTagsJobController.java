package cn.bugstack.trigger.http;

import cn.bugstack.api.request.crowdtags.CrowdTagsJobAddRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.crowdtags.job.CrowdTagsJobAddResponse;
import cn.bugstack.domain.crowdtags.model.entity.CrowdTagsJobEntity;
import cn.bugstack.domain.crowdtags.service.job.ICrowdTagsJobService;
import cn.bugstack.trigger.assembler.CrowdTagsJobAssembler;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/crowd-tags-jobs/")
public class CrowdTagsJobController {

    @Resource
    private ICrowdTagsJobService crowdTagsJobService;

    @PostMapping
    public Response<CrowdTagsJobAddResponse> add(@Valid @RequestBody CrowdTagsJobAddRequest request) {
        log.info("新增人群标签任务开始：request:{}", request);

        Long id = crowdTagsJobService.addCrowdTagJob(CrowdTagsJobAssembler.toEntity(request));
        CrowdTagsJobEntity crowdTagJob = crowdTagsJobService.queryCrowdTagJobById(id);
        CrowdTagsJobAddResponse response = CrowdTagsJobAssembler.toAddResponse(crowdTagJob);
        log.info("新增人群标签任务完成 id:{}", id);
        return Response.<CrowdTagsJobAddResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

}
