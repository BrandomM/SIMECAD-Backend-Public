
package com.simecad.simecad.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class UpdatePasswordRequestDTO implements Serializable {
    
    private long id;
    private String contrasena;
    private String nuevaContrasena;
    
}
