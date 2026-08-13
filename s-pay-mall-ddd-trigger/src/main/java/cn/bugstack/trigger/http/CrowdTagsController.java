package cn.bugstack.trigger.http;

import cn.bugstack.api.request.crowdtags.CrowdTagsAddRequest;
import cn.bugstack.api.request.crowdtags.CrowdTagsPageRequest;
import cn.bugstack.api.request.crowdtags.CrowdTagsUpdateRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.crowdtags.detail.CrowdTagsDetailResponse;
import cn.bugstack.api.response.page.PageResponse;
import cn.bugstack.domain.crowdtags.model.aggregate.CrowdTagsAggregate;
import cn.bugstack.domain.crowdtags.service.tag.ICrowdTagsService;
import cn.bugstack.trigger.assembler.CrowdTagsAssembler;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/crowd-tags")
@Slf4j
public class CrowdTagsController {

    @Resource
    private ICrowdTagsService crowdTagsService;

    @PostMapping("add")
    public Response<CrowdTagsDetailResponse> add(@Valid @RequestBody CrowdTagsAddRequest request) {
        log.info("新增人群标签开始 request:{}", request);
        Long id = crowdTagsService.addCrowdTag(CrowdTagsAssembler.toAggregate(request));
        CrowdTagsAggregate crowdTag = crowdTagsService.queryCrowdTagById(id);
        CrowdTagsDetailResponse response = CrowdTagsAssembler.toDetailResponse(crowdTag);
        log.info("新增人群标签完成 id:{}", id);
        return Response.<CrowdTagsDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        log.info("删除人群标签开始 id:{}", id);
        crowdTagsService.deleteCrowdTagById(id);
        log.info("删除人群标签完成 id:{}", id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    @PutMapping("{id}")
    public Response<CrowdTagsDetailResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody CrowdTagsUpdateRequest request) {
        log.info("更新人群标签开始 id:{} request:{}", id, request);
        CrowdTagsAggregate current = crowdTagsService.queryCrowdTagById(id);
        CrowdTagsAggregate updated = crowdTagsService.updateCrowdTag(
                id,
                CrowdTagsAssembler.toUpdatedAggregate(current, request)
        );
        CrowdTagsDetailResponse response = CrowdTagsAssembler.toDetailResponse(updated);
        log.info("更新人群标签完成 id:{}", id);
        return Response.<CrowdTagsDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @GetMapping("{id}")
    public Response<CrowdTagsDetailResponse> detail(@PathVariable Long id) {
        log.info("查询人群标签详情开始 id:{}", id);
        CrowdTagsAggregate crowdTag = crowdTagsService.queryCrowdTagById(id);
        CrowdTagsDetailResponse response = CrowdTagsAssembler.toDetailResponse(crowdTag);
        log.info("查询人群标签详情完成 id:{}", id);
        return Response.<CrowdTagsDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @GetMapping("page")
    public Response<PageResponse<CrowdTagsDetailResponse>> page(@Valid CrowdTagsPageRequest request) {
        log.info("分页查询人群标签开始 request:{}", request);
        int pageNo = request.getSafePageNo();
        int pageSize = request.getSafePageSize();
        List<CrowdTagsDetailResponse> list = crowdTagsService.queryCrowdTags(
                        request.getTagId(),
                        request.getTagName(),
                        pageNo,
                        pageSize
                ).stream()
                .map(CrowdTagsAssembler::toDetailResponse)
                .collect(Collectors.toList());
        long total = crowdTagsService.countCrowdTags(request.getTagId(), request.getTagName());

        PageResponse<CrowdTagsDetailResponse> pageResponse = PageResponse.<CrowdTagsDetailResponse>builder()
                .total(total)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .list(list)
                .build();
        log.info("分页查询人群标签完成 count:{} total:{}", list.size(), total);
        return Response.<PageResponse<CrowdTagsDetailResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(pageResponse)
                .build();
    }
}
