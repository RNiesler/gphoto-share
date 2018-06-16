package rniesler.gphotoshare.domain.googleapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import rniesler.gphotoshare.domain.Circle;

import java.util.List;

@Data
@NoArgsConstructor
public class GoogleCirclesResponse {
    @JsonProperty("items")
    private List<Circle> circleList;
}
