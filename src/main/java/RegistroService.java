

import com.caninoshop.domain.Rol;
import com.caninoshop.domain.Usuario;
import com.caninoshop.repository.RolRepository;
import com.caninoshop.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroService {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void crearUsuario(Usuario datos) {
        if (usuarioRepository.existsByUsername(datos.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }
        if (usuarioRepository.existsByCorreo(datos.getCorreo())) {
            throw new IllegalArgumentException("El correo ya esta registrado.");
        }
        Rol rolCliente = rolRepository.findByRol("CLIENTE")
                .orElseThrow(() -> new IllegalStateException("Falta el rol CLIENTE en la base de datos."));
        // Nunca tomar los roles ni el estado desde el formulario publico.
        Usuario usuario = new Usuario();
        usuario.setUsername(datos.getUsername());
        usuario.setNombre(datos.getNombre());
        usuario.setApellidos(datos.getApellidos());
        usuario.setCorreo(datos.getCorreo());
        usuario.setTelefono(datos.getTelefono());
        usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        usuario.setActivo(true);
        usuario.getRoles().add(rolCliente);
        usuarioRepository.save(usuario);
    }
}
