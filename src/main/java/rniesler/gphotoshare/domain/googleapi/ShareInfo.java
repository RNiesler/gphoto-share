package rniesler.gphotoshare.domain.googleapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareInfo implements Serializable {
    private String shareableUrl;
    private String shareToken;
}
