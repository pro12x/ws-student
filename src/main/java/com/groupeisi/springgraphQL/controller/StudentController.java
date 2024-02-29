package com.groupeisi.springgraphQL.controller;

import com.groupeisi.springgraphQL.dao.StudentRepository;
import com.groupeisi.springgraphQL.entity.Student;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.GraphQL;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import graphql.ExecutionResult;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private StudentRepository studentRepository;

    @Value("classpath:student.graphqls")
    private Resource schemaResource;

    private GraphQL graphQL;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @PostConstruct
    public void loadSchema() throws IOException {
        File schemaFile = schemaResource.getFile();
        TypeDefinitionRegistry registry = new SchemaParser().parse(schemaFile);
        RuntimeWiring wiring = buildWiring();
        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry, wiring);
        graphQL = GraphQL.newGraphQL(schema).build();
    }

    private RuntimeWiring buildWiring() {
        DataFetcher<List<Student>> fetcher1 = data -> {
            return (List<Student>) studentRepository.findAll();
        };

        DataFetcher<Student> fetcher2 = data -> {
            return studentRepository.findByEmail(data.getArgument("email"));
        };

        return RuntimeWiring.newRuntimeWiring().type("Query",
                        typeWriting -> typeWriting.dataFetcher("getAllStudent", fetcher1).dataFetcher("findStudent", fetcher2))
                .build();

    }

    @PostMapping("/getAllStudent")
    public ResponseEntity<Object> getAllStudent(@RequestBody String query) {
        ExecutionResult result = graphQL.execute(query);
        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }

    @PostMapping("/getStudentByEmail")
    public ResponseEntity<Object> getStudentByEmail(@RequestBody String query) {
        ExecutionResult result = graphQL.execute(query);
        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }

    @GetMapping
    public List<Student> getAll(){
        return studentRepository.findAll();
    }
    @GetMapping("/{id}")
    public Student getById(@PathVariable("id") int id){
        return studentRepository.findById(id).orElseThrow();
    }
}
