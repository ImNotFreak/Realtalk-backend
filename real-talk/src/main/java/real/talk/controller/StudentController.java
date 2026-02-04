package real.talk.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import real.talk.model.dto.student.AddStudentRequest;
import real.talk.model.dto.student.StudentResponse;
import real.talk.model.entity.User;
import real.talk.service.user.StudentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<Void> addStudent(@AuthenticationPrincipal User user,
            @RequestBody AddStudentRequest request) {
        studentService.addStudent(user.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getStudents(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(studentService.getStudents(user.getUserId()));
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> removeStudent(@AuthenticationPrincipal User user,
            @PathVariable UUID studentId) {
        studentService.removeStudent(user.getUserId(), studentId);
        return ResponseEntity.noContent().build();
    }
}
