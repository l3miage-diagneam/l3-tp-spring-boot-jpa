package fr.uga.l3miage.library.service.mock;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class BookServiceMockImpl implements BookService {

    private final AuthorService authorService;

    @Autowired
    public BookServiceMockImpl(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Override
    public Book save(Long authorId, Book book) throws EntityNotFoundException {
        book.setId(MockData.getNextId(Book.class));
        doSave(book);

        Author author = bind(authorId, book);

        authorService.update(author);
        return book;
    }


    @Override
    public Book get(Long id) throws EntityNotFoundException {
        return Optional.ofNullable(MockData.books.get(id))
                .orElseThrow(() -> new EntityNotFoundException("Cannot find book with id: " + id));
    }

    @Override
    public Collection<Book> list() {
        return MockData.books.values().stream().toList();
    }

    @Override
    public Book update(Book book) throws EntityNotFoundException {
        get(book.getId());
        doSave(book);
        return MockData.books.get(book.getId());
    }

    public Book addAuthor(Long bookId, Long authorId) throws EntityNotFoundException {
        var book = get(bookId);
        bind(authorId, book);
        return book;
    }

    @Override
    public void delete(Long id) throws EntityNotFoundException {
        get(id);
        MockData.books.remove(id);
    }

    @Override
    public Collection<Book> findByTitle(String title) {
        return filterBooks(MockData.books.values(), title);
    }

    @Override
    public Collection<Book> getByAuthor(Long authorId) throws EntityNotFoundException {
        return AuthorServiceMockImpl.doGet(authorId).getBooks();
    }

    @Override
    public Collection<Book> findByAuthor(Long authorId, String title) throws EntityNotFoundException {
        AuthorServiceMockImpl.doGet(authorId); // Trigger EntityNotFoundException if no Author founded
        return filterBooks(MockData.authors.get(authorId).getBooks(), title);
    }

    private Author bind(Long authorId, Book book) throws EntityNotFoundException {
        Author author = authorService.get(authorId);
        author.addBook(book);
        book.addAuthor(author);
        return author;
    }

    private static void doSave(Book book) {

        MockData.books.put(book.getId(), book);
    }

    private static Collection<Book> filterBooks(Collection<Book> books, String title) {
        return books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .toList();
    }

}
