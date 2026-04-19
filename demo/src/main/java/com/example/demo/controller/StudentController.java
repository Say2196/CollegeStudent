package com.example.demo.controller;

import com.example.demo.Exceptions.AlreadyUnregisteredException;
import com.example.demo.Exceptions.ContactNumberInvalidException;
import com.example.demo.dto.StudentDTO;
import com.example.demo.dto.SubjectsDto;
import com.example.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("demo/student/register")
public class StudentController {

    @Autowired
    StudentService serv;

    @PostMapping("/registerStudent")
    public ResponseEntity<StudentDTO> registerStudent(@RequestBody StudentDTO student) throws ContactNumberInvalidException {
        StudentDTO savedRecord = serv.regStudent(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecord);
    }

    @PostMapping("/bulkStudentsRegister")
    public ResponseEntity<List<StudentDTO>> bulkRegistration(@RequestBody List<StudentDTO> students) throws ContactNumberInvalidException {
        List<StudentDTO> savedRecords = serv.bulkRegistration(students);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecords);
    }

    @GetMapping("/fetchAllStudent")
    public ResponseEntity<List<StudentDTO>> getAllStudentData(){
        List<StudentDTO> body = serv.getAllStudentData();
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }


    @GetMapping("/fetchById/{stu_id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable(value = "stu_id") Integer id){
        StudentDTO response = serv.getStudentById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/fetchByName")
    public ResponseEntity<StudentDTO> getStudentByName(@RequestParam(value = "fname")String fname,@RequestParam(value = "lname")String lname){
        StudentDTO student = serv.getStudentByName(fname,lname);
        return ResponseEntity.status(HttpStatus.OK).body(student);
    }


    @GetMapping("/getDefaulters/{atdPer}")
    public ResponseEntity<List<Map<String,String>>> getAtdDef(@PathVariable(value = "atdPer") Double atdPer){
        List<Map<String, String>> defStudents = serv.getAtdDefaulters(atdPer);
        return ResponseEntity.status(HttpStatus.OK).body(defStudents);
    }


    @PostMapping("/updateStudent")
    public ResponseEntity<StudentDTO> updateStudent(@RequestBody StudentDTO student) throws ContactNumberInvalidException {
        StudentDTO res = serv.updateStudent(student);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(res);
    }


    @PostMapping("/deleteStudent/{id}")
    public ResponseEntity<StudentDTO> deleteStudent(@PathVariable(value = "id") Integer id) throws AlreadyUnregisteredException {
        StudentDTO res = serv.unRegStudent(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(res);
    }

    @GetMapping("/subData")
    public ResponseEntity<List<SubjectsDto>> getSubjects(){
        List<SubjectsDto> subData = serv.getAllSubject();
        return ResponseEntity.status(HttpStatus.OK).body(subData);
    }

    @GetMapping("/byMark")
    public ResponseEntity<List<StudentDTO>> getStudentByStream(@RequestParam(value = "mark") float mark){
        List<StudentDTO> getStudentList = serv.fetchByMark(mark);
        return ResponseEntity.status(HttpStatus.OK).body(getStudentList);
    }

}
