package rniesler.gphotoshare.domain.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityMapping {
    @Id
    @NotBlank
    private String email;
    private Set<String> authorities;
}
