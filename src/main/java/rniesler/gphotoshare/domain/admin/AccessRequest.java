package rniesler.gphotoshare.domain.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessRequest {
    @Id
    private String email;
    private String name;
    private boolean denied;
}
