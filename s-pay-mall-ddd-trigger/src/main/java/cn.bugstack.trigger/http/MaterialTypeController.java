package cn.bugstack.trigger.http;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/material-type")
public class MaterialTypeController {



//    @GetMapping("")
}
