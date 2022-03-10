package at.htlleonding.library.persistence;

public class LibraryRepositoryException extends Exception {

    public Throwable getException() {
        return exception;
    }

    public String getMessage() {
        if(exception != null)
            return exception.getMessage();
        else
            return "";
    }

    private final Throwable exception;

    LibraryRepositoryException(Throwable e) {
    this.exception = e;
}
}
