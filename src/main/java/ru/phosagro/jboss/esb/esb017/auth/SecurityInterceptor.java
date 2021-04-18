package ru.phosagro.jboss.esb.esb017.auth;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.util.Base64;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static ru.phosagro.jboss.esb.esb017.config.PropertyParameter.*;

//@Provider
public class SecurityInterceptor implements javax.ws.rs.container.ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
        Method method = methodInvoker.getMethod();

        if (!method.isAnnotationPresent(PermitAll.class)) {

            if (method.isAnnotationPresent(DenyAll.class)) {
                requestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }

            final MultivaluedMap<String, String> headers = requestContext.getHeaders();

            final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

            if (authorization == null || authorization.isEmpty()) {
                requestContext.abortWith(ACCESS_DENIED);
                return;
            }

            final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");

            String usernameAndPassword = null;
            try {
                usernameAndPassword = new String(Base64.decode(encodedUserPassword));
            } catch (IOException e) {
                requestContext.abortWith(SERVER_ERROR);
                return;
            }

            final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
            final String username = tokenizer.nextToken();
            final String password = tokenizer.nextToken();

            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));

                if (!isUserAllowed(username, password, rolesSet)) {
                    requestContext.abortWith(ACCESS_DENIED);
                    return;
                }
            }
        }
    }

    private boolean isUserAllowed(final String username, final String password, final Set<String> rolesSet) {
        boolean isAllowed = false;

        if (username.equals(System.getProperty("SharePointWS.Login")) && password.equals(System.getProperty("SharePointWS.Password"))) {
            String userRole = "USER";

            if (rolesSet.contains(userRole)) {
                isAllowed = true;
            }
        }
        return isAllowed;
    }
}
