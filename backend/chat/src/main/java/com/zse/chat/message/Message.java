package com.zse.chat.message;

import com.zse.chat.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "messages")
@Data
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(generator = "sequence-generator-message")
    @GenericGenerator(
            name = "sequence-generator-message",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "message_sequence"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    private final int id;

    @ManyToOne
    @JoinColumn(name = "nickname")
    private User author;
    private final String content;
    private final LocalDateTime createdAt;

    protected Message(){
        this.id = 0;
        this.content = "";
        this.createdAt = null;
    }
}
