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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@WebFilter("/*")
@Component
public final class JwtFilter implements Filter {

  /** {@link com.eos.streamus.utils.JwtService} to use. */
  @Autowired
  private JwtService jwtService;

  /** Token offset in bearer String. */
  private static final int TOKEN_OFFSET = 7;

  /** {@inheritDoc} */
  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                       final FilterChain filterChain) throws IOException, ServletException {
    if (servletRequest instanceof HttpServletRequest) {
      HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

      if (httpServletRequest.getRequestURI().contains("/users") ||
          httpServletRequest.getRequestURI().contains("/login") ||
          httpServletRequest.getRequestURI().contains("/refresh")) {
        filterChain.doFilter(servletRequest, servletResponse);
      } else if (httpServletRequest.getRequestURI().contains("/stream")) {
        handleStream(httpServletRequest, servletResponse, filterChain);
      } else {
        handleStandardRequest(httpServletRequest, servletResponse, filterChain);
      }
    }
  }

  private void handleStandardRequest(final HttpServletRequest httpServletRequest, final ServletResponse servletResponse,
                                     final FilterChain filterChain) throws IOException, ServletException {
    String jwtTokenHeader = httpServletRequest.getHeader("Authorization");
    if (jwtTokenHeader == null || !jwtTokenHeader.startsWith("Bearer ")) {
      ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      String jwtToken = jwtTokenHeader.substring(TOKEN_OFFSET);
      try {
        jwtService.decode(jwtToken);
        filterChain.doFilter(httpServletRequest, servletResponse);
      } catch (JwtException e) {
        ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_FORBIDDEN);
      }
    }
  }

  private void handleStream(final HttpServletRequest httpServletRequest, final ServletResponse servletResponse,
                            final FilterChain filterChain) throws IOException, ServletException {
    Optional<Cookie> sessionCookie = Arrays.stream(httpServletRequest.getCookies())
                                           .filter(cookie -> cookie.getName().equals("streamusSessionToken"))
                                           .findFirst();
    if (sessionCookie.isPresent()) {
      String value = sessionCookie.get().getValue();
      try {
        jwtService.decode(value);
        filterChain.doFilter(httpServletRequest, servletResponse);
      } catch (JwtException e) {
        System.out.println(e.getMessage());
        ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
    }
  }

}
