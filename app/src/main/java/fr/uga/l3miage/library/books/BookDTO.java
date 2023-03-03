package fr.uga.l3miage.library.books;

import fr.uga.l3miage.library.authors.AuthorDTO;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Collection;

public record BookDTO(
        Long id,
        @NotBlank(message = "Title is mandatory")
        String title,
        @Min(1000000000)
        long isbn,
        String publisher,
        @Max(9999)
        @Min(-9999)
        short year,
        @Pattern(regexp = "english|french")
        String language,
        Collection<AuthorDTO> authors
) {
}
