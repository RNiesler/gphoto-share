package rniesler.gphotoshare.domain.googleapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleAlbum implements Serializable {
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
    //TODO don't show the source when clone is present (or implement filters)
}
