package com.simecad.simecad.service.Impl;

import com.simecad.simecad.dao.UsuarioDAO;
import com.simecad.simecad.domain.Usuario;
import com.simecad.simecad.service.UsuarioService;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    UsuarioDAO usuarioDAO;

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioDAO.findAll();
    }

    @Override
    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioDAO.buscarUsuarioPorId(id);
    }

    @Override
    public Usuario registrarUsuario(Usuario usuario) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        String hash = argon2.hash(1, 1024, 1, usuario.getContrasena());
        usuario.setContrasena(hash);
        return usuarioDAO.save(usuario);
    }

    @Override
    public Usuario modificarUsuario(Usuario usuario) {
        return usuarioDAO.save(usuario);
    }

    @Override
    public Usuario eliminarUsuario(Usuario usuario) {
        usuarioDAO.delete(usuario);
        return usuario;
    }

    @Override
    public Usuario validarCredenciales(String correo, String contrasena) {
        List<Usuario> usuarios = usuarioDAO.validarCorreo(correo);

        if (usuarios.isEmpty()) {
            return null;
        }

        String hash = usuarios.get(0).getContrasena();
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

        if (argon2.verify(hash, contrasena)) {
            return usuarios.get(0);
        }

        return null;
    }

    @Override
    public boolean validarCorreo(String correo) {
        return usuarioDAO.validarCorreo(correo).isEmpty();
    }

    @Override
    public Usuario actualizarContrasena(long id, String contrasena, String nuevaContrasena) {
        Usuario usuario = usuarioDAO.buscarUsuarioPorId(id);
        
        if (usuario == null) {
            return null;
        }

        String hash = usuario.getContrasena();
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        
        if (!argon2.verify(hash, contrasena)) {
            return null;
        }
        
        String newHash = argon2.hash(1, 1024, 1, nuevaContrasena);
        
        usuario.setContrasena(newHash);
        return usuarioDAO.save(usuario);
    }
}