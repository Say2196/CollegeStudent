package com.example.demo.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@Entity
@Table(name="Student")
@Data
@NoArgsConstructor
public class StudentDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int stu_id;
    @NonNull
    @Column(name = "firstName")
    private String firstName;
    @NonNull
    @Column(name = "lastName")
    private String lastName;
    @NonNull
    @Column(name = "std")
    private String std;
    @NonNull
    @Column(name = "divison")
    private String divison;
    @NonNull
    @Column(name = "roll")
    private int roll;
    @NonNull
    @Column(name = "atd_per")
    private double atd_per;
    @Column(name = "marks")
    private double marks;
    @Column(name = "lst_grd")
    private String lst_grd;
    @Transient
    @NonNull
    private SubjectsDto subjects;
    @NonNull
    @Column(name = "gurd_name")
    private String gurd_name;
    @NonNull
    @Column(name = "phon")
    private String phon;
    @Column(name = "address")
    private String address;

}
