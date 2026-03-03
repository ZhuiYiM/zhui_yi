// service/impl/TopicServiceImpl.java
package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Topic;
import com.example.demo.entity.Comments;
import com.example.demo.entity.Like;
import com.example.demo.entity.User;
import com.example.demo.entity.dto.CommentDTO;
import com.example.demo.entity.dto.LikeDTO;
import com.example.demo.entity.dto.TopicDTO;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.mapper.CommentsMapper;
import com.example.demo.mapper.LikeMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.TopicService;
import com.example.demo.common.Result;
import com.example.demo.utils.JwtUtil;  // 添加这一行
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;  // 添加这一行
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;  // 添加这一行

    @Override
    public Result createTopic(TopicDTO topicDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || !jwtUtil.validateToken(token, username)) {
            return Result.error("Token无效");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return Result.error("用户不存在");
        }

        Topic topic = new Topic();
        topic.setUserId(user.getId());
        topic.setTitle(topicDTO.getTitle());
        topic.setContent(topicDTO.getContent());
        topic.setTags(topicDTO.getTags() != null ? topicDTO.getTags().toString() : null);
        topic.setLikeCount(0);
        topic.setCommentCount(0);
        topic.setViewCount(0);
        topic.setCreatedAt(LocalDateTime.now());
        topic.setUpdatedAt(LocalDateTime.now());

        boolean result = this.save(topic);
        if (result) {
            return Result.success("话题发布成功");
        } else {
            return Result.error("话题发布失败");
        }
    }

    @Override
    public Result getTopics(Integer page, Integer size, String tag, String keyword) {
        Page<Topic> topicPage = new Page<>(page, size);
        QueryWrapper<Topic> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
        }

        if (tag != null && !tag.isEmpty()) {
            wrapper.like("tags", tag);
        }
        wrapper.orderByDesc("created_at");

        Page<Topic> result = this.page(topicPage, wrapper);
        return Result.success(result);
    }

    @Override
    public Result getTopicDetail(Integer topicId, Integer userId) {
        Topic topic = this.getById(topicId);
        if (topic == null) {
            return Result.error("话题不存在");
        }

        // 增加浏览量
        topic.setViewCount(topic.getViewCount() + 1);
        this.updateById(topic);

        // 查询话题作者信息
        User author = userMapper.selectById(topic.getUserId());

        // 获取话题下的评论
        QueryWrapper<Comments> commentWrapper = new QueryWrapper<>();
        commentWrapper.eq("topic_id", topicId)
                .orderByDesc("created_at");
        List<Comments> comments = commentsMapper.selectList(commentWrapper);

        // 检查当前用户是否点赞了此话题
        boolean hasLiked = false;
        if (userId != null) {
            QueryWrapper<Like> likeWrapper = new QueryWrapper<>();
            likeWrapper.eq("user_id", userId)
                    .eq("target_type", "topic")
                    .eq("target_id", topicId);
            hasLiked = likeMapper.selectCount(likeWrapper) > 0;
        }

        // 构造返回结果
        TopicDetailVO detailVO = new TopicDetailVO();
        detailVO.setTopic(topic);
        detailVO.setAuthor(author);
        detailVO.setComments(comments);
        detailVO.setHasLiked(hasLiked);

        return Result.success(detailVO);
    }

    @Override
    public Result createComment(Integer topicId, CommentDTO commentDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || !jwtUtil.validateToken(token, username)) {
            return Result.error("Token无效");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 检查话题是否存在
        Topic topic = this.getById(topicId);
        if (topic == null) {
            return Result.error("话题不存在");
        }

        Comments comment = new Comments();
        comment.setTopicId(topicId);
        comment.setUserId(user.getId());
        comment.setParentId(commentDTO.getParentId());
        comment.setContent(commentDTO.getContent());
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());

        int result = commentsMapper.insert(comment);
        if (result > 0) {
            // 更新话题的评论数
            topic.setCommentCount(topic.getCommentCount() + 1);
            this.updateById(topic);
            return Result.success("评论发表成功");
        } else {
            return Result.error("评论发表失败");
        }
    }

    @Override
    public Result toggleLike(LikeDTO likeDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || !jwtUtil.validateToken(token, username)) {
            return Result.error("Token无效");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return Result.error("用户不存在");
        }

        QueryWrapper<Like> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user.getId())
                .eq("target_type", likeDTO.getTargetType())
                .eq("target_id", likeDTO.getTargetId());

        Like existingLike = likeMapper.selectOne(wrapper);

        if (existingLike != null) {
            // 取消点赞
            likeMapper.deleteById(existingLike.getId());

            // 更新目标的点赞数
            if ("topic".equals(likeDTO.getTargetType())) {
                Topic topic = this.getById(likeDTO.getTargetId());
                if (topic != null) {
                    topic.setLikeCount(Math.max(0, topic.getLikeCount() - 1));
                    this.updateById(topic);
                }
            } else if ("comment".equals(likeDTO.getTargetType())) {
                Comments comment = commentsMapper.selectById(likeDTO.getTargetId());
                if (comment != null) {
                    comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                    commentsMapper.updateById(comment);
                }
            }

            return Result.success("已取消点赞");
        } else {
            // 添加点赞
            Like like = new Like();
            like.setUserId(user.getId());
            like.setTargetType(likeDTO.getTargetType());
            like.setTargetId(likeDTO.getTargetId());
            like.setCreatedAt(LocalDateTime.now());

            likeMapper.insert(like);

            // 更新目标的点赞数
            if ("topic".equals(likeDTO.getTargetType())) {
                Topic topic = this.getById(likeDTO.getTargetId());
                if (topic != null) {
                    topic.setLikeCount(topic.getLikeCount() + 1);
                    this.updateById(topic);
                }
            } else if ("comment".equals(likeDTO.getTargetType())) {
                Comments comment = commentsMapper.selectById(likeDTO.getTargetId());
                if (comment != null) {
                    comment.setLikeCount(comment.getLikeCount() + 1);
                    commentsMapper.updateById(comment);
                }
            }

            return Result.success("点赞成功");
        }
    }

    @Override
    public Result deleteTopic(Integer topicId, Integer userId) {
        Topic topic = this.getById(topicId);
        if (topic == null) {
            return Result.error("话题不存在");
        }

        // 检查是否有权限删除（只能删除自己的话题）
        if (!topic.getUserId().equals(userId)) {
            return Result.error("无权删除此话题");
        }

        boolean result = this.removeById(topicId);
        if (result) {
            return Result.success("话题删除成功");
        } else {
            return Result.error("话题删除失败");
        }
    }

    @Override
    public Result updateTopic(Integer topicId, TopicDTO topicDTO, Integer userId) {
        Topic topic = this.getById(topicId);
        if (topic == null) {
            return Result.error("话题不存在");
        }

        // 检查是否有权限更新（只能更新自己的话题）
        if (!topic.getUserId().equals(userId)) {
            return Result.error("无权更新此话题");
        }

        topic.setTitle(topicDTO.getTitle());
        topic.setContent(topicDTO.getContent());
        topic.setTags(topicDTO.getTags() != null ? topicDTO.getTags().toString() : null);
        topic.setUpdatedAt(LocalDateTime.now());

        boolean result = this.updateById(topic);
        if (result) {
            return Result.success("话题更新成功");
        } else {
            return Result.error("话题更新失败");
        }
    }

    // 内部类用于封装话题详情
    private static class TopicDetailVO {
        private Topic topic;
        private User author;
        private List<Comments> comments;
        private boolean hasLiked;

        // getter and setter
        public Topic getTopic() { return topic; }
        public void setTopic(Topic topic) { this.topic = topic; }
        public User getAuthor() { return author; }
        public void setAuthor(User author) { this.author = author; }
        public List<Comments> getComments() { return comments; }
        public void setComments(List<Comments> comments) { this.comments = comments; }
        public boolean isHasLiked() { return hasLiked; }
        public void setHasLiked(boolean hasLiked) { this.hasLiked = hasLiked; }
    }
}
