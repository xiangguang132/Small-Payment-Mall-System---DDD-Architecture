package cn.bugstack.api.response.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private Long total;
    private Integer pageNo;
    private Integer pageSize;
    private List<T> list;
}
