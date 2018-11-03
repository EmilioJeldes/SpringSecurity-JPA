# Spring Security JPA
Spring security demo using JPA authentication

## Implementing JPA Authentication
#### 1. Create User and Role Entities
````java
@Data
@Entity
@NoArgsConstructor
@Table(name = "authorities", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "authority"})})
public class Role implements Serializable {

    private static final long serialVersionUID = -4068171455734205268L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String authority;
}

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class Usuario implements Serializable {

    private static final long serialVersionUID = 6569940285923965188L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(length = 60)
    private String password;

    private Boolean enabled;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<Role> roles;

}
````

#### 2. Create User Repository
````java
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);
}
````

#### 3. Create UserDetailsService implementation
````java
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
````
* Implements `UserDetailsService` which contains only one specific method `loadUserByUsername()` . This is gonna be called by the DaoAuthenticationProvider to get the spring `User` to create the `Authentication`object.
* It uses the UserRepository to get the User and the Role (authorities)
* Creates a set of authorities
* It returns an instance of `UserDetails` a Spring `User`which contains the username, password, authorities, enabled, credentialsNonExpired, accountNonExpired, accountNonLocked all set to true.

#### 4. Register the `UserDetailsService` in the `SpringSecurityConfig` class using `AuthenticationManagerBuilder`
````java
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    
    ...
    
    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private final JpaUserDetailsService jpaUserDetailsService;

    ...

    ...
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jpaUserDetailsService).passwordEncoder(passwordEncoder);

    }
    
    ...
}
````
* Injects the `BCryptPasswordEncoder` and the `JpaUserDetailsService` (via constructor or @Autowired)
* Overrides the `configure()` method with the `AuthenticationManagerBuilder` and sets the custom `userDetailsService` and `passwordEncoder()`

#### 5. Set database properties
````yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jdbc_test?useSSL=false
    username: root
    password: mi-contraseña
    driver-class-name: com.mysql.jdbc.Driver
  jpa:
    database-platform: org.hibernate.dialect.MySQL5Dialect
    database: mysql
    hibernate:
      ddl-auto: create-drop
logging:
  level:
    org:
      hibernate:
        SQL: debug
````
---
## Testing

#### Credentials
user1 = username: user, password: 12345
user2 = username: admin, password: 12345

#### Populate with data for testing on resources/import.sql
````sql
INSERT INTO users (username, password, enabled) VALUES ('user', '$2a$10$UjkBbFTTLtrVrPWKm4AmjufiyGGGprc04nxghBeWmWyP1o25lA.ka', 1);
INSERT INTO users (username, password, enabled) VALUES ('admin', '$2a$10$U.kxzZsFe3.1Uw3qgVicXek9X8HeyRbVGMRsG3VeuoGWRXyV2zHF2', 1);

INSERT INTO authorities (user_id, authority) VALUES ('1', 'ROLE_USER');
INSERT INTO authorities (user_id, authority) VALUES ('2', 'ROLE_USER');
INSERT INTO authorities (user_id, authority) VALUES ('2', 'ROLE_ADMIN');
````