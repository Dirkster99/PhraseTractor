package ProgramObjects.Errors;

/**
 * Defines an object that can be used to handle common errors as well as exception caught
 * in a deeper on a sub-routine.
 */
public class ErrorObject
{
    private String _Message;
    private ErrorSeverity _Severity;
    private Exception _Exp;

    /**
     * Class constructor from parameters
     * @param message
     * @param exp
     */
    public ErrorObject(String message, Exception exp)
    {
        super();

        _Message = message;
        _Severity = ErrorSeverity.ERROR;
        _Exp = exp;
    }

    /**
     * Class constructor from parameters
     * @param exp
     */
    public ErrorObject(Exception exp)
    {
        super();

        _Message = null;
        _Severity = ErrorSeverity.ERROR;
        _Exp = exp;
    }

    /**
     * Class constructor from parameters
     * @param message
     * @param severity
     */
    public ErrorObject(String message, ErrorSeverity severity)
    {
        super();

        _Message = message;
        _Severity = severity;
    }

    /**
     * Get the error Message for this error object.
     * @return
     */
    public String getMessage()
    {
        if (_Message != null && _Exp == null)
            return _Message;

        if (_Message == null && _Exp != null)
            return _Exp.getMessage() + "\n" + _Exp.getStackTrace().toString();

        if (_Message != null && _Exp != null)
            return _Message + "\n" + _Exp.getMessage() + "\n" + _Exp.getStackTrace().toString();
        
        return "Internal ERROR: No Error message in object found!";
    }

    /**
     * Get the Exception (if any) inside of this error object.
     * @return
     */    
    public Exception getException()
    {
        return _Exp;
    }

    /**
     * Get the Stacktrace of an Exception (if any) as string description.
     * @return
     */
    public String getStacktrace()
    {
        return _Exp.getStackTrace().toString();
    }

    /** Get the severity (Error, Warning) indicated as enumeration */
    public ErrorSeverity getSeverity(){ return _Severity; }

    /** Print details about this error in a human-readable fashion. */
	public void printDetails()
    {
        switch(_Severity)
        {
            case ERROR:
                System.out.printf("ERROR\n");
                System.out.printf("ERROR\n");
                System.out.printf("ERROR %s\n", getMessage());
                System.out.printf("\n");
            break;

            case WARNING:
                System.out.printf("WARNING\n");
                System.out.printf("WARNING %s\n", getMessage());
                System.out.printf("\n");
            break;

        default:
            System.out.printf("UNKNOWN ERROR LEVEL\n");
            System.out.printf("%s\n", getMessage());
        }
	}
}