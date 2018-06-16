package rniesler.gphotoshare.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareInfo {
    private String shareableUrl;
    private String shareToken;
    private List<ObjectId> sharedTo;
}
