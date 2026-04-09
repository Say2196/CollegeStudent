package com.example.demo.service;

import com.example.demo.Exceptions.AlreadyUnregisteredException;
import com.example.demo.Exceptions.ContactNumberInvalidException;
import com.example.demo.Exceptions.NoSuchStudentFound;
import com.example.demo.dto.StudentDTO;
import com.example.demo.dto.SubjectsDto;
import com.example.demo.dto.UnregStudentDTO;
import com.example.demo.repo.StudentRepo;
import com.example.demo.repo.UnregStudentRepo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.hibernate.ReadOnlyMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponse;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private static final Logger LOGGER = Logger.getLogger(StudentService.class.getName());

    @Autowired
    StudentRepo repo;
    @Autowired
    UnregStudentRepo unregRepo;
    @Autowired
    RestTemplate rt;


    private static final String MOB_REGEX = "^\\d{10}$";
    @Value("${app.subject.url}")
    private String SUB_URL;


    @Transactional(rollbackFor = Exception.class)
    public StudentDTO regStudent(StudentDTO student) throws ContactNumberInvalidException {

        if (student.getPhon().matches(MOB_REGEX)) {
            LOGGER.info("Student data is valid and registering into database");
            StudentDTO savedStudent = repo.save(student);
            if(student.getSubjects()!=null){

                student.getSubjects().setStu_id(student.getStu_id());

                HttpHeaders header = new HttpHeaders();
                header.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<SubjectsDto> request = new HttpEntity<>(student.getSubjects(),header);

               ResponseEntity<SubjectsDto> subjResp = rt.postForEntity(SUB_URL.concat("/registerSub"),request,SubjectsDto.class);
               LOGGER.info("Subject Data saved for "+student.getStu_id()+" with subject id: "+subjResp.getBody().getId());
               savedStudent.getSubjects().setId(subjResp.getBody().getId());
            }
            return savedStudent;
        } else {
            LOGGER.warning("Phone number is not valid, throwing exception");
            throw new ContactNumberInvalidException("Phone Number of the student is not valid");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public List<StudentDTO> bulkRegistration(List<StudentDTO> listOfStudents) throws ContactNumberInvalidException {

        boolean flag = listOfStudents.stream().allMatch(s -> s.getPhon().matches(MOB_REGEX));

        if (flag) {
            LOGGER.info("All the students data are valid & registering to database");
            List<StudentDTO> listOfSavedRecords = repo.saveAllAndFlush(listOfStudents);

            listOfSavedRecords.parallelStream().forEach(stu->stu.getSubjects().setStu_id(stu.getStu_id()));

            List<SubjectsDto> listOfSubjects = listOfSavedRecords.parallelStream().map(StudentDTO::getSubjects).collect(Collectors.toList());

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<SubjectsDto>> request = new HttpEntity<>(listOfSubjects,header);


            ResponseEntity<List<SubjectsDto>> response = rt.exchange(SUB_URL.concat("/bulkSubReg"),HttpMethod.POST,request,new ParameterizedTypeReference<List<SubjectsDto>>(){});

            Map<Integer,Integer> subjectIdMapper = response.getBody().stream().collect(Collectors.toMap(SubjectsDto::getStu_id,SubjectsDto::getId));

            listOfSavedRecords.parallelStream().forEach(stu -> stu.getSubjects().setId(subjectIdMapper.get(stu.getStu_id())));

            return listOfSavedRecords;
        } else {
            LOGGER.warning("Invalid contact number/s");
            throw new ContactNumberInvalidException("Please verify given contact number format of bulk student data, there is some invalid contact number");
        }
    }


    @Transactional(rollbackFor = Exception.class, readOnly = true)
    @Retryable(retryFor = {ConnectException.class}, maxAttempts = 5, backoff = @Backoff(delay = 1300,multiplier = 2))
    public List<StudentDTO> getAllStudentData() {
        LOGGER.info("Generating all the student data from database");
        List<StudentDTO> listOfAllStudents = repo.findAll().stream().sorted(Comparator.comparing(StudentDTO::getRoll).thenComparing(StudentDTO::getFirstName)).collect(Collectors.toList());

       Map<Integer, SubjectsDto> subMap = getAllSubject().stream().collect(Collectors.toMap(SubjectsDto::getStu_id, Function.identity()));

       listOfAllStudents.stream().forEach(stu -> {
                   SubjectsDto sub = subMap.get(stu.getStu_id());

                   if(sub!=null){
                       stu.setSubjects(sub);
                   }
               }
               );

        return listOfAllStudents;
    }

    @Recover
    public List<StudentDTO> fallBackForGetAllStudent(Exception ex){
        LOGGER.info("Fallback triggered for get all student data due to the error: "+ex.getMessage());
        List<StudentDTO> listOfFallBackStudent = repo.findAll().stream().sorted(Comparator.comparing(StudentDTO::getFirstName).thenComparing(StudentDTO::getRoll)).collect(Collectors.toList());
        return listOfFallBackStudent;
    }


    @EntityGraph(attributePaths = {"subjects"})
    public StudentDTO getStudentById(Integer id) {

        Optional<StudentDTO> student = repo.findById(id);
        Optional<SubjectsDto> subject = Optional.ofNullable(getSubjectByStuId(id));


        if (student.isPresent() && subject.isPresent()) {
            LOGGER.info("Student and subject data found with id " + id);
            student.get().setSubjects(subject.get());
            return student.get();
        } else {
            LOGGER.warning("Student and subject data not found with id " + id);
            throw new NoSuchStudentFound("Student and subject data not found with id " + id);
        }

    }


    @Retryable(retryFor = {Exception.class}, maxAttempts = 5,backoff = @Backoff(delay = 1500,multiplier = 2))
    public StudentDTO getStudentByName(String fname, String lname) {
        Optional<StudentDTO> student = repo.getStudentByName(fname, lname);

        if (student.isPresent()) {
            Optional<SubjectsDto> subject = Optional.ofNullable(getSubjectByStuId(student.get().getStu_id()));
            if(subject.isPresent()) {
                LOGGER.info("Data found for " + fname + " " + lname);
                student.get().setSubjects(subject.get());
            }else {
                LOGGER.info("No subject associated with this student");
            }
            return student.get();
        } else {
            LOGGER.warning("No data found for " + fname + " " + lname);
            throw new NoSuchStudentFound("No data found for " + fname + " " + lname);
        }
    }

    @Recover
    public StudentDTO fallBackSubject(Exception ex,String fname, String lname){
        LOGGER.warning("Fallback triggered due to "+ex.getMessage());
        Optional<StudentDTO> getStudentWithOutSubject = repo.getStudentByName(fname,lname);
        if(getStudentWithOutSubject.isPresent()){
            return getStudentWithOutSubject.get();
        }
        else{
            LOGGER.warning("No Student Found with name "+fname+" "+lname);
            throw new NoSuchStudentFound("No Student Found with name "+fname+" "+lname);
        }
    }



    public List<Map<String, String>> getAtdDefaulters(Double atd_per) {

        if (atd_per == null) {
            atd_per = 75.00;
        }

        List<Map<String, String>> def_students = repo.getAtdDefaulters(atd_per);

        if (!(def_students.size() == 0 || def_students.isEmpty())) {

            LOGGER.warning("There are some attendence defaulters found please take action to align them perfectly");
            return def_students;
        } else {
            LOGGER.info("Best of luck faculty, none of your student found as defaulters. Thank you!");
            throw new NoSuchStudentFound("Best of luck faculty, none of your student found as defaulters. Thank you!");
        }

    }


    @Transactional(rollbackFor = Exception.class)
    public StudentDTO updateStudent(StudentDTO student) throws ContactNumberInvalidException {
        Optional<StudentDTO> existing = repo.findById(student.getStu_id());

        if (existing.isPresent()) {
            if (student.getPhon().matches(MOB_REGEX)) {
                if (!student.equals(existing)) {
                    LOGGER.info("Update found for the student");
                    StudentDTO res = repo.save(student);
                    SubjectsDto subject = student.getSubjects();

                    HttpHeaders header = new HttpHeaders();
                    header.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<SubjectsDto> request = new HttpEntity<>(subject,header);

                    ResponseEntity<SubjectsDto> updateSubject = rt.exchange(SUB_URL.concat("/updateSubject"),HttpMethod.PUT,request,new ParameterizedTypeReference<SubjectsDto>(){});
                    if(updateSubject.getStatusCode()==HttpStatusCode.valueOf(202)){
                        LOGGER.info("Subject data also updated");
                    }
                    res.setSubjects(updateSubject.getBody());
                    return res;
                } else {
                    LOGGER.info("No update found for the student");
                    return existing.get();
                }
            } else {
                LOGGER.warning("Phone number is not valid, throwing exception");
                throw new ContactNumberInvalidException("Phone Number of the student is not valid");
            }
        } else {
            LOGGER.warning("No Student found with id: " + student.getStu_id());
            throw new NoSuchStudentFound("No Student found with id: " + student.getStu_id());
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public StudentDTO unRegStudent(int id) throws AlreadyUnregisteredException {
        Optional<StudentDTO> existing = repo.findById(id);

        if(existing.isPresent()) {
            StudentDTO student = existing.get();
            UnregStudentDTO unRegStudent = new UnregStudentDTO();

            if (!unregRepo.existsById(id)) {
                unRegStudent.setId(student.getStu_id());
                unRegStudent.setFirstName(student.getFirstName());
                unRegStudent.setLastName(student.getLastName());
                unRegStudent.setStd(student.getStd());
                unRegStudent.setDivision(student.getDivison());
                unRegStudent.setRoll(student.getRoll());
                unRegStudent.setPhon(student.getPhon());
                unRegStudent.setAddress(student.getAddress());
                unRegStudent.setYearOfUnreg(String.valueOf(LocalDate.now().getYear()));
                unRegStudent.setIs_active(false);

                unregRepo.save(unRegStudent);
                repo.delete(student);
            }
            else{
                LOGGER.info("Student with id "+id+" is already unregistered");
                throw new AlreadyUnregisteredException("Student with id "+id+" is already unregistered");
            }

            return student;
        }
        else{
            LOGGER.warning("No student present with id: "+id);
            throw new NoSuchStudentFound("No student present with id: "+id);
        }
    }


    @CircuitBreaker(name = "subjectDownBreaker", fallbackMethod = "hanldeSubjectDown")
    public SubjectsDto getSubjectByStuId(Integer stu_id){
        ResponseEntity<SubjectsDto> subRes = rt.exchange(SUB_URL.concat("/student/{id}"), HttpMethod.GET, null, SubjectsDto.class, stu_id);
        LOGGER.info("Subject found for student "+stu_id);
        return subRes.getBody();
    }


    public ErrorResponse hanldeSubjectDown(){
        return ErrorResponse.create(new Throwable("Something went wrong"),HttpStatus.EXPECTATION_FAILED,"Subject Service not responding");
    }



    public List<SubjectsDto> getAllSubject(){
        LOGGER.info("Fetching Subject details from Demo2 application..");

        ResponseEntity<List<SubjectsDto>> res = rt.exchange(SUB_URL.trim().concat("/allSubData".trim()), HttpMethod.GET, null, new ParameterizedTypeReference<List<SubjectsDto>>(){});

        return res.getBody();

    }



}
