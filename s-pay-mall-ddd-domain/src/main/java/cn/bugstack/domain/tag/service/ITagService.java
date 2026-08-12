package cn.bugstack.domain.tag.service;

public interface ITagService {

    /**
     * 批处理-人群标签任务
     * @param tagId
     * @param batchId
     */
    void execTagBatchJob(String tagId, String batchId);

}
