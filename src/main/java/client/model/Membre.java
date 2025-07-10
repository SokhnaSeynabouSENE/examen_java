package client.model;

import javax.persistence.*;
import java.util.List;

@Table(name = "Membre", schema = "public")
public class Membre {
    private long id ;
    private boolean banned ;
    private List<Message> message ;
}