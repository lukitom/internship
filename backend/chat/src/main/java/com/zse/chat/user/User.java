package com.zse.chat.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
    private final int id;
    @Column(unique = true)
    private final String nickname;
    private final String firstName;
    private final String lastName;
    @Column(unique = true, nullable = false)
    private final String email;
    private final String country;
    private final String city;
    private final Language userLanguage = Language.POLISH;
    private final ContentLanguage contentLanguage = ContentLanguage.MY_LANGUAGE;
    private final TimeZone timeZone = TimeZone.getTimeZone("Europe/Warsaw");


    private final Boolean deleted = false;
    private final Boolean showFirstNameAndLastName = false;
    private final Boolean showEmail = false;
    private final Boolean showAddress = false;

    protected User(){
        this.id = 0;
        this.nickname = "";
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.country = "";
        this.city = "";
    }
}
