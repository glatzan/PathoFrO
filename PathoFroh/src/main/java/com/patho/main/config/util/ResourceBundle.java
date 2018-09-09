package com.patho.main.config.util;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.common.DateFormat;
import com.patho.main.util.helper.TimeUtil;

@Component(value = "msg")
@Scope("singleton")
@Primary
public class ResourceBundle extends HashMap<Object, Object> {

	private static final long serialVersionUID = 1668009329184453712L;

	// todo shift to settings
	public static final Locale DEFAULT_LOCALE = Locale.GERMAN;

	@Autowired
	private MessageSource messageSource;

	@Override
	public String get(Object key) {
		// if view root is available use the current locale, if not use the default one
		return get(key,
				FacesContext.getCurrentInstance() != null ? FacesContext.getCurrentInstance().getViewRoot().getLocale()
						: Locale.GERMAN);
	}

	public String get(Object key, Locale locale) {
		try {
			return messageSource.getMessage((String) key, null, locale);
		} catch (NoSuchMessageException e) {
			return "???" + key + "???";
		}
	}

	public String get(String key, Object... params) {
		try {
			return getFormattedString(this.get(key), params);
		} catch (MissingResourceException e) {
			return "???" + key + "???";
		}
	}

	public String get(String key, Locale locale, Object... params) {
		try {
			return getFormattedString(this.get(key, locale), params);
		} catch (MissingResourceException e) {
			return "???" + key + "???";
		}
	}

	public String getFormattedString(String str, Object... params) {
		for (int i = 0; i < params.length; i++) {
			if (params[i].toString().startsWith("date:")) {
				String time = params[i].toString().replaceAll("date:", "");
				if (time.matches("[0-9]*")) {
					params[i] = TimeUtil.formatDate(Long.valueOf(time), DateFormat.GERMAN_DATE.getDateFormat());
				}
			}
		}
		return MessageFormat.format(str, params);
	}

	public static ResourceBundle getResourceBundle() {
		try {
			ResourceBundle bean = ApplicationContextProvider.getContext().getBean(ResourceBundle.class);
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}