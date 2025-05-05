package Request_Response;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private String command;
    private Object data;
    private Object additionalData;
    public Request(String command, Object data) {
        this.command = command;
        this.data = data;
    }
    public Request(String command, Object data, Object additionalData) {
        this.command = command;
        this.data = data;
        this.additionalData = additionalData;
    }

    public String getCommand() {
        return command;
    }

    public Object getData() {
        return data;
    }

    public Object getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Object additionalData) {
        this.additionalData = additionalData;
    }
}
