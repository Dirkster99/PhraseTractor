package ProgramObjects;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;

/**
 * Load a properties file without having to escape backslashes as they are used on a Windows path
 * https://stackoverflow.com/questions/6233532/reading-java-properties-file-without-escaping-values
 */
public class Config
{
     public static HashMap<String, String> readConfig(String fileName) throws IOException, Exception
     {
        HashMap<String, String> ret = new HashMap<String, String>();
        //...
        try (FileInputStream fis = new FileInputStream(fileName);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
            )
        {
      
            String str;
            int lineNumber=0;
            while ((str = reader.readLine()) != null)
            {
                if (str == null || str.length() == 0 )
                    continue;

                String [] keyValue = str.split("=");

                if (keyValue.length > 2 || str.contains("=") == false)
                    throw new Exception(String.format("Config file has invalid line '%s' in line %d.", str, lineNumber));

                if (keyValue.length == 2)
                    ret.put(keyValue[0].trim(), keyValue[1].trim());
                else
                    ret.put(keyValue[0].trim(), "");  // Assume a key without a value (or default value whatever that may be)

                lineNumber++;
            }      
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Exception(e);
        }

        return ret;
     }
}