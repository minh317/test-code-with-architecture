package com.example.demo.post.infrastructure;

import java.util.Optional;

public interface PostRepository {

    PostEntity save(PostEntity postEntity);

    Optional<PostEntity> findById(long id);
}
