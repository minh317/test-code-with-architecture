package com.example.demo.post.service;

import com.example.demo.common.exception.ResourceNotFoundException;
import com.example.demo.post.domain.PostCreate;
import com.example.demo.post.domain.PostUpdate;
import com.example.demo.post.infrastructure.PostEntity;
import com.example.demo.post.infrastructure.PostRepository;
import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.UserEntity;
import java.time.Clock;

import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository _postRepository;
    private final UserService userService;

    public PostEntity getById(long id) {
        return _postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Posts", id));
    }

    public PostEntity create(PostCreate postCreate) {
        User user = userService.getById(postCreate.getWriterId());

        PostEntity postEntity = new PostEntity();
        postEntity.setWriter(UserEntity.fromModel(user));
        postEntity.setContent(postCreate.getContent());
        postEntity.setCreatedAt(Clock.systemUTC().millis());

        return _postRepository.save(postEntity);
    }

    public PostEntity update(long id, PostUpdate postUpdate) {
        PostEntity postEntity = getById(id);
        postEntity.setContent(postUpdate.getContent());
        postEntity.setModifiedAt(Clock.systemUTC().millis());
        return _postRepository.save(postEntity);
    }
}