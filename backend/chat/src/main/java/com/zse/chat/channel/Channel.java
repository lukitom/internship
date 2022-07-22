package com.zse.chat.channel;

import com.zse.chat.message.Message;
import com.zse.chat.user.User;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "channel")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Channel {

    @Id
    @GeneratedValue(generator = "sequence-generator-channel")
    @GenericGenerator(
            name = "sequence-generator-channel",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "channel_sequence"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    int id;
    @ManyToMany
    @JoinTable(
            name = "user_owner_channel",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @NonFinal List<User> owners;
    @ManyToMany
    @JoinTable(
            name = "user_member_channel",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @NonFinal List<User> members;

    @OneToMany
    @JoinColumn(name = "channel_id")
    List<Message> messages;

    protected Channel() {
        this.id = 0;
        this.owners = new ArrayList<>();
        this.members = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

}
