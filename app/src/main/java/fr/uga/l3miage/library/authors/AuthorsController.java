package fr.uga.l3miage.library.authors;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.books.BookDTO;
import fr.uga.l3miage.library.books.BooksMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.DeleteAuthorException;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class AuthorsController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;
    private final BooksMapper booksMapper;

    @Autowired
    public AuthorsController(AuthorService authorService, AuthorMapper authorMapper, BooksMapper booksMapper) {
        this.authorService = authorService;
        this.authorMapper = authorMapper;
        this.booksMapper = booksMapper;
    }

    @GetMapping("/authors")
    public Collection<AuthorDTO> authors(@RequestParam(value = "q", required = false) String query) {
        Collection<Author> authors;
        if (query == null) {
            authors = authorService.list();
        } else {
            authors = authorService.searchByName(query);
        }
        return authors.stream()
                .map(authorMapper::entityToDTO)
                .toList();
    }
    @GetMapping("/authors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AuthorDTO author(@PathVariable("id") Long id) throws EntityNotFoundException {
        Author author;
        try {
            author = authorService.get(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return authorMapper.entityToDTO(author);
    }
    @PostMapping("/authors")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDTO newAuthor(@Valid @RequestBody AuthorDTO author) {

        /** Transform AuthorDTO(author) to Author entity*/
        Author au = authorMapper.dtoToEntity(author);
        Author aut = authorService.save(au);
        /** Transform author(aut) entity to AuthorDTO and return */
        return authorMapper.entityToDTO(aut);
    }
    @PutMapping("/authors/{id}")
    public AuthorDTO updateAuthor(@RequestBody AuthorDTO author, @PathVariable("id") Long id) throws EntityNotFoundException {

        /** ensure author ID equal to id parameter */
        if(author.id() != id){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        /** Verify if object exist in my dictionary*/
        Author au = this.authorService.get(id);
        if(au == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        /** Call service for updating author */
        Author edited = this.authorService.update(authorMapper.dtoToEntity(author));

        /** Transform author(aut) entity to AuthorDTO and return */
        return this.authorMapper.entityToDTO(edited);
    }
    @DeleteMapping("/authors/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable("id") Long id) throws EntityNotFoundException {

        /** Verify if object exist in my dictionary*/
        Author author = this.authorService.get(id);
        if(author == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        /** Call the delete method to delete author*/
        try {
            this.authorService.delete(id);
        } catch (DeleteAuthorException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("authors/{id}/books")
    public Collection<BookDTO> books(@PathVariable("id") Long authorId) throws EntityNotFoundException {
        Set<Book> books = this.authorService.get(authorId).getBooks();
        if (books != null) {
            return books.stream()
                    .map(booksMapper::entityToDTO)
                    .collect(Collectors.toSet());
        }

        return Collections.emptyList();
    }
}
