package com.usyd.capstone.common.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http

                .csrf().disable()
                .authorizeRequests()
//                    .antMatchers("/public/**").permitAll() // 公开访问的URL
                .antMatchers("/user/**").permitAll()
//                   .antMatchers("/user/registration").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN") // 需要ADMIN角色才能访问的URL

//                  .antMatchers("/user/**").authenticated() // 需要登录才能访问的URL
                .anyRequest().authenticated() // 其他URL需要登录才能访问
                .and()
                .formLogin()
                .loginPage("/login") // 登录页面的URL
                .permitAll()
                .and()
                .logout()
                .permitAll();

        http.addFilterBefore(new JwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}