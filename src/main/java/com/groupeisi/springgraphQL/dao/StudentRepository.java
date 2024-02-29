package com.groupeisi.springgraphQL.dao;

import com.groupeisi.springgraphQL.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Student findByEmail(String email);
}
