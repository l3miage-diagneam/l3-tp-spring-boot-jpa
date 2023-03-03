package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;

    private final AuthorService authorService;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper, AuthorService authorService) {
       this.bookService = bookService;
        this.booksMapper = booksMapper;
        this.authorService = authorService;
    }

    @GetMapping("/books")
    public Collection<BookDTO> books(@RequestParam(value = "q", defaultValue = "") String query) {
        Collection<Book> books;
        if (query == null) {
            books = this.bookService.list();
        } else {
            books = this.bookService.findByTitle(query);
        }
        return books.stream()
                .map(booksMapper::entityToDTO)
                .toList();
    }
    @GetMapping("/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO book(@PathVariable("id") Long id) throws EntityNotFoundException {

        Book book = bookService.get(id);
        return this.booksMapper.entityToDTO(book);
    }
    @PostMapping("/authors/{id}/books")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO newBook(@PathVariable("id") Long authorId, @Valid @RequestBody BookDTO book) {
        try {
            Book bo = bookService.save(authorId, this.booksMapper.dtoToEntity(book));
            return this.booksMapper.entityToDTO(bo);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
    @PutMapping("/books/{id}")
    public BookDTO updateBook(@PathVariable("id") Long idBook, @Valid @RequestBody BookDTO book) throws EntityNotFoundException {
        /** ensure author ID equal to id parameter */
        if(book.id() != idBook){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        /** Verify if object exist in my dictionary*/
        Book b = this.bookService.get(idBook);
        if(b == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        /** Call service for updating author */
        Book book_tmp = this.booksMapper.dtoToEntity(book);
        book_tmp.setAuthors(b.getAuthors());
        Book book_edited = this.bookService.update(book_tmp);

        /** Transform author(aut) entity to AuthorDTO and return */
        return this.booksMapper.entityToDTO(book_edited);
    }
    @DeleteMapping("/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable("id") Long id) throws EntityNotFoundException {
        /** Verify if object exist in my dictionary*/
        Book book = this.bookService.get(id);
        if(book == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        /** Call the delete method to delete author*/
        this.bookService.delete(id);
    }

    @PutMapping("books/{id}/authors")
    public BookDTO addAuthor(@PathVariable("id") Long bookID, @Valid @RequestBody AuthorDTO author) throws EntityNotFoundException {
        Book book = this.bookService.get(bookID);
        Book b = this.bookService.addAuthor(booksMapper.entityToDTO(book).id(), author.id());
        Book book_edited = this.bookService.update(b);
        return booksMapper.entityToDTO(book_edited);
    }
}
