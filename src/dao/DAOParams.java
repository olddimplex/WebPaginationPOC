package dao;

import java.util.HashMap;
import java.util.Map;

public final class DAOParams {

    private Map<String, Object> params;

    public DAOParams(final Map<String, Object> params) {
        this.params = params;
    }

    public DAOParams() {
        this(new HashMap<String, Object>());
    }

    public void addParameter(final String key, final Object value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
    }

    public void removeParameter(final String key) {
        if (params != null) {
            params.remove(key);
        }
    }

    public Object getParameter(final String key) {
        if (params != null) {
            return params.get(key);
        }
        return null;
    }

	@SuppressWarnings("unchecked")
	public <T> T getParameter(final String key, final Class<T> clazz) {
        final Object value = this.getParameter(key);
        if (value == null) {
            return null;
        }

        if (value.getClass().isAssignableFrom(clazz)) {
            return (T) value;
        }

        throw new IllegalArgumentException("The value is not of the specified class " + clazz.getName());
    }

    @Override
    public String toString() {
        return "DAOParams [params=" + params + "]";
    }
    
    public boolean isEmpty() {
        return params.isEmpty();
    }

}
