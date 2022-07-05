package com.zse.chat.user;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table
@Entity(name = "chatUsers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private Long id;

    private String name;

    private int age;

    @Override
    public String toString() {
        return "name: " + name + '\n' + "age: " + age;
    }
}
