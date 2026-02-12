package com.example.post.repository;

import com.example.post.models.Post;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostDAO extends MongoRepository<Post, ObjectId> {

    @Query("{ 'tags.?0': { $exists: true } }")
    List<Post> findByTagKey(List<String> tags);

    @Query("{ 'user.userId': ?0 }")
    List<Post> findByUserId(ObjectId userId);

    @Query("{ 'flagged': true }")
    List<Post> findFlaggedPosts();

    @Query("{ 'help': true }")
    List<Post> findPostsNeedingHelp();

    @Query("{ 'likes': ?0 }")
    List<Post> findPostsLikedByUser(ObjectId userId);
    
    @Query("{ 'group': ?0 }")
    List<Post> findByGroup(ObjectId groupId);
}