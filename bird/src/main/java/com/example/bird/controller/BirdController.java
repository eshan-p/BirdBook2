package com.example.bird.controller;

import com.example.bird.models.Bird;
import com.example.bird.service.BirdService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/birds")
public class BirdController {

    private final BirdService birdService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public BirdController(
            BirdService birdService,
            ObjectMapper objectMapper,
            Validator validator
    ) {
        this.birdService = birdService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchBirds(@RequestParam String query) {
        if(query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Bird> results = birdService.searchBirds(query.trim());
        List<Map<String, Object>> formattedResults = results.stream()
            .map(this::formatBirdResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(formattedResults);
    }

    // GET ALL BIRDS
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllBirds() {
        List<Bird> birds = birdService.getAllBirds();
        List<Map<String, Object>> formattedBirds = birds.stream()
            .map(this::formatBirdResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(formattedBirds);
    }

    // GET BIRD BY ID (USED BY BirdDetail PAGE)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBirdById(@PathVariable String id) {
        try {
            Bird bird = birdService.getBirdById(id);
            return ResponseEntity.ok(formatBirdResponse(bird));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ADD BIRD (MULTIPART)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addBirdMultipart(
            @RequestPart("bird") String birdJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            Bird newBird = objectMapper.readValue(birdJson, Bird.class);

            Set<ConstraintViolation<Bird>> violations = validator.validate(newBird);
            if (!violations.isEmpty()) {
                Map<String, String> errors = new HashMap<>();
                for (ConstraintViolation<Bird> v : violations) {
                    errors.put(v.getPropertyPath().toString(), v.getMessage());
                }
                return ResponseEntity.badRequest().body(errors);
            }

            Bird savedBird = birdService.addBird(newBird, image);
            return ResponseEntity.ok(formatBirdResponse(savedBird));

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid bird data: " + e.getMessage());
        }
    }

    // UPDATE BIRD
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateBirdMultipart(
            @PathVariable String id,
            @RequestPart("bird") String birdJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            Bird birdRequest = objectMapper.readValue(birdJson, Bird.class);
            Bird updatedBird = birdService.updateBird(
                    new ObjectId(id),
                    birdRequest,
                    image
            );
            return ResponseEntity.ok(formatBirdResponse(updatedBird));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE BIRD
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBird(@PathVariable String id) {
        birdService.deleteBird(new ObjectId(id));
        return ResponseEntity.ok("Bird deleted successfully");
    }

    private Map<String, Object> formatBirdResponse(Bird bird) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", bird.getId());
        response.put("commonName", bird.getCommonName());
        response.put("scientificName", bird.getScientificName());
        response.put("imageURL", bird.getImageURL());
        response.put("location", bird.getLocation());
        return response;
    }
}