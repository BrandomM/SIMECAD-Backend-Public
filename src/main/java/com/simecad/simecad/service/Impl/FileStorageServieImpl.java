package com.simecad.simecad.service.Impl;

import com.simecad.simecad.domain.Producto;
import com.simecad.simecad.domain.Usuario;
import com.simecad.simecad.service.FileStorageService;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Qualifier("local")
public class FileStorageServieImpl implements FileStorageService {

    static final String PRODUCTS_ABSOLUTE_STORAGE = "C:/xampp/htdocs/Almacenamiento/productos/";
    static final String PRODUCTS_LOCALHOST_STORAGE = "http://localhost/Almacenamiento/productos/";
    
    static final String USERS_ABSOLUTE_STORAGE = "C:/xampp/htdocs/Almacenamiento/usuarios/";
    static final String USERS_LOCALHOST_STORAGE = "http://localhost/Almacenamiento/usuarios/";

    @Override
    public String almacenarFotoProducto(Producto producto, MultipartFile foto){

        try {
            long id = producto.getId();
            String nombreFoto = String.valueOf(id) + foto.getOriginalFilename();
            foto.transferTo(new File(PRODUCTS_ABSOLUTE_STORAGE + nombreFoto));
            
            return PRODUCTS_LOCALHOST_STORAGE + nombreFoto;
        } catch (IOException | IllegalStateException e) {
            Logger.getLogger(FileStorageServieImpl.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    @Override
    public String almacenarFotoUsuario(Usuario usuario, MultipartFile foto){
        try {
            long id = usuario.getId();
            String nombreFoto = String.valueOf(id) + foto.getOriginalFilename();
            foto.transferTo(new File(USERS_ABSOLUTE_STORAGE + nombreFoto));
            
            return USERS_LOCALHOST_STORAGE + nombreFoto;
        } catch (IOException | IllegalStateException e) {
            Logger.getLogger(FileStorageServieImpl.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

}
