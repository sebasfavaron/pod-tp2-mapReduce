package itba.client;

public class ClientArguments {

    private String host;
    private Integer port;
    private String inPath, outPath, OACI;
    private Integer n, min;

    public ClientArguments() {
        this.host = null;
        this.port = null;
        this.inPath = null;
        this.outPath = null;
        this.OACI = null;
        this.n = null;
        this.min = null;
    }

    public void setHost(final String host) {
        if (!host.matches("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})|localhost")) {
            System.out.println("Host is not valid");
            System.exit(-1);
        }
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    private Integer stringToInt(final String str, final String errorMsg) {
        if (str == null) {
            System.out.println(errorMsg);
            System.exit(-1);
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            System.out.println(errorMsg);
            System.exit(-1);
        }
        return null;
    }

    public void setPort(final String port) {
        this.port = stringToInt(port, "Port is not valid");
    }

    public Integer getPort() {
        return port;
    }

    public String getInPath() {
        return inPath;
    }

    public void setInPath(final String inPath) {
        this.inPath = inPath;
    }

    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(final String outPath) {
        this.outPath = outPath;
    }

    public String getOACI() {
        return OACI;
    }

    public void setOACI(final String OACI) {
        this.OACI = OACI;
    }

    public Integer getN() {
        return n;
    }

    public void setN(final String n) {
        this.n = stringToInt(n, "Not a valid number");
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(final String min) {
        this.min = stringToInt(min, "Not a valid minimum number");
    }
}
