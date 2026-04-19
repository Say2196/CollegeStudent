package com.example.demo.repo;

import com.example.demo.dto.StudentDTO;
import com.example.demo.dto.SubjectsDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
public interface StudentRepo extends JpaRepository<StudentDTO,Integer> {

    @Query(value = "SELECT * FROM Student s WHERE TRIM(LOWER(s.firstName))= LOWER(:fname) AND TRIM(LOWER(lastName))=LOWER(:lname)", nativeQuery = true)
    public Optional<StudentDTO> getStudentByName(@Param("fname") String fname,@Param("lname") String lname);



    @Query(value = "SELECT CONCAT(firstName,' ',lastName) AS FullName,phon FROM Student WHERE atd_per < :atdPer", nativeQuery = true)
    public List<Map<String,String>> getAtdDefaulters(@Param("atdPer") double atd_per);


    @EntityGraph(attributePaths = {"firstName","lastName"})
    List<StudentDTO> findByMarksGreaterThan(float mark);


}
