/*
 * Copyright 2016-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patho.main.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.patho.main.config.security.filters.JWTAuthorizationFilter;
import com.patho.main.config.security.filters.JWTPasswortAuthorizationFilter;
import com.patho.main.config.security.filters.UserAuthorizationFilter;
import com.patho.main.config.security.handler.UserAuthenticationSuccessHandler;
import com.patho.main.config.security.provider.JWTAuthenticationProvider;
import com.patho.main.config.security.provider.JWTUserAuthenticationProvider;
import com.patho.main.config.security.provider.UserAuthenticationProvider;
import com.patho.main.config.security.handler.JWTAuthorizationFailureHandler;
import com.patho.main.config.security.handler.UserAuthenticationFailureHandler;

/**
 * Spring Security Configuration.
 *
 * @author Marcelo Fernandes
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private JWTAuthenticationProvider jwtAuthenticationProvider;

	@Autowired
	private UserAuthenticationProvider userAuthenticationProvider;

	@Autowired
	private JWTUserAuthenticationProvider jwtUserAuthenticationProvider;

	@Autowired
	private UserAuthenticationSuccessHandler userAuthenticationSuccessHandler;

	@Autowired
	private UserAuthenticationFailureHandler userAuthenticationFailureHandler;

	@Autowired
	private JWTAuthorizationFailureHandler jwtAuthorizationFailureHandler;

	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(HttpSecurity http) {
		try {
			// filter for generating jwt token, mapped to /rest/login
			http.addFilterBefore(new JWTPasswortAuthorizationFilter(authenticationManagerBean(),
					getApplicationContext(), jwtAuthorizationFailureHandler),
					UsernamePasswordAuthenticationFilter.class);

			// filter for rest security mapped to /rest/*
			http.addFilterBefore(new JWTAuthorizationFilter(authenticationManagerBean(), getApplicationContext(),
					jwtAuthorizationFailureHandler), UsernamePasswordAuthenticationFilter.class);

			// filter for user login mapped to /*
			http.addFilterBefore(new UserAuthorizationFilter(authenticationManagerBean(),
					userAuthenticationFailureHandler, userAuthenticationSuccessHandler),
					UsernamePasswordAuthenticationFilter.class);

			// configure authentication providers
			http.authenticationProvider(jwtAuthenticationProvider);
			http.authenticationProvider(jwtUserAuthenticationProvider);
			http.authenticationProvider(userAuthenticationProvider);

			http.headers().frameOptions().sameOrigin();
			
			// disable csrf
			http.csrf().disable();

			// setup security
			http.authorizeRequests().antMatchers("/javax.faces.resource/**").permitAll().antMatchers("/style/**")
					.permitAll().antMatchers("/gfx/**").permitAll().antMatchers("/scripts/**").permitAll().antMatchers("/javax.faces.resource/jquery/jquery-plugins.js.xhtml").permitAll()
					.antMatchers("/RES_NOT_FOUND").permitAll().antMatchers("/error").permitAll()
					.antMatchers("/login.xhtml*").permitAll().antMatchers("/**/favicon.ico").permitAll().anyRequest().authenticated().and().formLogin()
					.loginPage("/login.xhtml").permitAll().loginProcessingUrl("/perform_login").permitAll()
					.failureUrl("/login.jsf?error=true").and().logout().logoutSuccessUrl("/login.xhtml").permitAll();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Password Encoder
	 * 
	 * @return
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
