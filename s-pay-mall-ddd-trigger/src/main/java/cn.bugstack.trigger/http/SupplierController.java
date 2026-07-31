package cn.bugstack.trigger.http;

import cn.bugstack.api.request.supplier.SupplierAddRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.supplier.SupplierDetailResponse;
import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;
import cn.bugstack.domain.supplier.service.ISupplierService;
import cn.bugstack.trigger.assembler.SupplierAssembler;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/supplier")
@Slf4j
public class SupplierController {

    @Resource
    private ISupplierService supplierService;

    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody SupplierAddRequest request) {
        log.info("新增供应商开始 request:{}", request);
        SupplierAggregate supplier = SupplierAssembler.toAggregate(request);
        Long id = supplierService.addNewSupplier(supplier);
        log.info("新增供应商完成 id:{}", id);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id)
                .build();
    }

    @GetMapping("{id}")
    public Response<SupplierDetailResponse> detail(@PathVariable Long id) {
        log.info("查询供应商详情开始 id:{}", id);
        SupplierAggregate supplier = supplierService.querySupplierById(id);
        SupplierDetailResponse response = SupplierAssembler.toDetailResponse(supplier);
        log.info("查询供应商详情完成 id:{}", id);
        return Response.<SupplierDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @PutMapping("{id}")
    public Response<SupplierDetailResponse> update(@PathVariable Long id, @RequestBody SupplierAddRequest request) {
        log.info("更新供应商开始 id:{} request:{}", id, request);
        SupplierAggregate current = supplierService.querySupplierById(id);
        if (current == null) {
            throw new IllegalArgumentException("供应商不存在");
        }
        SupplierAggregate updated = SupplierAggregate.builder()
                .id(current.getId())
                .supplierCode(request.getSupplierCode() != null ? request.getSupplierCode().trim() : current.getSupplierCode())
                .name(request.getName() != null ? request.getName().trim() : current.getName())
                .contactName(request.getContactName() != null ? request.getContactName().trim() : current.getContactName())
                .contactPhone(request.getContactPhone() != null ? request.getContactPhone().trim() : current.getContactPhone())
                .address(request.getAddress() != null ? request.getAddress().trim() : current.getAddress())
                .status(request.getStatus() != null ? request.getStatus() : current.getStatus())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .build();
        supplierService.updateSupplierById(updated);

        SupplierAggregate refreshed = supplierService.querySupplierById(id);
        SupplierDetailResponse response = SupplierAssembler.toDetailResponse(refreshed);
        log.info("更新供应商完成 id:{}", id);
        return Response.<SupplierDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        log.info("删除供应商开始 id:{}", id);
        supplierService.deleteSupplierById(id);
        log.info("删除供应商完成 id:{}", id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }


}
