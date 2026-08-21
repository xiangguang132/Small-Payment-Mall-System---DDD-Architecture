package cn.bugstack.api.response.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockOrderResponse {

    // 锁单号
    private String lockId;

    // 锁单时间
    private LocalDateTime lockTime;

}
