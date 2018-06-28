package rniesler.gphotoshare.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import rniesler.gphotoshare.domain.notifications.WebPushSubscription;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
@Builder
public class Person {
    @Id
    private ObjectId id;
    @NotBlank
    private String email;
    private String name;
    private Set<WebPushSubscription> subscriptions;
}
