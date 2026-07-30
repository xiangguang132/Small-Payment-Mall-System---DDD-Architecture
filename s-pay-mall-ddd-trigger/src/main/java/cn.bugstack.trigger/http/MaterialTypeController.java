package cn.bugstack.trigger.http;

import cn.bugstack.api.request.materialtype.MaterialTypeAddRequest;
import cn.bugstack.api.response.Response;
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

//    @GetMapping("")
}
