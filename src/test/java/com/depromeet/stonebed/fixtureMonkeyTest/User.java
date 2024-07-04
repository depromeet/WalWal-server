package com.depromeet.stonebed.fixtureMonkeyTest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class User {

    @NotNull private String personId;

    @NotBlank private String personName;

    private Gender gender;

    @Size(min = 1, max = 16)
    private String personNo;

    @Size(min = 1, max = 16)
    private int age;

    private List<String> addressList;

    public enum Gender {
        MAN,
        WOMAN
    }
}
