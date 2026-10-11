package com.reservas.ms_reservas.service;

import com.reservas.commons.entity.Student;
import com.reservas.commons.exception.InvalidOperationException;
import com.reservas.ms_reservas.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class StudentService extends BaseService<Student> {

    public StudentService(StudentRepository repository) {
        super(repository, "Student");
    }

    @Transactional(readOnly = true)
    public Student getReservableStudent(UUID id) {
        Student student = findOrThrow(id);
        if (!student.canReserve()) {
            log.warn("Student {} is inactive and cannot reserve", id);
            throw new InvalidOperationException("The student is not allowed to make reservations");
        }
        return student;
    }
}
