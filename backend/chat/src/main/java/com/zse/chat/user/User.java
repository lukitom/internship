package com.zse.chat.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.TimeZone;

@Entity(name = "chat_user")
@Data
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Builder
public class User {
    @Id
    @GeneratedValue(generator = "sequence-generator-user")
    @GenericGenerator(
            name = "sequence-generator-user",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "user_sequence"),
                    @Parameter(name = "initial value", value = "0"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    int id;
    @Column(unique = true, nullable = false)
    String nickname;
    String firstName;
    String lastName;
    @Column(unique = true, nullable = false)
    String email;
    String phoneNumber;
    String country;
    String city;
    UserStatus userStatus;
    Language userLanguage;
    TimeZone timeZone;


    Boolean showFirstNameAndLastName;
    Boolean showEmail;
    Boolean showPhoneNumber;
    Boolean showAddress;
    Boolean deleted;

    protected User(){
        this.id = 0;
        this.nickname = "";
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.country = "";
        this.city = "";
        this.phoneNumber = "";

        this.userStatus = UserStatus.OFFLINE;
        this.userLanguage = Language.POLISH;
        this.timeZone = TimeZone.getTimeZone("Europe/Warsaw");

        this.deleted = false;
        this.showFirstNameAndLastName = false;
        this.showEmail = false;
        this.showPhoneNumber = false;
        this.showAddress = false;
    }

    public enum Language {
        POLISH, ENGLISH, GERMAN
    }

}
