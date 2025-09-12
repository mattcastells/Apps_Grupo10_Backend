
package com.uade.ritmofitapi.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

// Import GymClass for taughtClasses
import com.uade.ritmofitapi.model.GymClass;

@Data
@EqualsAndHashCode(callSuper = true)
public class Professor extends User {

	// List of class types the professor teaches
	private List<String> classTypes;

	// List of classes taught by the professor
	private List<GymClass> taughtClasses;

	// Role is set to 'professor' by default
	private final String role = "professor";

	public Professor() {
		super();
	}

	public Professor(String email) {
		super(email);
	}
}
