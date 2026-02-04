package real.talk.repository.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import real.talk.model.entity.StudentTeacherLink;
import real.talk.model.entity.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentTeacherRepository extends JpaRepository<StudentTeacherLink, UUID> {
    List<StudentTeacherLink> findAllByTeacherUserId(UUID teacherId);

    boolean existsByTeacherAndStudent(User teacher, User student);

    void deleteByTeacherUserIdAndStudentUserId(UUID teacherId, UUID studentId);
}
