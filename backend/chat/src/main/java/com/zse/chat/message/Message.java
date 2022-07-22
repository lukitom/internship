package com.zse.chat.message;

import com.zse.chat.channel.Channel;
import com.zse.chat.user.User;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@RequiredArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
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
    int id;

    @ManyToOne
    @JoinColumn(name = "nickname", nullable = false)
    User author;
    String content;
    LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.EAGER)
    Channel channel;
    boolean deleted;

    protected Message(){
        this.id = 0;
        this.author = null;
        this.content = "";
        this.createdAt = null;
        this.channel = null;
        this.deleted = false;
    }
}
