package real.talk.model.entity.words;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import real.talk.model.entity.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "word_sets")
public class WordSet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "word_count", nullable = false)
    private Integer wordCount = 0;

    @ColumnDefault("false")
    @Column(name = "archived")
    private Boolean archived;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "wordSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WordSetWord> wordSetWords = new ArrayList<>();

    public List<Word> getWords() {
        return wordSetWords.stream()
                .map(WordSetWord::getWord)
                .toList();
    }

    @ManyToMany
    @JoinTable(name = "word_set_shared_users", joinColumns = @JoinColumn(name = "word_set_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private java.util.Set<User> sharedWith = new java.util.HashSet<>();

    public void updateWordCount() {
        this.wordCount = wordSetWords.size();
    }
}