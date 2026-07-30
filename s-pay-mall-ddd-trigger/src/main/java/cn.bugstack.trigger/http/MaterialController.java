package cn.bugstack.trigger.http;

import cn.bugstack.api.request.material.MaterialAddRequest;
import cn.bugstack.api.request.material.MaterialUpdateRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.material.MaterialDetailResponse;
import cn.bugstack.domain.material.model.aggregate.MaterialAggregate;
import cn.bugstack.domain.material.service.IMaterialService;
import cn.bugstack.trigger.assembler.MaterialAssembler;
import cn.bugstack.types.enums.ResponseCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/material")
public class MaterialController {

    @Resource
    private IMaterialService materialService;

    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody MaterialAddRequest request) {
        MaterialAggregate material = MaterialAssembler.toAggregate(request);
        Long id = materialService.addNewMaterial(material);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id)
                .build();
    }

    @GetMapping("{id}")
    public Response<MaterialDetailResponse> detail(@PathVariable Long id) {
        MaterialAggregate material = materialService.queryMaterialById(id);
        MaterialDetailResponse response = MaterialAssembler.toDetailResponse(material);
        return Response.<MaterialDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        materialService.deleteMaterialById(id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    @PutMapping("{id}")
    public Response<MaterialDetailResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody MaterialUpdateRequest request) {
        MaterialAggregate current = materialService.queryMaterialById(id);
        if (current == null) {
            throw new IllegalArgumentException("原料不存在");
        }
        MaterialAggregate updated = MaterialAssembler.toUpdatedAggregate(current, request);
        materialService.updateMaterialById(updated);

        MaterialAggregate refreshed = materialService.queryMaterialById(id);
        MaterialDetailResponse response = MaterialAssembler.toDetailResponse(refreshed);
        return Response.<MaterialDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }
}
