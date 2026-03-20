package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Comments;
import com.example.demo.mapper.CommentsMapper;
import com.example.demo.service.CommentsService;
import org.springframework.stereotype.Service;

/**
 * 评论服务实现类
 */
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements CommentsService {
}
