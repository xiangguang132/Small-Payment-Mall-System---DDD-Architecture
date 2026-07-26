package cn.bugstack.api;

import cn.bugstack.api.dto.CreatePayRequestDTO;
import cn.bugstack.api.response.Response;

public interface IPayService {

    Response<String> createPayOrder(CreatePayRequestDTO createPayRequestDTO);

}
