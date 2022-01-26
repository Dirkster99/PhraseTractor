package ProgramObjects.Errors;

/** Defines a severity of an error (Warning or Error) to determine how a calling process
 * should deal with an error returned from a sub-routine.*/
public enum ErrorSeverity
{
    // This can be used to indicate a fatel error
    ERROR,

    // This can be used to indicate a non-fatal error situation
    WARNING
}
