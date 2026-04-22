package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "administrateurs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Administrateur extends Utilisateur {
    @OneToMany(mappedBy = "administrateur")
    private List<Annonce> annoncesAdmin;
}
