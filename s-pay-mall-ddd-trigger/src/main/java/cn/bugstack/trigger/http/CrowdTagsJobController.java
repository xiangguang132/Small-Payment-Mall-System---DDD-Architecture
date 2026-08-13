package cn.bugstack.trigger.http;

import cn.bugstack.api.request.crowdtags.CrowdTagsJobAddRequest;
import cn.bugstack.api.request.crowdtags.CrowdTagsJobPageRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.crowdtags.job.CrowdTagsJobAddResponse;
import cn.bugstack.api.response.crowdtags.job.CrowdTagsJobDetailResponse;
import cn.bugstack.api.response.page.PageResponse;
import cn.bugstack.domain.crowdtags.model.entity.CrowdTagsJobEntity;
import cn.bugstack.domain.crowdtags.service.job.ICrowdTagsJobService;
import cn.bugstack.trigger.assembler.CrowdTagsJobAssembler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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

    @PostMapping("{id}/{action}")
    public Response<Boolean> operateSwitch(@PathVariable Long id, @PathVariable("action") String action) {
        boolean open = resolveOpen(action);
        String actionText = open ? "开启" : "关闭";
        log.info("{}人群标签任务 id:{}", actionText, id);
        crowdTagsJobService.operateSwitch(id, open);
        log.info("{}人群标签任务成功 id:{}", actionText, id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(actionText + "人群标签任务成功，id：" + id)
                .data(true)
                .build();
    }

    private boolean resolveOpen(String action) {
        if ("open".equals(action)) {
            return true;
        }
        if ("close".equals(action)) {
            return false;
        }
        throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签任务操作只支持 open/close");
    }


    @PostMapping("{id}/manual-execute")
    public Response<Boolean> execute(@PathVariable Long id) {
        log.info("人工执行人群标签任务 id:{}", id);
        crowdTagsJobService.manualExecute(id);
        log.info("人工执行人群标签任务完成 id:{}", id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("人工执行人群标签任务完成，id：" + id)
                .data(true)
                .build();
    }

    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        log.info("删除人群标签任务 id:{}", id);
        crowdTagsJobService.deleteCrowdTagsJobById(id);
        log.info("删除人群标签任务完成 id:{}", id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除人群标签任务完成，id：" + id)
                .data(true)
                .build();
    }

    @GetMapping("{id}")
    public Response<CrowdTagsJobDetailResponse> detail(@PathVariable Long id) {
        log.info("查询人群标签任务详情开始 id:{}", id);
        CrowdTagsJobEntity crowdTagJob = crowdTagsJobService.queryCrowdTagJobById(id);
        CrowdTagsJobDetailResponse response = CrowdTagsJobAssembler.toDetailResponse(crowdTagJob);
        log.info("查询人群标签任务详情完成 id:{}", id);
        return Response.<CrowdTagsJobDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @GetMapping
    public Response<PageResponse<CrowdTagsJobDetailResponse>> page(@Valid CrowdTagsJobPageRequest request) {
        log.info("分页查询人群标签任务开始 request:{}", request);
        int pageNo = request.getSafePageNo();
        int pageSize = request.getSafePageSize();
        List<CrowdTagsJobDetailResponse> list = crowdTagsJobService.queryCrowdTagJob(
                request.getTagId(),
                request.getBatchId(),
                request.getTagType(),
                request.getTagRule(),
                pageNo,
                pageSize
        ).stream()
                .map(CrowdTagsJobAssembler::toDetailResponse)
                .collect(Collectors.toList());
        long total = crowdTagsJobService.countCrowdTagJob(
                request.getTagId(),
                request.getBatchId(),
                request.getTagType(),
                request.getTagRule()
        );

        PageResponse<CrowdTagsJobDetailResponse> pageResponse = PageResponse.<CrowdTagsJobDetailResponse>builder()
                .total(total)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .list(list)
                .build();
        log.info("分页查询人群标签任务完成 count:{} total:{}", list.size(), total);
        return Response.<PageResponse<CrowdTagsJobDetailResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(pageResponse)
                .build();
    }
}
