/*
        Insecure Web App (IWA)

        Copyright (C) 2022 Micro Focus or one of its affiliates

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.microfocus.example.web.controllers;

import com.microfocus.example.config.LocaleConfiguration;
import com.microfocus.example.config.handlers.CustomAuthenticationSuccessHandler;
import com.microfocus.example.entity.CustomUserDetails;
import com.microfocus.example.utils.JwtUtils;
import com.microfocus.example.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Currency;
import java.util.Locale;

/**
 * Default (root) controller
 *
 * @author Kevin A. Lee
 */
@Controller
@Scope("session")
//@SessionAttributes({"currentUser", "currentUserId", "loginReferer"})
public class DefaultController {

    private static final Logger log = LoggerFactory.getLogger(DefaultController.class);

    @Value("${app.messages.home}")
    private String message = "Hello World";

    @Autowired
    LocaleConfiguration localeConfiguration;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        Locale currentLocale = localeConfiguration.getLocale();
        Currency currency = Currency.getInstance(currentLocale);
        model.addAttribute("currencySymbol", currency.getSymbol());
        model.addAttribute("message", message);
        return "index";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model, Principal principal) {
        HttpSession session = request.getSession(false);
        String referer = (String) request.getHeader("referer");
        session.setAttribute("loginReferer", referer);
        return "login";
    }

    @GetMapping("/verify")
    public String verify(HttpServletRequest request, Model model, Principal principal) {
        return "verify";
    }

    @PostMapping("/verify")
    public String verify(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam("otp") String otp, Model model, Principal principal) {
        Authentication authentication = (Authentication) principal;
        String jwtToken = jwtUtils.generateAndSetSession(request, response, authentication);
        String targetUrl = CustomAuthenticationSuccessHandler.getTargetUrl(request, response, authentication);
        return "redirect:"+targetUrl;
    }

    @GetMapping("/services")
    public String services(Model model, Principal principal) {
        return "services";
    }

    @GetMapping("/prescriptions")
    public String prescriptions(Model model, Principal principal) {
        return "prescriptions";
    }

    @GetMapping("/advice")
    public String advice(Model model, Principal principal) {
        return "advice";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model, Principal principal) {
        if (principal != null) {
        	log.debug("DefaultController:accessDenied: " + principal.toString());
            CustomUserDetails loggedInUser = (CustomUserDetails) ((Authentication) principal).getPrincipal();
            model.addAttribute("user", WebUtils.toString(loggedInUser.getUserDetails()));
            String message = "Sorry <strong>" + principal.getName() + "</strong> - " //
                    + "you do not have permission to access this page.";
            model.addAttribute("message", message);
        }
        return "error/403-access-denied";
    }

    @GetMapping("/backdoor")
    public String backdoor(Model model, Principal principal) {
        log.debug("Oops! Someone has found the backdoor!");
        return "admin/backdoor";
    }

    @GetMapping("/not-yet-implemented")
    public String notYetImplemented(Model model, Principal principal) {
        return "/error/not-implemented.html";
    }

    @GetMapping("/site-message")
    @ResponseBody
    public String siteMessage() {
        return "This site is currently healthy.";
    }

}
