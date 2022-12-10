package com.example.demo.rest;


import com.example.demo.dao.BookRepository;
import com.example.demo.exceptions.BookRecordNotFoundException;
import com.example.demo.model.Book;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book")
public class BookController {

    private BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public List<Book> getAllBookRecords() {
        return bookRepository.findAll();
    }

    @GetMapping("{id}")
    public Book getBookById(@PathVariable(value = "id") Integer id) {
        return bookRepository.findById(id).get();
    }

    @PostMapping
    public Book createBookRecord(@RequestBody @Valid Book bookRecord) {
        return bookRepository.save(bookRecord);
    }

    @PutMapping
    public Book updateBookRecord(@RequestBody @Valid Book bookRecord) {
        Book currentBook = bookRepository.findById(bookRecord.getId())
                .orElseThrow(() -> new BookRecordNotFoundException("Not found book record with id = " + bookRecord.getId()));
        currentBook.setName(bookRecord.getName());
        currentBook.setDescription(bookRecord.getDescription());
        currentBook.setRating(bookRecord.getRating());
        return bookRepository.save(currentBook);
    }

    @DeleteMapping(value = "{id}")
    public void deleteBookById(@PathVariable(value = "id") Integer id) throws BookRecordNotFoundException{

        try {
            bookRepository.deleteById(id);
        }
        catch(Exception e) {
            throw new BookRecordNotFoundException("Not found book with id = " + id);
        }

    }
}
