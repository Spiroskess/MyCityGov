package gr.hua.dit.mycitygov.web.api.advice;

public class ApiError {
    private int status;
    private String message;

    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
    }

}
