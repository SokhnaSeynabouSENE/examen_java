package server.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Membre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String pseudo;

    private boolean banned = false;

    @OneToMany(mappedBy = "membre", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();
} 