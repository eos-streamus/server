package com.eos.streamus.filters;

import com.eos.streamus.utils.JwtService;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
@Component
public class JwtFilter implements Filter {

  @Autowired
  private JwtService jwtService;

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                       final FilterChain filterChain) throws IOException, ServletException {
    if (servletRequest instanceof HttpServletRequest) {
      HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

      if (httpServletRequest.getRequestURI().contains("/users") ||
          httpServletRequest.getRequestURI().contains("/login")) {
        filterChain.doFilter(servletRequest, servletResponse);
      } else {
        String jwtTokenHeader = httpServletRequest.getHeader("Authorization");
        if (jwtTokenHeader == null || !jwtTokenHeader.startsWith("Bearer ")) {
          ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
          String jwtToken = jwtTokenHeader.substring(7);
          try {
            jwtService.decode(jwtToken);
            filterChain.doFilter(servletRequest, servletResponse);
          } catch (JwtException e) {
            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_FORBIDDEN);
          }
        }
      }
    }
  }

}
