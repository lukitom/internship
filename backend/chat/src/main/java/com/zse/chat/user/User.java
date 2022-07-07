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
    private final String name;
    @Column(unique = true)
    private final String nick;
    private final Boolean deleted = false;

    protected User(){
        this.id = 0;
        this.name = "";
        this.nick = "";
    }
}
