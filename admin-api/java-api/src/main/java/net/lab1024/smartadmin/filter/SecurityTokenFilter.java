package net.lab1024.smartadmin.filter;

import net.lab1024.smartadmin.common.constant.RequestHeaderConst;
import net.lab1024.smartadmin.module.system.login.domain.LoginUserDetail;
import net.lab1024.smartadmin.module.system.login.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * [  ]
 * 注意此处不能 加入@Component
 * 否则对应ignoreUrl的相关请求 将会进入此Filter，并会覆盖CorsFilter
 *
 * @author 罗伊
 * @date
 */

public class SecurityTokenFilter extends OncePerRequestFilter {

    private LoginService loginService;

    public SecurityTokenFilter(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        //需要做token校验, 消息头的token优先于请求query参数的token
        String xHeaderToken = request.getHeader(RequestHeaderConst.TOKEN);
        String xRequestToken = request.getParameter(RequestHeaderConst.TOKEN);
        String xAccessToken = null != xHeaderToken ? xHeaderToken : xRequestToken;
        if (StringUtils.isBlank(xAccessToken)) {
            chain.doFilter(request, response);
            return;
        }
        //清理spring security
        SecurityContextHolder.clearContext();
        LoginUserDetail loginUserDetail = loginService.getLoginUserDetail(xAccessToken);
        if (null != loginUserDetail) {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUserDetail, null, loginUserDetail.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        // 若未给予spring security上下文用户授权 则会授权失败 进入AuthenticationEntryPointImpl
        chain.doFilter(request, response);
    }
}