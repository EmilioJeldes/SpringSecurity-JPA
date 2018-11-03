package cl.ejeldes.springsecuritymvc.service;

import cl.ejeldes.springsecuritymvc.entities.Usuario;
import cl.ejeldes.springsecuritymvc.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service("jpaUserDetailsService")
public class JpaUserDetailsService implements UserDetailsService {

    private UsuarioRepository usuarioRepository;
    private Logger logger = LoggerFactory.getLogger(JpaUserDetailsService.class);

    @Autowired
    public JpaUserDetailsService(UsuarioRepository usuarioRepository) {this.usuarioRepository = usuarioRepository;}

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("No existe un usuario con username: '" + username + "'"));

        logger.info(usuario.toString());
        logger.info(usuario.toString());

        Set<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toSet());

        if (authorities.isEmpty()) {
            logger.error("Error login: usuario: '" + username + "'");
            throw new UsernameNotFoundException("Username: '" + username + "' no existe en el sistema");
        }

        return new User(usuario.getUsername(), usuario.getPassword(), authorities);
    }
}
