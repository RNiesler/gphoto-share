package rniesler.gphotoshare.domain.googleapi;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinCommand {
    private String shareToken;
}
