package org.avlasov.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class User implements Serializable {

    private int userId;
    private int age;
    private String gender;
    private String occupation;
    private String zip;

}
