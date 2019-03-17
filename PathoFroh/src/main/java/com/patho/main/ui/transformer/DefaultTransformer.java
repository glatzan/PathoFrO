package com.patho.main.ui.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.patho.main.model.interfaces.ID;
import com.patho.main.util.helper.StreamUtils;

import lombok.Getter;
import lombok.Setter;

public class DefaultTransformer<T extends ID> implements Converter {

	private List<T> objects;

	@Getter
	@Setter
	private boolean advancedMode;

	public DefaultTransformer(T[] objects) {
		this(Arrays.asList(objects));
	}

	public DefaultTransformer(List<T> objects) {
		this(objects, false);
	}
	
	public DefaultTransformer(Set<T> objects) {
		this(objects, false);
	}

	public DefaultTransformer(T[] objects, boolean advancedMode) {
		this(Arrays.asList(objects),advancedMode);
	}
	
	public DefaultTransformer(Set<T> objects, boolean advancedMode) {
		this(new ArrayList<T>(objects), advancedMode);
	}
	
	public DefaultTransformer(List<T> objects, boolean advancedMode) {
		this.objects = objects;
		this.advancedMode = advancedMode;
	}

	public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
		if (value != null && value.trim().length() > 0) {
			try {
				if (advancedMode) {
					String[] arr = value.split("_");
					int hashCode = Integer.valueOf(arr[0]);
					long id = Long.valueOf(arr[1]);
					return objects.stream().filter(p -> p.getId() == id && p.hashCode() == hashCode).collect(StreamUtils.firstInListCollector());
				} else {
					long id = Long.valueOf(value);
					return objects.stream().filter(p -> p.getId() == id).collect(StreamUtils.firstInListCollector());
				}
			} catch (NumberFormatException | IllegalStateException | NullPointerException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		if (object != null && object instanceof ID) {
			return (advancedMode ? object.hashCode() + "_" : "") + String.valueOf(((T) object).getId());
		} else {
			return "";
		}
	}
}