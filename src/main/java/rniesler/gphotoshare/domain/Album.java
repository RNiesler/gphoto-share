package rniesler.gphotoshare.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Album {
    @Id
    private String id;
    @JsonProperty("title")
    private String name;
    @JsonProperty("productUrl")
    private String url;
    @JsonProperty("totalMediaItems")
    private Integer size;
    @JsonProperty("coverPhotoBaseUrl")
    private String coverPhotoUrl;

    private ShareInfo shareInfo;
    private Person owner;
}
