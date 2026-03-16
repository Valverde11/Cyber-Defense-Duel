package persistence;

public class SimpleJson {
    // Un userrecord a JSON manualmente//
    public static String toJson(model.UserRecord u) {
        return "{\"username\":\"" + esc(u.username) + "\""
                + ",\"passwordHash\":\"" + esc(u.passwordHash) + "\""
                + ",\"avatar\":\"" + esc(u.avatar) + "\""
                + ",\"highScore\":" + u.highScore + "}";
    }

    // String de un Json //
    public static String readString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0)
            return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        return end < 0 ? "" : json.substring(start, end);
    }

    // Lee un campo int de un JSON simple
    public static int readInt(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0)
            return 0;
        start += search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-'))
            end++;
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (Exception e) {
            return 0;
        }
    }

    // Escapa caracteres especiales para JSON
    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
