package com.example.bird.service;

import com.example.bird.models.Bird;
import com.example.bird.repository.BirdDAO;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class BirdService {

    private final BirdDAO birdDAO;
    private final MongoTemplate mongoTemplate;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String birdPrefix;

    public BirdService(
            BirdDAO birdDAO,
            MongoTemplate mongoTemplate,
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket:birdbook-images}") String bucketName,
            @Value("${aws.s3.bird-prefix:birds}") String birdPrefix
    ) {
        this.birdDAO = birdDAO;
        this.mongoTemplate = mongoTemplate;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.birdPrefix = birdPrefix;
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
        return mongoTemplate.find(mongoQuery, Bird.class)
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    // GET ALL
    public List<Bird> getAllBirds() {
        return birdDAO.findAll()
                .stream()
                .map(this::withResolvedImageUrl)
                .toList();
    }

    // GET BY ID
    public Bird getBirdById(String id) {
        Bird bird = birdDAO.findById(new ObjectId(id))
                .orElseThrow(() -> new IllegalArgumentException("Bird not found"));
        return withResolvedImageUrl(bird);
    }

    // GET BY COMMON NAME
    public Bird getBirdByCommonName(String commonName) {
        Bird bird = birdDAO.findByCommonName(commonName);
        return withResolvedImageUrl(bird);
    }

    // ADD
    public Bird addBird(Bird newBird, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageKey = uploadImageToS3(imageFile);
            newBird.setImageURL(imageKey);
        }
        return withResolvedImageUrl(birdDAO.save(newBird));
    }

    // UPDATE
    public Bird updateBird(ObjectId id, Bird birdRequest, MultipartFile image) {
        Bird existingBird = birdDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bird not found"));

        existingBird.setCommonName(birdRequest.getCommonName());
        existingBird.setScientificName(birdRequest.getScientificName());
        existingBird.setLocation(birdRequest.getLocation());

        if (image != null && !image.isEmpty()) {
            deleteImageIfManagedByS3(existingBird.getImageURL());
            String imageKey = uploadImageToS3(image);
            existingBird.setImageURL(imageKey);
        }

        return withResolvedImageUrl(birdDAO.save(existingBird));
    }

    // DELETE
    public void deleteBird(ObjectId id) {
        Bird existingBird = birdDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bird not found"));

        deleteImageIfManagedByS3(existingBird.getImageURL());
        birdDAO.deleteById(id);
    }

    private String uploadImageToS3(MultipartFile imageFile) {
        try {
            requireS3Configured();

            String cleanPrefix = birdPrefix == null ? "birds" : birdPrefix.trim();
            if (cleanPrefix.isEmpty()) {
                cleanPrefix = "birds";
            }

            String originalFileName = imageFile.getOriginalFilename() == null
                    ? "image"
                    : imageFile.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            String objectKey = cleanPrefix + "/" + UUID.randomUUID() + "_" + originalFileName;
            String contentType = imageFile.getContentType() == null
                    ? "application/octet-stream"
                    : imageFile.getContentType();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(imageFile.getInputStream(), imageFile.getSize())
            );

            return objectKey;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload bird image to S3", e);
        }
    }

    private Bird withResolvedImageUrl(Bird bird) {
        if (bird == null || bird.getImageURL() == null || bird.getImageURL().isBlank()) {
            return bird;
        }

        String imageReference = bird.getImageURL();
        if (imageReference.startsWith("http://") || imageReference.startsWith("https://")) {
            return bird;
        }

        try {
            requireS3Configured();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageReference)
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            String presignedUrl = s3Presigner.presignGetObject(getObjectPresignRequest)
                    .url()
                    .toExternalForm();
            bird.setImageURL(presignedUrl);
        } catch (Exception ignored) {
        }

        return bird;
    }

    private void deleteImageIfManagedByS3(String imageReference) {
        if (imageReference == null || imageReference.isBlank()) {
            return;
        }
        if (imageReference.startsWith("http://") || imageReference.startsWith("https://")) {
            return;
        }

        try {
            requireS3Configured();

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageReference)
                    .build());
        } catch (Exception ignored) {
        }
    }

    private void requireS3Configured() {
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("S3 bucket is not configured. Set AWS_S3_BUCKET.");
        }
    }
}
