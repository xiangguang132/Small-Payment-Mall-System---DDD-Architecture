package cn.bugstack.domain.crowdtags.repository.detail;

public interface ICrowdTagsDetailRepository {

    void addCrowdTagsByUserId(String tagId, String userId);
}
