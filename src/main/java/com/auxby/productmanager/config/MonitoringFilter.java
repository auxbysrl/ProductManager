package com.auxby.productmanager.config;

import com.amazonaws.util.StringUtils;
import com.auxby.productmanager.api.v1.user.UserService;
import com.auxby.productmanager.utils.SecurityContextUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

@Component
@AllArgsConstructor
public class MonitoringFilter implements Filter {

    private final UserService userService;

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws ServletException, IOException {
        String username = SecurityContextUtil.getUsername();
        if (StringUtils.hasValue(username)) {
            userService.updateUserLastSeen(username);
        }
        chain.doFilter(request, response);
    }
}
