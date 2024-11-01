package com.example.demo.post.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    final PostJpaRepository _postJpaRepository;

    @Override
    public PostEntity save(PostEntity postEntity) {
        return _postJpaRepository.save(postEntity);
    }

    @Override
    public Optional<PostEntity> findById(long id) {
        return _postJpaRepository.findById(id);
    }
}
