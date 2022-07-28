package com.zse.chat.user;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class UserFixture {

    public static User.UserBuilder createDefaultUser(int number) {
        return User.builder()
                .id(number)
                .nickname("testNickname" + number)
                .firstName("testFirstName" + number)
                .lastName("testLastName" + number)
                .email("testEmail" + number + "@mail.com")
                .phoneNumber("11122233" + number)
                .country("Testcountry")
                .city("Testcity")
                .userLanguage(User.Language.POLISH)
                .timeZone(TimeZone.getTimeZone("Europe/Warsaw"))
                .userStatus(User.UserStatus.OFFLINE)
                .showFirstNameAndLastName(false)
                .showEmail(false)
                .showPhoneNumber(false)
                .showAddress(false)
                .deleted(false);
    }

    public static List<User> createListOfDefaultUser(int amount) {
        return createListOfDefaultUser(1, amount);
    }

    public static List<User> createListOfDefaultUser(int min, int amount) {
        List<User> users = new ArrayList<>();

        for(int i = 0; i < amount; i++){
            users.add(createDefaultUser(i + min).build());
        }

        return users;
    }


}
