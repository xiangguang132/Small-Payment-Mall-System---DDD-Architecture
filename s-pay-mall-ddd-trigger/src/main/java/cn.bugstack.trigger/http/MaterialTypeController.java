package cn.bugstack.trigger.http;

import cn.bugstack.api.request.materialtype.MaterialTypeAddRequest;
import cn.bugstack.api.request.materialtype.MaterialTypeUpdateRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.materialtype.MaterialTypeDetailResponse;
import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.domain.materialtype.service.IMaterialTypeService;
import cn.bugstack.trigger.assembler.MaterialTypeAssembler;
import cn.bugstack.types.enums.ResponseCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/material-type")
public class MaterialTypeController {

    @Resource
    private IMaterialTypeService materialTypeService;

    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody MaterialTypeAddRequest request) {
        MaterialTypeAggregate materialTypeAggregate = MaterialTypeAssembler.toAggregate(request);
        Long id = materialTypeService.addNewMaterialType(materialTypeAggregate);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id).build();
    }

    @GetMapping("{id}")
    public Response<MaterialTypeDetailResponse> detail(@PathVariable Long id) {
        MaterialTypeAggregate materialType = materialTypeService.queryMaterialTypeById(id);
        MaterialTypeDetailResponse response = MaterialTypeAssembler.toDetailResponse(materialType);
        return Response.<MaterialTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        materialTypeService.deleteMaterialTypeById(id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    @PutMapping("{id}")
    public Response<MaterialTypeDetailResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody MaterialTypeUpdateRequest request) {
        MaterialTypeAggregate current = materialTypeService.queryMaterialTypeById(id);
        MaterialTypeAggregate updated = MaterialTypeAssembler.toUpdatedAggregate(current, request);
        materialTypeService.updateMaterialTypeById(updated);

        MaterialTypeAggregate refreshed = materialTypeService.queryMaterialTypeById(id);
        MaterialTypeDetailResponse response = MaterialTypeAssembler.toDetailResponse(refreshed);
        return Response.<MaterialTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @PutMapping("enable/{id}")
    public Response<MaterialTypeDetailResponse> enable(@PathVariable Long id) {
        MaterialTypeAggregate updated = materialTypeService.enableMaterialTypeById(id);
        MaterialTypeDetailResponse response = MaterialTypeAssembler.toDetailResponse(updated);
        return Response.<MaterialTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @PutMapping("disable/{id}")
    public Response<MaterialTypeDetailResponse> disable(@PathVariable Long id) {
        MaterialTypeAggregate updated = materialTypeService.disableMaterialTypeById(id);
        MaterialTypeDetailResponse response = MaterialTypeAssembler.toDetailResponse(updated);
        return Response.<MaterialTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }
}
