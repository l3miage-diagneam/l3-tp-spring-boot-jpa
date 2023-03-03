package fr.uga.l3miage.library.authors;

import jakarta.validation.constraints.NotBlank;

public record AuthorDTO(
        Long id,
        @NotBlank
        String fullName
) {
}
