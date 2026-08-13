package cn.bugstack.domain.crowdtags.service.detail;

public interface ICrowdTagsExecuteService {

    /**
     * 批处理-人群标签任务
     * @param tagId
     * @param batchId
     */
    void execTagBatchJob(String tagId, String batchId);

}
