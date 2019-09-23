package poulet.superficial.imports;

import poulet.PouletException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileSearcher {
    public static String findFile(String fileName, List<String> directories, List<Boolean> recursive) {
        List<String> results = new ArrayList<>();
        for(int i = 0; i < directories.size(); i++) {
            boolean recur = i < recursive.size() ? recursive.get(i) : true;
            results.addAll(findMatches(fileName, new File(directories.get(i)), recur));
        }
        if(results.size() > 0) {
            if(results.size() > 1)
                throw new PouletException("multiple valid imports found for filename " + fileName);
            return results.get(0);
        }
        return null;
    }

    private static List<String> findMatches(String fileName, File directory, boolean recursive) {
        try {
            if (!directory.isDirectory())
                throw new PouletException("linked library " + directory.getCanonicalPath() + " is not a directory");

            List<String> result = new ArrayList<>();
            if (directory.canRead()) {
                for (File temp : directory.listFiles()) {
                    if (temp.isDirectory() && recursive) {
                        result.addAll(findMatches(fileName, temp, recursive));
                    } else if (fileName.equals(temp.getName()))
                        result.add(temp.getCanonicalPath());
                }
            } else {
                if (!directory.exists())
                    throw new PouletException("linked library " + directory.getCanonicalPath() + " does not exist");
                else
                    throw new PouletException("linked library " + directory.getCanonicalPath() + " does not have read permission");
            }
            return result;
        } catch (IOException exception) {
            throw new PouletException("error searching linked libraries");
        }
    }
}
