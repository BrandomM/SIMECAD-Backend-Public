package com.simecad.simecad.controller;

import com.simecad.simecad.domain.Usuario;
import com.simecad.simecad.dto.CreateUserPhotoRequestDTO;
import com.simecad.simecad.dto.UpdatePasswordRequestDTO;
import com.simecad.simecad.dto.UpdateUserPhotoRequestDTO;
import com.simecad.simecad.dto.UsuarioDTO;
import com.simecad.simecad.dto.UsuarioImportDTO;
import com.simecad.simecad.service.FileStorageService;
import com.simecad.simecad.service.UsuarioService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/usuario")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    @Qualifier("aws")
    FileStorageService fileStorageService;

    @GetMapping("/listar")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Usuario> buscarUsuarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorId(id));
    }

    @PostMapping("/registrar")
    public ResponseEntity registrarUsuario(@RequestBody Usuario usuario) {

        boolean disponible = usuarioService.validarCorreo(usuario.getCorreo());
        if (!disponible) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está en uso");
        }

        usuario.setImagen("");
        usuario.setContrasena(usuario.getCorreo());
        usuarioService.registrarUsuario(usuario);

        return ResponseEntity.ok("Usuario creado existosamente");
    }

    @PutMapping("/modificar")
    public ResponseEntity modificarUsuario(@RequestBody Usuario usuario) {
        usuarioService.modificarUsuario(usuario);
        return ResponseEntity.ok("Usuario actualizado exitosamente");
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity eliminarUsuario(@RequestBody Usuario usuario) {
        try {
            usuarioService.eliminarUsuario(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se pudo eliminar el usuario");
        }
        return ResponseEntity.ok("Usuario eliminado con éxito");
    }

    @PostMapping("/importar")
    public ResponseEntity importarUsuarios(@RequestBody List<UsuarioImportDTO> listaUsuariosDTO) {
        try {
            listaUsuariosDTO.stream().map(usuarioImportDTO -> {
                Usuario usuario = new Usuario();
                usuario.setNombre(usuarioImportDTO.getNombre());
                usuario.setCelular(usuarioImportDTO.getCelular());
                usuario.setCorreo(usuarioImportDTO.getCorreo());
                usuario.setRol(usuarioImportDTO.getRol());
                usuario.setContrasena(usuarioImportDTO.getCorreo());
                return usuario;
            }).map(usuario -> {
                usuario.setImagen("");
                return usuario;
            }).forEachOrdered(usuario -> {

                if (usuarioService.validarCorreo(usuario.getCorreo())) {

                    if (usuario.getCorreo() != null && usuario.getNombre() != null) {
                        usuarioService.registrarUsuario(usuario);
                    }
                }
            });
            return ResponseEntity.ok("Usuarios registrados con éxito");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Documento mal formateado");
        }
    }

    @PostMapping("/registrarConFoto")
    public ResponseEntity registrarConFoto(@ModelAttribute CreateUserPhotoRequestDTO createUserPhotoRequestDTO) {

        boolean disponible = usuarioService.validarCorreo(createUserPhotoRequestDTO.getCorreo());

        if (!disponible) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está en uso");
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(createUserPhotoRequestDTO.getNombre());
        usuario.setCelular(createUserPhotoRequestDTO.getCelular());
        usuario.setCorreo(createUserPhotoRequestDTO.getCorreo());
        usuario.setContrasena(createUserPhotoRequestDTO.getCorreo());
        usuario.setRol(createUserPhotoRequestDTO.getRol());

        usuario = usuarioService.registrarUsuario(usuario);

        long id = usuario.getId();

        String imagen = fileStorageService.almacenarFotoUsuario(usuario, createUserPhotoRequestDTO.getFoto());
        usuario.setImagen(imagen);

        usuarioService.modificarUsuario(usuario);

        return ResponseEntity.ok("Recibido");
    }

    @PutMapping("/modificarConFoto")
    public ResponseEntity modificarConFoto(@ModelAttribute UpdateUserPhotoRequestDTO updateUserPhotoRequestDTO) {

        Usuario usuario = new Usuario();

        usuario.setNombre(updateUserPhotoRequestDTO.getNombre());
        usuario.setCelular(updateUserPhotoRequestDTO.getCelular());
        usuario.setCorreo(updateUserPhotoRequestDTO.getCorreo());
        usuario.setContrasena(updateUserPhotoRequestDTO.getContrasena());
        usuario.setRol(updateUserPhotoRequestDTO.getRol());
        usuario.setId(updateUserPhotoRequestDTO.getId());

        String imagen = fileStorageService.almacenarFotoUsuario(usuario, updateUserPhotoRequestDTO.getFoto());
        usuario.setImagen(imagen);

        usuarioService.modificarUsuario(usuario);

        return ResponseEntity.ok("Recibido");
    }

    @PutMapping("/actualizarContrasena")
    public ResponseEntity actualizarContrasena(@RequestBody UpdatePasswordRequestDTO updatePasswordRequestDTO) {
        long in = updatePasswordRequestDTO.getId();
        String contrasena = updatePasswordRequestDTO.getContrasena();
        String nuevaContrasena = updatePasswordRequestDTO.getNuevaContrasena();
        Usuario usuario = usuarioService.actualizarContrasena(in, contrasena, nuevaContrasena);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Algo salió mal");
        }
        return ResponseEntity.ok("Contraseña actualizada con éxito");
    }

    @PutMapping("/actualizarPerfil")
    public ResponseEntity actualizarPerfil(@RequestBody Usuario usuario) {
        Usuario usuarioActualizacion = usuarioService.buscarUsuarioPorId(usuario.getId());
        if (usuarioActualizacion == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Algo salió mal");
        }
        usuarioActualizacion.setNombre(usuario.getNombre());
        usuarioActualizacion.setCelular(usuario.getCelular());
        usuarioActualizacion = usuarioService.modificarUsuario(usuarioActualizacion);

        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(usuarioActualizacion.getId());
        usuarioDTO.setNombre(usuarioActualizacion.getNombre());
        usuarioDTO.setRol(usuarioActualizacion.getRol());
        usuarioDTO.setImagen(usuarioActualizacion.getImagen());

        return ResponseEntity.ok(usuarioDTO);
    }

    @PutMapping("/actualizarPerfilConFoto")
    public ResponseEntity actualizarPerfilConFoto(@ModelAttribute UpdateUserPhotoRequestDTO updateUserPhotoRequestDTO) {
        Usuario usuarioActualizacion = usuarioService.buscarUsuarioPorId(updateUserPhotoRequestDTO.getId());
        if (usuarioActualizacion == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Algo salió mal");
        }

        usuarioActualizacion.setNombre(updateUserPhotoRequestDTO.getNombre());
        usuarioActualizacion.setCelular(updateUserPhotoRequestDTO.getCelular());

        String imagen = fileStorageService.almacenarFotoUsuario(usuarioActualizacion, updateUserPhotoRequestDTO.getFoto());
        usuarioActualizacion.setImagen(imagen);

        usuarioService.modificarUsuario(usuarioActualizacion);
        
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(usuarioActualizacion.getId());
        usuarioDTO.setNombre(usuarioActualizacion.getNombre());
        usuarioDTO.setRol(usuarioActualizacion.getRol());
        usuarioDTO.setImagen(usuarioActualizacion.getImagen());

        return ResponseEntity.ok(usuarioDTO);
    }

}
