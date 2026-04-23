package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import com.school.elearning.model.enums.TypeModerateur;

@Entity @Table(name = "moderateurs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Moderateur extends Utilisateur {
	
	@Enumerated (EnumType.STRING)
	private TypeModerateur type;

    @OneToMany ( mappedBy = "moderateur" )
    private List <Communaute> communautes;
    
}
