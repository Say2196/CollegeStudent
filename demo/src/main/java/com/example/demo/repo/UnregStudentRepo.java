package com.example.demo.repo;

import com.example.demo.dto.UnregStudentDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnregStudentRepo extends JpaRepository<UnregStudentDTO, Integer> {
}
