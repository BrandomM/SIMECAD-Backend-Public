package com.simecad.simecad.dto.report;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class ClienteReportDTO implements Serializable {

    @Id
    private Long id;
    private String nombre;
    private int total;
}
