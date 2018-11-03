package cl.ejeldes.springsecuritymvc.config;

import cl.ejeldes.springsecuritymvc.service.JpaUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    private final BCryptPasswordEncoder passwordEncoder;
    private final JpaUserDetailsService jpaUserDetailsService;

    @Autowired
    public SpringSecurityConfig(BCryptPasswordEncoder passwordEncoder, JpaUserDetailsService jpaUserDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.jpaUserDetailsService = jpaUserDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/public").permitAll()
                .antMatchers("/private", "/user/**").hasAnyRole("USER")
                .antMatchers("/admin").hasAnyRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin().permitAll()
                .and()
                .logout().permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jpaUserDetailsService).passwordEncoder(passwordEncoder);

    }
}
