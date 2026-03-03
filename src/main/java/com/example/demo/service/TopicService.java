// service/TopicService.java
package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Topic;
import com.example.demo.entity.dto.CommentDTO;
import com.example.demo.entity.dto.LikeDTO;
import com.example.demo.entity.dto.TopicDTO;
import com.example.demo.common.Result;

import jakarta.servlet.http.HttpServletRequest;

public interface TopicService extends IService<Topic> {
    Result createTopic(TopicDTO topicDTO, HttpServletRequest request);
    Result getTopics(Integer page, Integer size, String tag, String keyword);
    Result getTopicDetail(Integer topicId, Integer userId);
    Result createComment(Integer topicId, CommentDTO commentDTO, HttpServletRequest request);
    Result toggleLike(LikeDTO likeDTO, HttpServletRequest request);
    Result deleteTopic(Integer topicId, Integer userId);
    Result updateTopic(Integer topicId, TopicDTO topicDTO, Integer userId);
}
