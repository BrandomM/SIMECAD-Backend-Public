package com.simecad.simecad.service;

import com.simecad.simecad.domain.Producto;
import com.simecad.simecad.domain.Usuario;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    public String almacenarFotoProducto(Producto producto, MultipartFile foto);

    public String almacenarFotoUsuario(Usuario usuario, MultipartFile foto);
}
