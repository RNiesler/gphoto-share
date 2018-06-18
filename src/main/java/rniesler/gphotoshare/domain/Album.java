package rniesler.gphotoshare.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
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

    //TODO refactor to persist only ShareInfo and keep Album in some cache for max 60minut (baseUrls retention time by Google)

    @Length(min = 1, max = 500) // 500 is the restriction of the Google Photos API
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
