package rniesler.gphotoshare.domain.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestAccessCommand {
    @NotBlank
    private String name;
    @NotBlank
    private String email;
}
