package com.buaa01.illumineer_backend.config.filter;

import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.service.impl.user.UserDetailsImpl;
import com.buaa01.illumineer_backend.tool.JsonWebTokenTool;
import com.buaa01.illumineer_backend.tool.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JsonWebTokenTool jsonWebTokenTool;
    @Autowired
    private RedisTool redisTool;

    /**
     * 认证过滤器，所有token都需要进入这里进行验证
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            // 通过开放接口过滤器后，如果没有可解析的token就放行
            System.out.println("judge token");
            filterChain.doFilter(request, response);
            return;
        }
        token = token.substring(7);
        //解析token
        boolean verifyToken = jsonWebTokenTool.verifyToken(token);
        if (!verifyToken) {
            response.addHeader("message", "not login"); // 设置响应头信息，给前端判断用
            response.setStatus(403);
            return;
        }
        String userId = JsonWebTokenTool.getSubjectFromToken(token);
        String role = JsonWebTokenTool.getClaimFromToken(token, "role");

        // 从redis中获取用户信息
        User user = redisTool.getObjectByClass("security:" + role + ":" + userId, User.class);

        if (user == null) {
            response.addHeader("message", "not login"); // 设置响应头信息，给前端判断用
            response.setStatus(403);
            return;
        }

        // 存入SecurityContextHolder，这里建议只供读取uid用，其中的状态等非静态数据可能不准，所以建议redis另外存值
        UserDetailsImpl loginUser = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 放行
        filterChain.doFilter(request, response);
    }
}
