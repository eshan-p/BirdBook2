package com.birdbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// import com.birdbook.controller.BirdController;
// import com.birdbook.repository.impl.BirdDAOimpl;
// import com.birdbook.service.BirdService;

@SpringBootApplication
public class BirdbookApplication {
	public static void main(String[] args) {
		//BirdDAOimpl birdDAOimpl = new BirdDAOimpl("birds");
		//BirdService birdService = new BirdService(birdDAOimpl);
		//BirdController controller = new BirdController(birdService);
		//System.out.println(controller.getAllBirds());
		SpringApplication.run(BirdbookApplication.class, args);
	}	
}
