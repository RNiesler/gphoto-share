package rniesler.gphotoshare.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Album {
    private String id;
    @JsonProperty("title")
    private String name;
    @JsonProperty("productUrl")
    private String url;
    @JsonProperty("totalMediaItems")
    private Integer size;
    @JsonProperty("coverPhotoBaseUrl")
    private String coverPhotoUrl;
}
