package cn.bugstack.trigger.http;

import cn.bugstack.api.request.materialtype.MaterialTypeAddRequest;
import cn.bugstack.api.request.materialtype.MaterialTypeUpdateRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.materialtype.MaterialTypeDetailResponse;
import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.domain.materialtype.service.IMaterialTypeService;
import cn.bugstack.trigger.assembler.MaterialTypeAssembler;
import cn.bugstack.trigger.interceptor.RequireRole;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.enums.RoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/material-type")
@RequireRole({RoleEnum.ADMIN, RoleEnum.INVENTORY_MANAGER})
@Slf4j
public class MaterialTypeController {

    @Resource
    private IMaterialTypeService materialTypeService;

    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody MaterialTypeAddRequest request) {
        log.info("新增原料分类开始 request:{}", request);
        MaterialTypeAggregate materialTypeAggregate = MaterialTypeAssembler.toAggregate(request);
        Long id = materialTypeService.addNewMaterialType(materialTypeAggregate);
        log.info("新增原料分类完成 id:{}", id);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id).build();
    }

    @GetMapping("{id}")
    public Response<MaterialTypeDetailResponse> detail(@PathVariable Long id) {
        log.info("查询原料分类详情开始 id:{}", id);
        MaterialTypeAggregate materialType = materialTypeService.queryMaterialTypeById(id);
        MaterialTypeDetailResponse response = MaterialTypeAssembler.toDetailResponse(materialType);
        log.info("查询原料分类详情完成 id:{}", id);
        return Response.<MaterialTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        log.info("删除原料分类开始 id:{}", id);
        materialTypeService.deleteMaterialTypeById(id);
        log.info("删除原料分类完成 id:{}", id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    @PutMapping("{id}")
    public Response<MaterialTypeDetailResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody MaterialTypeUpdateRequest request) {
        log.info("更新原料分类开始 id:{} request:{}", id, request);
        MaterialTypeAggregate current = materialTypeService.queryMaterialTypeById(id);
        MaterialTypeAggregate updated = MaterialTypeAssembler.toUpdatedAggregate(current, request);
        materialTypeService.updateMaterialTypeById(updated);

        MaterialTypeAggregate refreshed = materialTypeService.queryMaterialTypeById(id);
        MaterialTypeDetailResponse response = MaterialTypeAssembler.toDetailResponse(refreshed);
        log.info("更新原料分类完成 id:{}", id);
        return Response.<MaterialTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @PutMapping("enable/{id}")
    public Response<MaterialTypeDetailResponse> enable(@PathVariable Long id) {
        log.info("启用原料分类开始 id:{}", id);
        MaterialTypeAggregate updated = materialTypeService.enableMaterialTypeById(id);
        MaterialTypeDetailResponse response = MaterialTypeAssembler.toDetailResponse(updated);
        log.info("启用原料分类完成 id:{}", id);
        return Response.<MaterialTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @PutMapping("disable/{id}")
    public Response<MaterialTypeDetailResponse> disable(@PathVariable Long id) {
        log.info("禁用原料分类开始 id:{}", id);
        MaterialTypeAggregate updated = materialTypeService.disableMaterialTypeById(id);
        MaterialTypeDetailResponse response = MaterialTypeAssembler.toDetailResponse(updated);
        log.info("禁用原料分类完成 id:{}", id);
        return Response.<MaterialTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }
}
