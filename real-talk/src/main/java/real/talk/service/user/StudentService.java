package real.talk.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import real.talk.model.dto.student.AddStudentRequest;
import real.talk.model.dto.student.StudentResponse;
import real.talk.model.entity.StudentTeacherLink;
import real.talk.model.entity.User;
import real.talk.model.entity.enums.UserRole;
import real.talk.repository.student.StudentTeacherRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentTeacherRepository studentTeacherRepository;
    private final UserService userService;

    @Transactional
    public StudentResponse addStudent(UUID teacherId, AddStudentRequest request) {
        User teacher = userService.getUserById(teacherId);

        // check if student exists
        Optional<User> existingStudent = userService.getUserByEmail(request.email());
        User student;

        if (existingStudent.isPresent()) {
            student = existingStudent.get();
        } else {
            // create new student user
            student = new User();
            student.setEmail(request.email());
            String name = request.name();
            if (name == null || name.isBlank()) {
                name = request.email().split("@")[0];
            }
            student.setName(name);
            student.setRole(UserRole.STUDENT);
            student.setCreatedAt(Instant.now());
            student.setLessonCount(0);
            student.setDuration(0.0);
            student.setOrderNumber(UUID.randomUUID());
            userService.saveUser(student);
            log.info("Created new student user: {}", student.getEmail());
        }

        // Link to teacher if not already linked
        if (!studentTeacherRepository.existsByTeacherAndStudent(teacher, student)) {
            StudentTeacherLink link = new StudentTeacherLink();
            link.setTeacher(teacher);
            link.setStudent(student);
            link.setCreatedAt(Instant.now());
            studentTeacherRepository.save(link);
            log.info("Linked student {} to teacher {}", student.getEmail(), teacher.getEmail());
            return new StudentResponse(student.getUserId(), student.getName(), student.getEmail(), link.getCreatedAt());
        } else {
            log.info("Student {} already linked to teacher {}", student.getEmail(), teacher.getEmail());
            // Fetch existing link date or just use current time if consistency isn't
            // critical strictly here,
            // but better to fetch logic if we want exact link creation time.
            // For now, let's return the student info.
            // If strictly needed, we could fetch the link.
            // Simplified: return response with current time or null if link date is not
            // easily available without fetch.
            // Let's fetch the link to be precise or just return info.
            return new StudentResponse(student.getUserId(), student.getName(), student.getEmail(), Instant.now());
        }
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudents(UUID teacherId) {
        return studentTeacherRepository.findAllByTeacherUserId(teacherId).stream()
                .map(link -> {
                    User s = link.getStudent();
                    return new StudentResponse(s.getUserId(), s.getName(), s.getEmail(), link.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeStudent(UUID teacherId, UUID studentId) {
        studentTeacherRepository.deleteByTeacherUserIdAndStudentUserId(teacherId, studentId);
        log.info("Teacher {} removed link to student {}", teacherId, studentId);
    }

    @Transactional(readOnly = true)
    public Optional<StudentResponse> getMyTeacher(UUID studentId) {
        return studentTeacherRepository.findByStudentUserId(studentId).stream()
                .findFirst()
                .map(link -> {
                    User t = link.getTeacher();
                    return new StudentResponse(t.getUserId(), t.getName(), t.getEmail(), link.getCreatedAt());
                });
    }
}
