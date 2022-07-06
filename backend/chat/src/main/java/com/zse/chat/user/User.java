package com.zse.chat.user;

import lombok.*;

import javax.persistence.*;

@Table
@Entity(name = "chatUsers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private String nick;
    private Boolean deleted = false;

    @Override
    public String toString() {
        return "name: " + name + '\n' + "nick: " + nick;
    }
}
