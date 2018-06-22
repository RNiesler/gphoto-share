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
public class ShareAlbumCommand {
    private String albumId;
    private List<ObjectId> sharedTo;
    private String publicUrl;
    private String shareToken;
}
