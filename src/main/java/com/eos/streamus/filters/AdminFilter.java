package com.eos.streamus.filters;

import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter()
@Component
public class AdminFilter implements Filter {
  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                       final FilterChain filterChain) throws IOException, ServletException {
    filterChain.doFilter(servletRequest, servletResponse);
  }

}
