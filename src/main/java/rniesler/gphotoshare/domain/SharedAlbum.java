package rniesler.gphotoshare.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedAlbum {
    @Id
    private String id;
    private String name;
    private String owner;
    private String clonedFrom;  // id of the album that it was cloned from
    private String publicUrl;
    private List<ObjectId> sharedTo; // ids of circles
    private String shareToken;
    private Binary coverPhoto;

}
