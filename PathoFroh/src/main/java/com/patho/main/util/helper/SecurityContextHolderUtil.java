package com.patho.main.util.helper;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SecurityContextHolderUtil {

    /**
     * Setes a key value pair to the securityContext. Workaround for passing values
     * to the RevisionListener.
     *
     * @param key
     * @param value
     */
    @SuppressWarnings("unchecked")
    public static void setObjectToSecurityContext(String key, Object value) {
        SecurityContext sec = SecurityContextHolder.getContext();
        AbstractAuthenticationToken auth = (AbstractAuthenticationToken) sec.getAuthentication();

        if (auth != null) {
            HashMap<String, Object> info = null;

            if (auth.getDetails() == null || !(auth.getDetails() instanceof Map<?, ?>)) {
                info = new HashMap<String, Object>();
                auth.setDetails(info);
            } else
                info = ((HashMap<String, Object>) auth.getDetails());

            info.put(key, value);
        }
    }

    /**
     * Clears an object from the securityContext
     *
     * @param key
     */
    public static void clearObjectFromSecruityContext(String key) {
        setObjectToSecurityContext(key, null);
    }

    /**
     * Reads a value for the passed key from the securityContext. Workaround for
     * passing values to the RevisionListener.
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <S> Optional<S> getObjectFromSecurityContext(String key) {
        try {
            if (SecurityContextHolder.getContext() == null
                    || SecurityContextHolder.getContext().getAuthentication() == null)
                return Optional.empty();

            if (SecurityContextHolder.getContext().getAuthentication().getDetails() instanceof Map<?, ?>) {
                Map<String, Object> info = (Map<String, Object>) SecurityContextHolder.getContext().getAuthentication()
                        .getDetails();
                return Optional.ofNullable((S) info.get(key));
            }
        } catch (ClassCastException exc) {
        }

        return Optional.empty();
    }
}
