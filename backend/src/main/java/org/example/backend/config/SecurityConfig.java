package org.example.backend.config;

import org.example.backend.common.security.filter.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Spring Security 核心过滤链配置
     * @param http HttpSecurity配置对象
     * @return 构建完成的安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 配置跨域
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 关闭CSRF防护
                .csrf(csrf -> csrf.disable())
                // 关闭默认HTTP Basic认证
                .httpBasic(basic -> basic.disable())
                // 关闭默认表单登录
                .formLogin(form -> form.disable())
                // 关闭默认登出功能
                .logout(logout -> logout.disable())
                // 配置请求授权规则
                .authorizeHttpRequests(auth -> {
                    // 配置白名单接口
                    auth.requestMatchers(
                            "/api/auth/login",
                            "/api/user/**",
                            "/api/role/**",
                            "/api/member/**",
                            "/api/org/**",
                            "/api/drive-stat/**",
                            "/api/log/**",
                            "/api/perm/**",
                            "/api/storage/**",
                            "/api/system/**",
                            "/api/traffic/**",
                            "/api/share/file",
                            "/api/share/info",
                            "/api/share/download"
                    ).permitAll();
                    auth.anyRequest().authenticated();
                })
                // 自定义异常处理
                .exceptionHandling(exception -> exception
                        // 未认证访问
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"code\":401,\"msg\":\"未认证\",\"data\":null}");
                        })
                        // 权限不足访问
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"code\":403,\"msg\":\"权限不足\",\"data\":null}");
                        })
                )
                // 配置会话管理
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 添加JWT过滤器
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 全局跨域配置
     * @return 跨域配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 开发环境用 *，生产环境指定具体域名
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Content-Disposition");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 密码加密编码器
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 用户详情服务
     * @return UserDetailsService实例
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("用户认证不通过此服务查找: " + username);
        };
    }
}
