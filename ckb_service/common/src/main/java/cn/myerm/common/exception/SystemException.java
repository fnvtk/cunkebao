package cn.myerm.common.exception;

public class SystemException extends RuntimeException {

    private static final long serialVersionUID = 1095242212086237834L;

    protected Long errorCode;
    protected Object[] args;
    protected Object data;

    public SystemException() {
        super();
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(String message) {
        super(message);
        this.errorCode = 10001L;
    }

    public SystemException(String message, Object[] args, Throwable cause) {
        super(message, cause);
        this.args = args;
    }

    public SystemException(String message, Object[] args) {
        super(message);
        this.args = args;
    }

    public SystemException(Long errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public SystemException(Long errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SystemException(Long errorCode, String message, Object[] args, Throwable cause) {
        super(message, cause);
        this.args = args;
        this.errorCode = errorCode;
    }

    public SystemException(Long errorCode, String message, Object[] args) {
        super(message);
        this.args = args;
        this.errorCode = errorCode;
    }

    public SystemException(Long errorCode, String message, Object data) {
        super(message);
        this.data = data;
        this.errorCode = errorCode;
    }

    public SystemException(Throwable cause) {
        super(cause);
    }

    public Object[] getArgs() {
        return args;
    }

    public Long getErrorCode() {
        return errorCode;
    }

    public Object getData() {
        return data;
    }
}
