package real.talk.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "white_list")
public class WhiteList {

    @Id
    @Email
    @Column(name = "email", unique = true)
    private String email;
}
