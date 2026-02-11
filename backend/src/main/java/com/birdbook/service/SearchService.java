package com.birdbook.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.birdbook.models.Group;
import com.birdbook.models.Post;
import com.birdbook.models.User;
import com.birdbook.models.Bird;

@Service
public class SearchService {
    private final MongoTemplate mongoTemplate;

    public SearchService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Map<String, Object> searchAll(String query) {
        Map<String, Object> results = new HashMap<>();
        results.put("birds", searchBirds(query));
        results.put("users", searchUsers(query));
        results.put("posts", searchPosts(query));
        results.put("groups", searchGroups(query));
        return results;
    }

    public List<Bird> searchBirds(String query) {
        Query mongoQuery = new Query();
        Criteria criteria = new Criteria().orOperator(
            Criteria.where("commonName").regex(query, "i"),
            Criteria.where("scientificName").regex(query, "i")
        );

        mongoQuery.addCriteria(criteria);
        mongoQuery.limit(10);

        return mongoTemplate.find(mongoQuery, Bird.class);
    }

    public List<User> searchUsers(String query) {
        Query mongoQuery = new Query();
        Criteria criteria = Criteria.where("username").regex(query, "i");

        mongoQuery.addCriteria(criteria);
        mongoQuery.limit(10);

        return mongoTemplate.find(mongoQuery, User.class);
    }

    public List<User> searchFriends(String query, ObjectId userId) {
        Query userQuery = new Query(Criteria.where("_id").is(userId));
        User user = mongoTemplate.findOne(userQuery, User.class);
        if (user == null || user.getFriends() == null || user.getFriends().length == 0) {
            return new ArrayList<>();
        }
        List<ObjectId> friendIds = List.of(user.getFriends());
        
        Query friendsQuery = new Query();
        Criteria criteria = new Criteria().andOperator(
            Criteria.where("_id").in(friendIds),
            Criteria.where("username").regex(query, "i")
        );
        
        friendsQuery.addCriteria(criteria);
        friendsQuery.limit(10);
        
        return mongoTemplate.find(friendsQuery, User.class);
    }

    public List<Group> searchGroups(String query) {
        Query mongoQuery = new Query();
        Criteria criteria = Criteria.where("name").regex(query, "i");
        
        mongoQuery.addCriteria(criteria);
        mongoQuery.limit(10);
        
        return mongoTemplate.find(mongoQuery, Group.class);
    }

    public List<Group> searchUserGroups(String query, ObjectId userId) {
        Query userQuery = new Query(Criteria.where("_id").is(userId));
        User user = mongoTemplate.findOne(userQuery, User.class);
        if (user == null || user.getGroups() == null || user.getGroups().length == 0) {
            return new ArrayList<>();
        }
        List<ObjectId> groupIds = List.of(user.getGroups());
        
        Query groupsQuery = new Query();
        Criteria criteria = new Criteria().andOperator(
            Criteria.where("_id").in(groupIds),
            Criteria.where("name").regex(query, "i")
        );
        
        groupsQuery.addCriteria(criteria);
        groupsQuery.limit(10);
        
        return mongoTemplate.find(groupsQuery, Group.class);
    }

    public List<Post> searchPosts(String query) {
        Query mongoQuery = new Query();
        Criteria criteria = new Criteria().orOperator(
            Criteria.where("header").regex(query, "i"),
            Criteria.where("textBody").regex(query, "i")
        );
        
        mongoQuery.addCriteria(criteria);
        mongoQuery.limit(10);
        
        return mongoTemplate.find(mongoQuery, Post.class);
    }

}
