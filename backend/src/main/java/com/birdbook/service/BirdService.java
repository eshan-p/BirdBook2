package com.birdbook.service;

import com.birdbook.models.Bird;
import com.birdbook.repository.BirdDAO;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class BirdService {

    private final BirdDAO birdDAO;
    private final MongoTemplate mongoTemplate;

    public BirdService(BirdDAO birdDAO, MongoTemplate mongoTemplate) {
        this.birdDAO = birdDAO;
        this.mongoTemplate = mongoTemplate;
    }

    // SEARCH (optional, does not break anything)
    public List<Bird> searchBirds(String query) {
        Query mongoQuery = new Query();
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("commonName").regex(query, "i"),
                Criteria.where("scientificName").regex(query, "i")
        );
        mongoQuery.addCriteria(criteria);
        mongoQuery.limit(20);
        return mongoTemplate.find(mongoQuery, Bird.class);
    }

    // GET ALL
    public List<Bird> getAllBirds() {
        return birdDAO.findAll();
    }

    // GET BY ID
    public Bird getBirdById(String id) {
        return birdDAO.findById(new ObjectId(id))
                .orElseThrow(() -> new IllegalArgumentException("Bird not found"));
    }

    // GET BY COMMON NAME
    public Bird getBirdByCommonName(String commonName) {
        return birdDAO.findByCommonName(commonName);
    }

    // ADD
    public Bird addBird(Bird newBird, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            newBird.setImageURL(imagePath);
        }
        return birdDAO.save(newBird);
    }

    // UPDATE
    public Bird updateBird(ObjectId id, Bird birdRequest, MultipartFile image) {
        Bird existingBird = birdDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bird not found"));

        existingBird.setCommonName(birdRequest.getCommonName());
        existingBird.setScientificName(birdRequest.getScientificName());
        existingBird.setLocation(birdRequest.getLocation());

        if (image != null && !image.isEmpty()) {
            String imagePath = saveImage(image);
            existingBird.setImageURL(imagePath);
        }

        return birdDAO.save(existingBird);
    }

    // DELETE
    public void deleteBird(ObjectId id) {
        if (!birdDAO.existsById(id)) {
            throw new IllegalArgumentException("Bird not found");
        }
        birdDAO.deleteById(id);
    }

    // IMAGE SAVE HELPER
    private String saveImage(MultipartFile imageFile) {
        try {
            String uploadDir = "images";
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.copy(
                    imageFile.getInputStream(),
                    filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return uploadDir + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }
    }
}


    //create crud
    /* public Bird addBird(Bird newBird) {
        return birdDAO.insert(newBird);
    } */

    /*public Bird updateBird(ObjectId id, Bird birdRequest) {
        Bird existingBird = birdDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Bird not found."));

        existingBird.setCommonName(birdRequest.getCommonName());
        existingBird.setImageURL(birdRequest.getImageURL());

        return birdDAO.save(existingBird);
    }*/
