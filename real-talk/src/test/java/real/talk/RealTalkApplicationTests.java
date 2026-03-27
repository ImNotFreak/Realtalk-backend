package real.talk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import real.talk.model.dto.lesson.PresetClaudia;

import java.io.IOException;
import java.nio.file.Files;

@SpringBootTest
class RealTalkApplicationTests {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void contextLoads() throws IOException {
        String lessonJson = Files.readString(
                new ClassPathResource("prompts/lesson.json").getFile().toPath()
        );

        PresetClaudia lessonGeneratedByLlm = objectMapper.readValue(lessonJson, PresetClaudia.class);

        System.out.println(lessonGeneratedByLlm);
    }

}
