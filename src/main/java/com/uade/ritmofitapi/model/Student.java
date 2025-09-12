
package com.uade.ritmofitapi.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Student extends User {

	// Role is set to 'student' by default
	private final String role = "student";

    // List of current bookings (reserved classes)
    private List<Booking> currentBookings;

	// List of all classes the user has attended (class history)
    private List<GymClass> classHistory;

	public Student() {
		super();
	}

	public Student(String email) {
		super(email);
	}
}
