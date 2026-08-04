package cn.bugstack.api.request.page;

import lombok.Data;

@Data
public class PageRequest {

    private Integer pageNo = 1;
    private Integer pageSize = 10;

    public Integer offset() {
        return (getSafePageNo() - 1) * getSafePageSize();
    }

    public Integer limit() {
        return getSafePageSize();
    }

    public Integer getSafePageNo() {
        return pageNo == null || pageNo <= 0 ? 1 : pageNo;
    }

    public Integer getSafePageSize() {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

}
