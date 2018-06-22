package rniesler.gphotoshare.domain.googleapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareInfo {
    private String shareableUrl;
    private String shareToken;
}
