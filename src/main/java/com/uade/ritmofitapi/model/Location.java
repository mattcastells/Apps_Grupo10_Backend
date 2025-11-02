// Archivo: src/main/java/com/uade/ritmofitapi/model/Sede.java

package com.uade.ritmofitapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "locations")
public class Location {

    @Id
    private String id;
    private String name;
    private String address;

    public Location(String name, String address) {
        this.name = name;
        this.address = address;
    }
}