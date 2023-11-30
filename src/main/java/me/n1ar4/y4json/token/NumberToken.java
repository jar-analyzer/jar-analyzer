package me.n1ar4.y4json.token;

public class NumberToken {
    private final String strValue;
    private Class<?> type;
    private double doubleValue;
    private int intValue;
    private long longValue;

    public NumberToken(String strValue) {
        this.strValue = strValue;
        try {
            this.intValue = Integer.parseInt(strValue);
            this.type = int.class;
            return;
        } catch (NumberFormatException ignored) {
        }
        try {
            this.longValue = Long.parseLong(strValue);
            this.type = long.class;
            return;
        } catch (NumberFormatException ignored) {
        }
        try {
            this.doubleValue = Double.parseDouble(strValue);
            this.type = double.class;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("not support number format: " + strValue);
        }
    }

    public Class<?> getType() {
        return this.type;
    }

    public String getStrValue() {
        return strValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public long getLongValue() {
        return longValue;
    }
}
