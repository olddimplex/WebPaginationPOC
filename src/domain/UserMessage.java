package domain;

import java.io.Serializable;

public class UserMessage implements Serializable{

	private static final long serialVersionUID = -3368083554737346495L;

	public enum Status {
        SUCCESS("Success"), INFO("Info"), WARNING("Warning"), ERROR("Error");

        private final String label;

        Status(final String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    private final Status status;

    private final String message;

    public UserMessage(final Status status, final String message) {
        this.status = status;
        this.message = message;
    }

    public final Status getStatus() {
        return this.status;
    }

    public final String getMessage() {
        return this.message;
    }
}
