package com.example.demo;

import com.example.demo.dao.BookRepository;
import com.example.demo.exceptions.BookRecordNotFoundException;
import com.example.demo.model.Book;
import com.example.demo.rest.BookController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import javax.persistence.EntityNotFoundException;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
//ensure we are using only Mockito to run this class and that the web server is not upk
public class BookControllerTest {

    private MockMvc mockMvc;  //helps perform an emulation of a "get" request
    ObjectMapper objectMapper = new ObjectMapper();  // in regular classes spring manages the mapping POJO-String-Json for us, but here we need to do it manually, that's why we need the mapper
    ObjectWriter objectWriter = objectMapper.writer();

    @Mock                          // we do not want to call the original everytime, so we mock it for tests
    private BookRepository bookRepository;

    @Mock
    private BookRecordNotFoundException bookRecordNotFoundException;

    @InjectMocks                   //the class that will be accepting the Mocks
    private BookController bookController;

    Book testRecord_1 = new Book(1, "Test driven development", "learn to write code with a test-first approach", 5);
    Book testRecord_2 = new Book(2, "Head first design patterns", "In depth view of software design patterns", 5);
    Book testRecord_3 = new Book(3, "Spring in action", "Guide to learn SpringBoot", 5);
    List<Book> bookRecordsList = new ArrayList<>(Arrays.asList(testRecord_1, testRecord_2, testRecord_3));


    @Before   //run this method before any test
    public void setUp() {
        MockitoAnnotations.openMocks(this);     //initializes Mockito inside our test class
//        MockitoAnnotations.initMocks(this);     //initializes Mockito inside our test class
        this.mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();  // ensures we are using Mockito and our MockMvc class, it will mock our book repository when we need to use it, and it will not open any tomcat kweb servers
    }

    @Test
    public void getAllRecords_success() throws Exception {

        Mockito.when(bookRepository.findAll()).thenReturn(bookRecordsList);

        mockMvc.perform(       //emulation of a get request to check the content being returned
                        //--------------------------------------- below we build a GET request using a mock
                        MockMvcRequestBuilders
                                .get("/book")      // the url we are targeting
                                .contentType(MediaType.APPLICATION_JSON))  // what we want the content type to be
                //-------------------------------------------------------------------------------
                // once we made the GET request we check if what we expect back is what we get
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(3)))   //$ checks the expression from the beginning
                .andExpect(jsonPath("$[2].name", is("Spring in action")))  // here we check if the name in record index 2 (the 3rd record), has a name that matches what we expect. Any value can be checked
                .andExpect(jsonPath("$[0].rating", is(5)));  // here we check if the rating in record index 0 (the 1st record), has a rating that matches what we expect. Any value can be checked
    }


    @Test
    public void getBookById_success() throws Exception {
        Mockito.when(bookRepository.findById(testRecord_1.getId())).thenReturn(Optional.of(testRecord_1));

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/book/1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("Test driven development")))
                .andExpect(jsonPath("$.rating", is(5)));
    }

    @Test
    public void getBookById_BookRecordNotFound() throws Exception {
        Random random = new Random();
        int nonExistentId = bookRecordsList.size() + random.nextInt();
        Mockito.lenient().when(bookRepository.findById(nonExistentId)).thenThrow(bookRecordNotFoundException);


//        bookRepository.findById(nonExistentId);
//        Mockito.verify(bookRepository).findById(nonExistentId).orElseThrow(() -> new BookRecordNotFoundException("Not found book record with id = " + nonExistentId));
    }

    @Test
    public void createBookRecord_success() throws Exception {

        Book testRecord = Book.builder()    //we added this builder on our entity class via @Builder annotation
                .id(4)
                .name("Introduction to Java")
                .description("Java core")
                .rating(5)
                .build();

        Mockito.when(bookRepository.save(testRecord)).thenReturn(testRecord);

        // we need to send the request body in for of a String.
        // In regular classes spring manages the mapping POJO-String-Json for us,
        // but here we need to do it manually, that's why we need the mapper
        String content = objectWriter.writeValueAsString(testRecord);
        MockHttpServletRequestBuilder mockHttpServletRequest = MockMvcRequestBuilders.post("/book")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(content);

        mockMvc.perform(mockHttpServletRequest)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("Introduction to Java")))
                .andExpect(jsonPath("$.rating", is(5)));
    }


    @Test
    public void updateBookRecord_success() throws Exception {

        Book updatedTestRecord = Book.builder()    //we added this builder on our entity class via @Builder annotation
                .id(1)
                .name("Updated name")
                .description("Updated description")
                .rating(4)
                .build();

        Mockito.when(bookRepository.findById(testRecord_1.getId())).thenReturn(Optional.ofNullable(testRecord_1));
        Mockito.when(bookRepository.save(updatedTestRecord)).thenReturn(updatedTestRecord);

        String updatedContent = objectWriter.writeValueAsString(updatedTestRecord);
        MockHttpServletRequestBuilder mockHttpServletRequest = MockMvcRequestBuilders.put("/book")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(updatedContent);

        mockMvc.perform(mockHttpServletRequest)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("Updated name")))
                .andExpect(jsonPath("$.rating", is(4)));
    }


    @Test
    public void deleteBookById_success() throws Exception {

//        Mockito.lenient().when(bookRepository.findById(testRecord_2.getId())).thenReturn(Optional.of(testRecord_2));

//rewrote to avoid using lenient() and get rid of unnecessary Stubbing
// (A stub is an object that always returns the same value, regardless of which parameters you provide on a stub’s methods.)

        bookRepository.deleteById(testRecord_2.getId());
        Mockito.verify(bookRepository).deleteById(testRecord_2.getId());

        mockMvc.perform(
                        MockMvcRequestBuilders
                        .delete("/book/2")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());
    }

}











