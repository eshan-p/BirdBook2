package com.birdbook.BirdTests;

import com.birdbook.models.Bird;
import com.birdbook.repository.BirdDAO;
import com.birdbook.service.BirdService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BirdServiceTests {

    @Mock
    private BirdDAO birdDAO;

    @InjectMocks
    private BirdService birdService;

    private Bird bird;
    private ObjectId birdId;

    @BeforeEach
    void setUp() {
        birdId = new ObjectId();
        bird = new Bird();
        bird.setObjectId(birdId);
        bird.setCommonName("Cardinal");
        bird.setImageURL("image-url");
    }

    @Test
    void getBirdByCommonName_returnsBird() {
        when(birdDAO.findByCommonName("Cardinal")).thenReturn(bird);

        Bird result = birdService.getBirdByCommonName("Cardinal");

        assertNotNull(result);
        verify(birdDAO).findByCommonName("Cardinal");
 
        assertEquals("Cardinal", result.getCommonName());
        assertEquals(birdId.toHexString(), result.getId());
    }

    @Test
    void getAllBirds_returnsList() {
        when(birdDAO.findAll()).thenReturn(List.of(bird));

        List<Bird> birds = birdService.getAllBirds();

        assertEquals(1, birds.size());
        verify(birdDAO).findAll();
    }

    @Test
    void addBird_insertsAndReturnsBird() {
        when(birdDAO.save(bird)).thenReturn(bird);

        Bird result = birdService.addBird(bird, null);

        assertNotNull(result);
        verify(birdDAO).save(bird);
        
        assertEquals("Cardinal", result.getCommonName());
        assertEquals("image-url", result.getImageURL());
    }

    @Test
    void deleteBird_whenBirdExists_deletesBird() {
        when(birdDAO.existsById(birdId)).thenReturn(true);

        birdService.deleteBird(birdId);

        verify(birdDAO).deleteById(birdId);
    }

    @Test
    void deleteBird_whenBirdDoesNotExist_throwsException() {
        when(birdDAO.existsById(birdId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> birdService.deleteBird(birdId)
        );

        assertEquals("Bird not found", exception.getMessage());
        verify(birdDAO, never()).deleteById(any());
    }

    @Test
    void updateBird_whenBirdExists_updatesAndSaves() {
        Bird updateRequest = new Bird();
        updateRequest.setCommonName("Blue Jay");
        updateRequest.setScientificName("Cyanocitta cristata");
        updateRequest.setImageURL("new-image-url");

        when(birdDAO.findById(birdId)).thenReturn(Optional.of(bird));
        when(birdDAO.save(any(Bird.class))).thenAnswer(invocation -> invocation.getArgument(0));

        birdService.updateBird(birdId, updateRequest, null);

        verify(birdDAO).save(bird);
        
        assertEquals("Blue Jay", bird.getCommonName());
        assertEquals("Cyanocitta cristata", bird.getScientificName());
    }

    @Test
    void updateBird_whenBirdDoesNotExist_throwsException() {
        when(birdDAO.findById(birdId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> birdService.updateBird(birdId, bird, null)
        );

        assertEquals("Bird not found", exception.getMessage());
        verify(birdDAO, never()).save(any());
    }
}

