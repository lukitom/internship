package com.zse.chat.user;

import java.util.TimeZone;

public class UserFixture {

    public static User.UserBuilder createDefaultUser(int number){
        return User.builder()
                .nickname("testNickname" + number)
                .firstName("testFirstName" + number)
                .lastName("testLastName" + number)
                .email("testEmail" + number)
                .phoneNumber("testPhoneNumber" + number)
                .country("testCountry" + number)
                .city("testCity" + number)
                .userLanguage(User.Language.POLISH)
                .timeZone(TimeZone.getTimeZone("Europe/Warsaw"))
                .userStatus(UserStatus.OFFLINE)
                .showFirstNameAndLastName(false)
                .showEmail(false)
                .showPhoneNumber(false)
                .showAddress(false)
                .deleted(false);
    }

}
