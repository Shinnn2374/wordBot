package com.example.WordBot.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserData {
    private String fullName;
    private String birthDate;
    private String gender;
    private String photoPath;

    public UserData(String fullName, String birthDate, String gender, String photoPath) {
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.photoPath = photoPath;
    }
}