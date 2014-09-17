package model;

import javax.persistence.*;

@Entity
@Table(name="MattsInfo")
public class MattInfoEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;
}
