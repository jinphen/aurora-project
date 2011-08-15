package aurora.plugin.sap.sync.idoc;



public class ApplicationException extends Exception {
	private static final long serialVersionUID = -3184478964424768398L;

	public ApplicationException() {
		super("Error occurred in application.");
	}

	public ApplicationException(String message) {
		super(message);
	}

	public ApplicationException(String message, Throwable cause) {
		super(message,cause);
	}
	public ApplicationException(Throwable cause) {
		super(cause);
	}
}
