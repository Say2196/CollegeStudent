package com.example.demo.dto;

import jakarta.persistence.*;

import java.util.Objects;


@Entity
    @Table(name = "Subjects")
    public class SubjectsDto {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Integer id;
        @Column(name = "stu_id")
        private Integer stu_id;
        @Column(name = "subComb_code")
        private String subjectCode;
        @Column(name = "stream")
        private String Stream;


        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getStu_id() {
            return stu_id;
        }

        public void setStu_id(Integer stu_id) {
            this.stu_id = stu_id;
        }

        public String getSubjectCode() {
            return subjectCode;
        }

        public void setSubjectCode(String subjectCode) {
            this.subjectCode = subjectCode;
        }

        public String getStream() {
            return Stream;
        }

        public void setStream(String stream) {
            Stream = stream;
        }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SubjectsDto that = (SubjectsDto) o;
        return Objects.equals(id, that.id) && Objects.equals(stu_id, that.stu_id) && Objects.equals(subjectCode, that.subjectCode) && Objects.equals(Stream, that.Stream);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, stu_id, subjectCode, Stream);
    }
}


