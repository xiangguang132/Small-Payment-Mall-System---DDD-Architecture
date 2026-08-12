package cn.bugstack.domain.tag.repository;

public interface ITagRepository {

    void addCrowdTagsByUserId(String tagId, String userId);
}
