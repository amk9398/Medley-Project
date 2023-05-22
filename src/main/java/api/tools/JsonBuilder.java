package api.tools;

public class JsonBuilder {

    public static JsonTree parse(String jsonString) {
        String[] lines = jsonString.split("\n");
        StringBuilder builder = new StringBuilder();
        for(int i = 1; i < lines.length - 1; i++) builder.append(lines[i]).append("\n");
        return parseR(builder.toString());
    }

    private static JsonTree parseR(String jsonString) {
        JsonTree jsonTree = new JsonTree(jsonString);
        String[] lines = jsonString.split("\n");
        int indentSize = numLeadingSpaces(lines[0]);

        for(int i = 0; i < lines.length; i++) {
            if(lines[i].contains(": [")) {
                StringBuilder builder = new StringBuilder();
                int listIndentSize = numLeadingSpaces(lines[i]);

                for(int j = i; j < lines.length; j++) {
                    builder.append(lines[j]).append("\n");
                    if(lines[j].contains(" ]") && numLeadingSpaces(lines[j]) == listIndentSize) break;
                }

                String listJsonString = builder.toString();
                JsonTree listTree = new JsonTree(listJsonString);
                String key = lines[i].split("\"")[1];

                String[] lines2 = listJsonString.split("\n");
                builder = new StringBuilder();
                for(int j = 1; j < lines2.length; j++) {
                    if(numLeadingSpaces(lines2[j]) == listIndentSize) {
                        listTree.add(Integer.toString(listTree.getMap().keySet().size()), parseR(builder.toString()));
                        builder = new StringBuilder();
                    } else {
                        builder.append(lines2[j]).append("\n");
                    }
                }

                jsonTree.add(key, listTree);
                i += lines2.length - 1;
            } else {
                StringBuilder builder = new StringBuilder();
                int j = i;
                int loopTaken = 0;

                if (j + 1 < lines.length) {
                    while (numLeadingSpaces(lines[j + 1]) != indentSize) {
                        builder.append(lines[++j]).append("\n");
                        loopTaken = 1;
                    }
                }

                String subTreeString = builder.toString();
                String key = lines[i].split("\"")[1];
                if (subTreeString.isEmpty()) {
                    try {
                        String value = lines[i].split("\"")[3];
                        jsonTree.add(key, new JsonTree(value));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        String value = lines[i].split(":")[1].replace(" ", "").replace(",", "");
                        jsonTree.add(key, new JsonTree(value));
                    }
                } else {
                    jsonTree.add(key, parseR(subTreeString));
                }

                i = j + loopTaken;
            }
        }

        return jsonTree;
    }

    private static int numLeadingSpaces(String str) {
        int count = 0;
        for(int i = 0; i < str.length(); i++) {
            if(str.charAt(i) == ' ') count++;
            else break;
        }
        return count;
    }


}

